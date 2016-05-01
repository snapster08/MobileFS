package mfs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import mfs.network.Message;
import mfs.network.MessageContract;
import mfs.node.MobileNode;
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
    public static String getIpFromLink(String link){
        String [] linkTokens = link.split(":");
        if(linkTokens.length < 1) {
            return null;
        }
        else {
            return linkTokens[0];
        }
    }
    public static int getPortFromLink(String link){
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

    public static JSONObject nodeToJson(MobileNode node) {
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

    public static JSONArray nodeListToJson(List<MobileNode> nodeList) {
        JSONArray jsonList = new JSONArray();
        for(MobileNode node : nodeList) {
            JSONObject nodeJson  = nodeToJson(node);
            jsonList.put(nodeJson);
        }
        return jsonList;
    }
}
