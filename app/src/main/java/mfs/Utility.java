package mfs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

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
            int type = messageJson.getInt(MessageContract.Field.FIELD_MSG_TYPE);
            String body = messageJson.getString(MessageContract.Field.FIELD_MSG_BODY);
            return new Message(type, body);
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException", e);
            return null;
        }
    }

    public static String convertMessagetoString(Message message) {
        try{
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put(MessageContract.Field.FIELD_MSG_TYPE, message.getType());
            jsonMessage.put(MessageContract.Field.FIELD_MSG_BODY, message.getBody());
            return jsonMessage.toString();
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable convert Message to JSON", e);
            return null;
        }
    }

    public static JSONObject convertNodeToJson(MobileNode node) {
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

    public static String convertFileMetadataToJson(File file) {
        try{
            JSONObject json = new JSONObject();
            json.put(MessageContract.Field.FIELD_FILE_PATH, file.getAbsolutePath());
            json.put(MessageContract.Field.FIELD_FILE_SIZE, file.length());
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


    public static JSONObject getFileSystemStructure(String path, boolean includeHidden) {

        File root = new File(path);
        // return null if the path is not a directory
        if(!root.isDirectory()) {
            return null;
        }

        JSONObject fileSystemStructure = new JSONObject();
        try {
            File [] fileList = root.listFiles();
            JSONArray fileSystemArray = new JSONArray();
            for (File currentFile: fileList) {
                // ignore hidden files, based on the option
                if(!includeHidden && currentFile.isHidden()){
                    continue;
                }
                // for files just add the name
                if(currentFile.isFile()){
                    fileSystemArray.put(currentFile.getName());
                    continue;
                }
                // for directories create a JSONObject {"directoryname" : [<contents>]}
                fileSystemArray.put(getFileSystemStructure(currentFile.getAbsolutePath(), includeHidden));
            }
            fileSystemStructure.put(root.getName(), fileSystemArray);
        }
        catch (JSONException e) {
            Log.i(LOG_TAG, "Improper format", e);
            return null;
        }
        return fileSystemStructure;
    }

}
