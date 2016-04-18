package mfs.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Messenger;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import mfs.service.BackgroundService;
import mfs.service.BackgroundServiceConnection;
import mobilefs.seminar.pdfs.service.R;

public class HomeActivity extends AppCompatActivity {

    Button mJoinGroupButton;
    Button mCreateGroupButton;
    ViewGroup mNoGroupLayout;
    FloatingActionButton mFab;
    ListView membersListView;
    BackgroundService mBgService;

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
            }
        });

        // check if connected to a group, if connected do mNoGroupLayout.setVisibility(View.INVISIBLE);
        //if(NodeManager.isConnectedToGroup())
        membersListView.setVisibility(View.INVISIBLE);

        // bind to background service
        BackgroundServiceConnection connection = new BackgroundServiceConnection();
        bindService(new Intent(getApplicationContext(),
                BackgroundService.class), connection, Context.BIND_AUTO_CREATE);

        Messenger service = connection.getService();
        if(service != null){
            Toast.makeText(this, "Connected to background service.",
                       Toast.LENGTH_SHORT).show();
        }

        unbindService(connection);


    }
}
