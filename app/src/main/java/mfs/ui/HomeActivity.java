package mfs.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import mfs.service.BackgroundService;
import mfs.service.BackgroundServiceConnection;
import mfs.service.MessageContract;
import mfs.service.ResponseHandler;
import mobilefs.seminar.pdfs.service.R;

public class HomeActivity extends AppCompatActivity {

    public final static String LOG_TAG = HomeActivity.class.getSimpleName();
    Button mJoinGroupButton;
    Button mCreateGroupButton;
    ViewGroup mNoGroupLayout;
    FloatingActionButton mFab;
    ListView membersListView;
    BackgroundServiceConnection mBgServiceConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mJoinGroupButton = (Button) findViewById(R.id.button_join);
        mCreateGroupButton = (Button) findViewById(R.id.button_create);
        mNoGroupLayout = (LinearLayout) findViewById(R.id.layout_noGroup);
        mFab = (FloatingActionButton) findViewById(R.id.fab_add);
        membersListView = (ListView) findViewById(R.id.list_members);

        mJoinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open the join group Activity
                Intent intent = new Intent(HomeActivity.this, JoinGroupActivity.class);
                startActivity(intent);

            }
        });

        mCreateGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open the create group Activity
                Intent intent = new Intent(HomeActivity.this, CreateGroupActivity.class);
                startActivity(intent);
            }
        });

        mFab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //temp
                Snackbar.make(mJoinGroupButton, "Does Nothing for now", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                // sample to send message to bg service
                Messenger bgService = mBgServiceConn.getService();

                if(bgService != null){
                    Message request = Message.obtain(null, MessageContract.MSG_HELLO);
                    request.replyTo = new Messenger(new ResponseHandler());
                    try{
                        bgService.send(request);
                    }
                    catch (RemoteException e)
                    {
                        Log.e(LOG_TAG, "Unable to send request bg service.");
                    }
                }
                else {
                    Log.e(LOG_TAG, "Not connected to bg service.");

                }
            }
        });

        // check if connected to a group, if connected do mNoGroupLayout.setVisibility(View.INVISIBLE);
        //if(NodeManager.isConnectedToGroup())
        membersListView.setVisibility(View.INVISIBLE);

        // bind to background service
        mBgServiceConn = new BackgroundServiceConnection();
        startService(new Intent(this,BackgroundService.class));
        bindService(new Intent(this,
                BackgroundService.class), mBgServiceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mBgServiceConn);
    }
}
