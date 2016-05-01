package mfs.service;

import android.os.Handler;
import android.os.Message;

/**
 * Created by barry on 4/18/16.
 */
public class RequestHandler extends Handler {
    public final String LOG_TAG = RequestHandler.class.getSimpleName();
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what)
        {
//            case MessageContract.MSG_HELLO:
//                Log.i(LOG_TAG, "Received MSG_HELLO from a client.");
//                // get a new message object with these parameters
//                Message reponse = Message.obtain(null, MessageContract.MSG_HELLO_BACK);
//                try{
//                    msg.replyTo.send(reponse);
//                }
//                catch (RemoteException e)
//                {
//                    Log.e(LOG_TAG, "Unable to send response for MSG_HELLO");
//                }
//                break;
            default:
                super.handleMessage(msg);
        }
    }
}
