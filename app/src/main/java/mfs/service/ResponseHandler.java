package mfs.service;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ResponseHandler extends Handler {

    public final static String LOG_TAG = ResponseHandler.class.getSimpleName();

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what){
            case MessageContract.MSG_HELLO_BACK:
                Log.i(LOG_TAG, "Received MSG_HELLO_BACK from Service.");
                break;
            default:
                super.handleMessage(msg);
        }

    }
}