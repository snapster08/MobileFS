package mfs.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

public class BackgroundServiceConnection implements ServiceConnection {

    public final static String LOG_TAG = BackgroundService.class.getSimpleName();

    /** Messenger for communicating with service. */
    Messenger mService = null;

    /**
     * Called when a connection to the Service has been established, with
     * the {@link IBinder} of the communication channel to the
     * Service.
     *
     * @param name    The concrete component name of the service that has
     *                been connected.
     * @param service The IBinder of the Service's communication channel,
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i(LOG_TAG, "Connected to the bg service.");
        mService = new Messenger(service);
    }

    /**
     * Called when a connection to the Service has been lost.  This typically
     * happens when the process hosting the service has crashed or been killed.
     * This does <em>not</em> remove the ServiceConnection itself -- this
     * binding to the service will remain active, and you will receive a call
     * to {@link #onServiceConnected} when the Service is next running.
     *
     * @param name The concrete component name of the service whose
     *             connection has been lost.
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(LOG_TAG, "Disconnected from the bg service.");
        mService = null;
    }

    public Messenger getService(){
        return mService;
    }
}
