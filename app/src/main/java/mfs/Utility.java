package mfs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import mfs.network.Message;
import mfs.network.MessageContract;
import mfs.node.MobileNode;
import mfs.node.MobileNodeImpl;
import mobilefs.seminar.pdfs.service.R;

public class Utility {
    private static final String LOG_TAG = Utility.class.getSimpleName();

    public static void setServiceStarted(Context context, boolean isStarted)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(context.getString(R.string.key_server_isStarted), isStarted);
        editor.apply();
    }

    public static boolean isServiceStarted(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.key_server_isStarted), false);
    }
    public static String getIpFromAddress(String link){
        String [] linkTokens = link.split(":");
        if(linkTokens.length < 1) {
            return null;
        }
        else {
            return linkTokens[0];
        }
    }
    public static int getPortFromAddress(String link){
        String [] linkTokens = link.split(":");
        if(linkTokens.length < 2) {
            return 0;
        }
        else {
            return Integer.parseInt(linkTokens[1]);
        }
    }

    public static Message convertStringToMessage(String messageString) {
        try {
            JSONObject messageJson = new JSONObject(messageString);
            String senderId = messageJson.getString(MessageContract.Field.FIELD_MSG_SENDER_ID);
            int type = messageJson.getInt(MessageContract.Field.FIELD_MSG_TYPE);
            String body = messageJson.getString(MessageContract.Field.FIELD_MSG_BODY);
            return new Message(senderId, type, body);
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException", e);
            return null;
        }
    }

    public static String convertMessageToString(Message message) {
        try{
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put(MessageContract.Field.FIELD_MSG_SENDER_ID, message.getSenderId());
            jsonMessage.put(MessageContract.Field.FIELD_MSG_TYPE, message.getType());
            jsonMessage.put(MessageContract.Field.FIELD_MSG_BODY, message.getBody());
            return jsonMessage.toString();
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable convert Message to JSON", e);
            return null;
        }
    }

    public static JSONObject convertNodeToJson(MobileNode node) {
        if(node == null) {
            return null;
        }

        try{
            JSONObject json = new JSONObject();
            json.put(MessageContract.Field.FIELD_NODE_ID, node.getId());
            json.put(MessageContract.Field.FIELD_NODE_NAME, node.getName());
            json.put(MessageContract.Field.FIELD_NODE_ADDRESS, node.getAddress());
            return json;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable convert Node to JSON", e);
            return null;
        }
    }

    public static JSONArray convertNodeListToJson(List<MobileNode> nodeList) {
        JSONArray jsonList = new JSONArray();
        for(MobileNode node : nodeList) {
            JSONObject nodeJson  = convertNodeToJson(node);
            jsonList.put(nodeJson);
        }
        return jsonList;
    }

    public static MobileNode convertJsonToNode(String node) {
        try {
            JSONObject nodeJson = new JSONObject(node);
            return new MobileNodeImpl(
                    nodeJson.getString(MessageContract.Field.FIELD_NODE_ID),
                    nodeJson.getString(MessageContract.Field.FIELD_NODE_NAME),
                    nodeJson.getString(MessageContract.Field.FIELD_NODE_ADDRESS));
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to convert string to Mobile Node.", e);
            return null;
        }
    }

    public static List<MobileNode> convertJsonToNodeList(String list) {
        try {
            JSONArray jsonList = new JSONArray(list);
            List<MobileNode> nodeList = new LinkedList<>();
            for(int i = 0; i < jsonList.length(); i++) {
                JSONObject nodeJson = jsonList.getJSONObject(i);
                nodeList.add(convertJsonToNode(nodeJson.toString()));
            }
            return nodeList;
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to convert string to Mobile Node list.", e);
            return null;
        }
    }


    public static String[] convertJsonToFileMetadata(String metsdata) {
        try{
            JSONObject json = new JSONObject(metsdata);
            String filepath = json.getString(MessageContract.Field.FIELD_FILE_PATH);
            String filesize = json.getString(MessageContract.Field.FIELD_FILE_SIZE);
            return new String [] {filepath, filesize};
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable convert Node to JSON", e);
            return null;
        }
    }

    public static String convertFileMetadataToJson(String fullPath, long fileSize) {
        try{
            JSONObject json = new JSONObject();
            json.put(MessageContract.Field.FIELD_FILE_PATH, fullPath);
            json.put(MessageContract.Field.FIELD_FILE_SIZE, fileSize);
            return json.toString();
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable convert Node to JSON", e);
            return null;
        }
    }
    public static String genHash(String input)  {
        MessageDigest sha1 = null;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            Log.e(LOG_TAG, "Exception: SHA-1 algorithm not found", e);
        }
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }


    public static JSONObject getFilesystemMetadata(String path, boolean includeHidden) {
        File root = new File(path);
        JSONObject directory = new JSONObject();
        try {
            // terminating condition
            if(!root.isDirectory()) {
                directory.put(MessageContract.Field.FIELD_FS_FILE_NAME, root.getName());
                directory.put(MessageContract.Field.FIELD_FS_FILE_TYPE, "file");
                return directory;
            }
            File [] fileList = root.listFiles();
            JSONArray fileSystemArray = new JSONArray();
            for (File currentFile: fileList) {
                // ignore hidden files, based on the option
                if(!includeHidden && currentFile.isHidden()){
                    continue;
                }
                // for files just add the name
                if(currentFile.isFile()){
                    JSONObject file = new JSONObject();
                    file.put(MessageContract.Field.FIELD_FS_FILE_NAME, currentFile.getName());
                    file.put(MessageContract.Field.FIELD_FS_FILE_TYPE, "file");
                    fileSystemArray.put(file);
                    continue;
                }

                // for directories
                JSONObject file = new JSONObject();
                file.put(MessageContract.Field.FIELD_FS_FILE_NAME, currentFile.getName());
                file.put(MessageContract.Field.FIELD_FS_FILE_TYPE, "directory");
                fileSystemArray.put(file);

                //fileSystemArray.put(getFilesystemMetadata(currentFile.getAbsolutePath(), includeHidden));

            }
            directory.put(MessageContract.Field.FIELD_FS_FILE_NAME, root.getName());
            directory.put(MessageContract.Field.FIELD_FS_FILE_TYPE, "directory");
            directory.put(MessageContract.Field.FIELD_FS_FILE_CONTENTS,
                    fileSystemArray);
        }
        catch (JSONException e) {
            Log.i(LOG_TAG, "JSONException in getFilesystemMetadata()", e);
            return null;
        }
        return directory;
    }

    public static String getMimeType(String url)
    {
        String parts[]=url.split("\\.");
        String extension=parts[parts.length-1];
        String type = null;
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static JSONObject convertFileListToJson(List<File> sharedFiles) {
        /*
        entry structure
        {
         "name" :"directoryname"
         "type" : "directory/file"
         "contents" : [<contents>]
        }
        */
        JSONObject directory = new JSONObject();
        try {
            JSONArray sharedFilesJson = new JSONArray();
            if(sharedFiles == null || sharedFiles.isEmpty())
            {
                directory.put(MessageContract.Field.FIELD_FS_FILE_NAME, "/dummy");
                directory.put(MessageContract.Field.FIELD_FS_FILE_TYPE, "directory");
                directory.put(MessageContract.Field.FIELD_FS_FILE_CONTENTS,
                        sharedFilesJson);
                return directory;
            }

            for(File file : sharedFiles) {
                JSONObject sharedFileJson = new JSONObject();
                sharedFileJson.put(MessageContract.Field.FIELD_FS_FILE_NAME, file.getAbsolutePath());
                sharedFileJson.put(MessageContract.Field.FIELD_FS_FILE_TYPE, "file");
                sharedFilesJson.put(sharedFileJson);
            }
            directory.put(MessageContract.Field.FIELD_FS_FILE_NAME, sharedFiles.get(0).getAbsolutePath());
            directory.put(MessageContract.Field.FIELD_FS_FILE_TYPE, "directory");
            directory.put(MessageContract.Field.FIELD_FS_FILE_CONTENTS,
                    sharedFilesJson);
        } catch (JSONException e) {
            Log.i(LOG_TAG, "JSONException in convertFileListToJson()", e);
            return directory;
        }
        return directory;
    }
}
