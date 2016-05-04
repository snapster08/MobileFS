package mfs.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import mfs.Utility;
import mfs.node.MobileNode;
import mfs.node.NodeManager;
import mfs.service.ServiceAccessor;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = NetworkChangeReceiver.class.getSimpleName();
    private AsyncTask resyncTask;

    public static boolean isResyncing;

    public static boolean isResyncing() {
        return isResyncing;
    }

    public static void setIsResyncing(boolean isResyncing) {
        NetworkChangeReceiver.isResyncing = isResyncing;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG, "Received a Network change intent.");
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            Log.i(LOG_TAG, "Connected to Internet.");
            isResyncing = true;
            resyncTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Log.e(LOG_TAG, "InterruptedException", e);

                    }
                    Log.i(LOG_TAG, "Starting Re-sync Task.");
                    // send Get group info message to all my current nodes
                    NodeManager nodeManager = ServiceAccessor.getNodeManager();
                    Message getGroupInfoMessage = new Message(ServiceAccessor.getMyId(),
                            MessageContract.Type.MSG_GET_GROUP_INFO,
                            nodeManager.generateJoiningLink());

                    // this is a hack to overcome EHOSTUNREACH error
                    String myAddress = nodeManager.generateJoiningLink();
                    Client.getInstance().sendMessage(
                            Utility.getIpFromAddress(myAddress),
                            Utility.getPortFromAddress(myAddress),
                            Utility.convertMessageToString(getGroupInfoMessage)
                    );
                    // End of hack

                    synchronized (nodeManager.getCurrentNodes()) {
                        for(MobileNode node : nodeManager.getCurrentNodes()) {
                            Client.getInstance().sendMessage(
                                    Utility.getIpFromAddress(node.getAddress()),
                                    Utility.getPortFromAddress(node.getAddress()),
                                    Utility.convertMessageToString(getGroupInfoMessage)
                            );
                        }
                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else {
            Toast.makeText( context, "Not Connected to Internet.", Toast.LENGTH_SHORT ).show();
            Log.i(LOG_TAG, "Not Connected to Internet.");
        }
    }
}
