package mfs.filesystem;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import mfs.Utility;
import mfs.network.Client;
import mfs.network.Message;
import mfs.network.MessageContract;
import mfs.node.MobileNode;
import mfs.service.ServiceAccessor;

public class FilesystemImpl implements Filesystem {
    private final static String LOG_TAG = FilesystemImpl.class.getSimpleName();

    private JSONObject filesystemMetadata;
    private MobileNode owningNode;
    List<MobileFile> openfileList = new LinkedList<>();

    public void setFilesystemMetadata(JSONObject filesystemMetadata) {
        this.filesystemMetadata = filesystemMetadata;
    }

    public void setOwningNode(MobileNode owningNode) {
        this.owningNode = owningNode;
    }

    @Override
    public List<MobileFile> getOpenFiles() {
        return openfileList;
    }


    public JSONObject getFilesystemMetadata() {
        return filesystemMetadata;
    }

    public FilesystemImpl(JSONObject filesystemMetadata, MobileNode owningNode) {
        this.filesystemMetadata = filesystemMetadata;
        this.owningNode = owningNode;
    }

    @Override
    public MobileNode getOwningNode() {
        return owningNode;
    }

    public MobileFile getOpenedFile(String path) {
        for(MobileFile file : getOpenFiles()) {
            if(file.getOriginalPath().equals(path)) {
                return file;
            }
        }
        // if file not open already return null
        return null;
    }

    @Override
    public MobileFile openFile(String path) {
        MobileFile file = getOpenedFile(path);
        if(file != null) {
            return file;
        }
        // else fetch the file
        Client.Response<File> response = Client.getInstance().executeRequestFile(
                Utility.getIpFromAddress(getOwningNode().getAddress()),
                Utility.getPortFromAddress(getOwningNode().getAddress()),
                path);
        if(response == null) {
            return null;
        }
        File openedFile = response.getResult();
        response.close();

        file = new MobileFileImpl(this, path,
                openedFile.isFile()?MobileFileImpl.Type.file:MobileFileImpl.Type.directory);
        file.setLocalFileName(openedFile.getName());
        // add the fill to open file list
        openfileList.add(file);
        return file;
    }

    @Override
    public boolean commitFile(MobileFile fileHandle) {
        // send commit request
        Message commitRequest = new Message(ServiceAccessor.getMyId(),
                MessageContract.Type.MSG_COMMIT_REQUEST,
                Utility.convertFileMetadataToJson(fileHandle.getOriginalPath(),
                        fileHandle.getLocalFileObject().length()));
        MobileNode node = fileHandle.getOwningFilesystem().getOwningNode();
        Client.Response<String> response = Client.getInstance()
                .executeRequestString(Utility.getIpFromAddress(node.getAddress()),
                        Utility.getPortFromAddress(node.getAddress()),
                        Utility.convertMessageToString(commitRequest));
        if(response == null) {
            return false;
        }
        Log.i(LOG_TAG, "Received Response: " +response.getResult());
        Message responseMessage = Utility.convertStringToMessage(response.getResult());
        // check if response is failure
        if(responseMessage == null || responseMessage.getType() != MessageContract.Type.MSG_COMMIT_ACCEPT){
            return false;
        }

        // send the file contents
        try {
            File localFile = fileHandle.getLocalFileObject();
            Log.i(LOG_TAG, "Starting to send file: " +localFile.getAbsolutePath());
            final int BUFFER_SIZE = 10 * 1024;
            byte [] buffer = new byte[BUFFER_SIZE];
            long remainingFileSize = localFile.length();
            InputStream fileInputStream = new FileInputStream(localFile);
            OutputStream socketOutputStream = response.getSocket().getOutputStream();
            while(remainingFileSize > 0) {
                int bytesRead = fileInputStream.read(buffer);
                socketOutputStream.write(buffer, 0, bytesRead);
                remainingFileSize -= bytesRead;
            }
            fileInputStream.close();
            Log.i(LOG_TAG, "Done sending file: " +localFile.getAbsolutePath());
        }
        catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "File Not Found, but this has been checked already.", e);
            return false;
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Unable to get requestsocket input stream. Socket closed already??", e);
            return false;
        }

        // read commit completion response
        try {
            DataInputStream in = new DataInputStream(response.getSocket().getInputStream());
            String completionResponse = in.readUTF();
            Log.i(LOG_TAG, "Received: " +completionResponse);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error while receiving commit complete response", e);
            return false;
        }
        // close the file
        return closeFile(fileHandle);
    }

    @Override
    public boolean closeFile(MobileFile fileHandle) {
        if(fileHandle == null || fileHandle.getLocalFileName() == null) {
            return false;
        }

        // send close command to owner, this will release the lock
        Message closeCommand = new Message(ServiceAccessor.getMyId(),
                MessageContract.Type.MSG_FILE_CLOSE,
                fileHandle.getOriginalPath());
        MobileNode node = fileHandle.getOwningFilesystem().getOwningNode();
        Client.getInstance().sendMessage(Utility.getIpFromAddress(node.getAddress()),
                        Utility.getPortFromAddress(node.getAddress()),
                        Utility.convertMessageToString(closeCommand));

        // delete the local cache
        File localFile = fileHandle.getLocalFileObject();
        if(localFile == null) {
            return false;
        }
        if(!localFile.delete()) {
            return false;
        }
        fileHandle.setLocalFileName(null);

        // remove file from openFile list
        return openfileList.remove(fileHandle);
    }

    @Override
    public boolean isOpen(MobileFile file) {
        return getOpenFiles().contains(file);
    }

    @Override
    public List<MobileFile> ls(String path) {
        // parse the filesystem metadata and return
        List<MobileFile> fileList = new LinkedList<>();
        JSONObject filesystem = getFilesystemMetadata();
        try {
            String name = filesystem.getString(MessageContract.Field.FIELD_FS_FILE_NAME);
            String type = filesystem.getString(MessageContract.Field.FIELD_FS_FILE_TYPE);
            if(type.equals("file")) {
                fileList.add(new MobileFileImpl(
                        this, name, MobileFileImpl.Type.file));
                return fileList;
            }
            JSONArray contents = new JSONArray(filesystem.getString(
                    MessageContract.Field.FIELD_FS_FILE_CONTENTS));
            for(int i = 0; i < contents.length(); i++) {
                JSONObject file = contents.getJSONObject(i);
                String filename = file.getString(MessageContract.Field.FIELD_FS_FILE_NAME);
                String filetype = file.getString(MessageContract.Field.FIELD_FS_FILE_TYPE);
                if(filetype.equals("file")) {
                    fileList.add(new MobileFileImpl(
                            this, filename, MobileFileImpl.Type.file));
                }
                else {
                    fileList.add(new MobileFileImpl(
                            this,
                            filename,
                            MobileFileImpl.Type.directory));
                }
            }
            return fileList;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error in ls.", e);
            return null;
        }
    }
}
