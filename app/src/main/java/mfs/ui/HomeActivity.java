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

import mfs.node.NodeManager;
import mfs.service.BackgroundService;
import mfs.service.BackgroundServiceConnection;
import mfs.service.MessageContract;
import mfs.service.ResponseHandler;
import mfs.service.ServiceAccessor;
import mobilefs.seminar.pdfs.service.R;

public class HomeActivity extends AppCompatActivity {

    public final static String LOG_TAG = HomeActivity.class.getSimpleName();
    Button mJoinGroupButton;
    Button mCreateGroupButton;
    ViewGroup mNoGroupLayout;
    FloatingActionButton mFab;
    ListView membersListView;
    BackgroundServiceConnection mBgServiceConn;
    int mActiontype;
    NodeManager mNodeManager = ServiceAccessor.getNodeManager();

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
                // open the enterName Activity
                Intent intent = new Intent(HomeActivity.this, EnterNameActivity.class);
                intent.putExtra(Constants.TAG_ACTION_TYPE, Constants.ACTION_JOIN_GROUP);
                startActivity(intent);
            }
        });

        mCreateGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open the enterName Activity
                Intent intent = new Intent(HomeActivity.this, EnterNameActivity.class);
                intent.putExtra(Constants.TAG_ACTION_TYPE, Constants.ACTION_CREATE_GROUP);
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

        // start the background service
        mBgServiceConn = new BackgroundServiceConnection();
        startService(new Intent(this,BackgroundService.class));

//        //To get the filesystem structure
//        Filesystem fs = ServiceAccessor.getFilesystem();
//        Log.i(LOG_TAG, fs.getFileSystemStructure(
//                Environment.getExternalStorageDirectory().getAbsolutePath()).toString());

        Log.i(LOG_TAG, "On Create.");

    }

    @Override
    protected void onStart() {
        super.onStart();
        // process the extra information provided
        mActiontype = getIntent().getIntExtra(Constants.TAG_ACTION_TYPE, Constants.ACTION_NOTHING);
        Log.i(LOG_TAG, "On Start ActionType: " +mActiontype );

        switch (mActiontype) {
            case Constants.ACTION_CREATE_GROUP_DONE:
                Snackbar.make(membersListView, "Created Group.", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                break;
            case Constants.ACTION_JOIN_GROUP_DONE:
                Snackbar.make(membersListView, "Joined Group.", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                break;

        }

        // bind to the background service
        bindService(new Intent(this,
                BackgroundService.class), mBgServiceConn, Context.BIND_AUTO_CREATE);

        // check if connected to a group and display the appropriate elements
        if(mNodeManager == null || !mNodeManager.isConnectedToGroup())  {
            mNoGroupLayout.setVisibility(View.VISIBLE);
            membersListView.setVisibility(View.INVISIBLE);
            mFab.setVisibility(View.INVISIBLE);
        }
        else {
            membersListView.setVisibility(View.VISIBLE);
            mFab.setVisibility(View.VISIBLE);
            mNoGroupLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mBgServiceConn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // this will stop the service provided all the clients are unbound
        stopService(new Intent(this,BackgroundService.class));
    }
}