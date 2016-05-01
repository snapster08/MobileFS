package mfs.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Messenger;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import mfs.Utility;
import mfs.network.Server;
import mfs.network.ServerContract;
import mfs.ui.HomeActivity;
import mobilefs.seminar.pdfs.service.R;

public class BackgroundService extends Service {

    private final static String LOG_TAG = BackgroundService.class.getSimpleName();

    // This is the object that receives interactions from clients.
    private Messenger mMessenger;

    // for the notification
    private static final int PERMANENT_NOTIFICATION_ID = 1;
    public static final String ACTION_EXIT = "mobilefs.seminar.pdfs.service.action.exit";
    private Notification mPermanentNotification;

    // server
    Server mServer;

    Notification buildPermanentNotification() {

        Intent homeIntent = new Intent(this, HomeActivity.class);
        PendingIntent homePendingIntent =
                PendingIntent.getActivity(
                        getApplicationContext(),
                        0,
                        homeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Intent stopIntent = new Intent(this, BackgroundService.class);
        stopIntent.setAction(ACTION_EXIT);
        PendingIntent exitPendingIntent = PendingIntent.getService(getApplicationContext(),
                0,
                stopIntent,
                0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Listening for requests")
                        .setContentText("Touch to open.")
                        .setContentIntent(homePendingIntent)
                        .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                                "Stop", exitPendingIntent);
        return mBuilder.build();
    }

    @Override
    public void onCreate() {

        // initialize the service accessor
        ServiceAccessor.setContext(getApplicationContext());
        ServiceAccessor.setMyId(Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID));

        // initialize the permanent notification
        Utility.setServiceStarted(this, true);
        mMessenger = new Messenger(new RequestHandler());
        mPermanentNotification = buildPermanentNotification();
        Log.i(LOG_TAG, "Bg Service Created.");

        // create a server and listen to requests
        mServer = new Server();
        mServer.start(ServerContract.SERVER_PORT);
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * {@link Context#startService}, providing the arguments it supplied and a
     * unique integer token representing the start request.  Do not call this method directly.
     * <p/>
     * <p>For backwards compatibility, the default implementation calls
     * {@link #onStart} and returns either {@link #START_STICKY}
     * or {@link #START_STICKY_COMPATIBILITY}.
     * <p/>
     * <p class="caution">Note that the system calls this on your
     * service's main thread.  A service's main thread is the same
     * thread where UI operations take place for Activities running in the
     * same process.  You should always avoid stalling the main
     * thread's event loop.  When doing long-running operations,
     * network calls, or heavy disk I/O, you should kick off a new
     * thread, or use {@link AsyncTask}.</p>
     *
     * @param intent  The Intent supplied to {@link Context#startService},
     *                as given.  This may be null if the service is being restarted after
     *                its process has gone away, and it had previously returned anything
     *                except {@link #START_STICKY_COMPATIBILITY}.
     * @param flags   Additional data about this start request.  Currently either
     *                0, {@link #START_FLAG_REDELIVERY}, or {@link #START_FLAG_RETRY}.
     * @param startId A unique integer representing this specific request to
     *                start.  Use with {@link #stopSelfResult(int)}.
     * @return The return value indicates what semantics the system should
     * use for the service's current started state.  It may be one of the
     * constants associated with the {@link #START_CONTINUATION_MASK} bits.
     * @see #stopSelfResult(int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "Bg Service onStartCommand().");

        final String action = intent.getAction();
        if(action != null){
            switch (action) {
                case ACTION_EXIT:
                    Log.i(LOG_TAG, "Exiting Bg Service.");
                    stopSelf();
                    break;
            }
        }

        startForeground(PERMANENT_NOTIFICATION_ID, mPermanentNotification);
        return START_STICKY;
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.setServiceStarted(this, false);
        mMessenger = null;
        Log.i(LOG_TAG, "Bg Service Destroyed.");

        // stop the server
        mServer.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}
