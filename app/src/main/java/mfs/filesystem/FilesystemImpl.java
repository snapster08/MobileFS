package mfs.filesystem;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import mfs.Utility;
import mfs.network.Client;
import mfs.network.MessageContract;
import mfs.node.MobileNode;

public class FilesystemImpl implements Filesystem {
    private final static String LOG_TAG = FilesystemImpl.class.getSimpleName();

    private JSONObject filesystemMetadata;
    private String rootDirectory;
    private MobileNode owningNode;

    List<MobileFile> openfileList = new LinkedList<>();

    @Override
    public List<MobileFile> getOpenFiles() {
        return openfileList;
    }
    public String getRootDirectory() {
        return rootDirectory;
    }

    public JSONObject getFilesystemMetadata() {
        return filesystemMetadata;
    }

    public FilesystemImpl(String rootDirectory, JSONObject filesystemMetadata, MobileNode owningNode) {
        this.rootDirectory = rootDirectory;
        this.filesystemMetadata = filesystemMetadata;
        this.owningNode = owningNode;
    }

    public MobileNode getOwningNode() {
        return owningNode;
    }

    @Override
    public MobileFile openFile(String path, MobileNode node) {
        // check if file is already opened
        for(MobileFile file : getOpenFiles()) {
            if(file.getOriginalPath().equals(path)) {
                return file;
            }
        }

        // else fetch the file
        Client.Response<File> response = Client.getInstance().executeRequestFile(Utility.getIpFromAddress(node.getAddress()),
                Utility.getPortFromAddress(node.getAddress()), path);
        if(response == null) {
            return null;
        }
        File openedFile = response.getResult();
        response.close();

        MobileFile file = new MobileFileImpl(this, openedFile.getAbsolutePath(),
                openedFile.isFile()?MobileFileImpl.Type.file:MobileFileImpl.Type.directory);
        // add the fill to open file list
        openfileList.add(file);
        return file;
    }

    @Override
    public void commitFile(MobileFile fileHandle) {

    }

    @Override
    public void closeFile(MobileFile fileHandle) {

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
                        this, getRootDirectory() +"/" +name, MobileFileImpl.Type.file));
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
                            this, getRootDirectory() +"/" +filename, MobileFileImpl.Type.file));
                }
                else {
                    fileList.add(new MobileFileImpl(
                            this,
                            getRootDirectory() +"/" +filename,
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
