package mfs.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import mfs.Utility;
import mfs.data.DataContract;
import mfs.node.MobileNode;
import mfs.node.NodeManager;
import mfs.service.BackgroundService;
import mfs.service.BackgroundServiceConnection;
import mfs.service.ServiceAccessor;
import mobilefs.seminar.pdfs.service.R;

public class HomeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public final static String LOG_TAG = HomeActivity.class.getSimpleName();
    Button mJoinGroupButton;
    Button mCreateGroupButton;
    ViewGroup mNoGroupLayout;
    FloatingActionButton mFab;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ListView mMembersListView;
    MemberListAdapter mMembersListAdapter;
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
        mMembersListView = (ListView) findViewById(R.id.list_members);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.list_members_SwipeRefresh);

        mJoinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open the enterName Activity
                Intent intent = new Intent(HomeActivity.this, NameAndFilesActivity.class);
                intent.putExtra(Constants.TAG_ACTION_TYPE, Constants.ACTION_JOIN_GROUP);
                startActivity(intent);
            }
        });

        mCreateGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open the enterName Activity
                Intent intent = new Intent(HomeActivity.this, NameAndFilesActivity.class);
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
            }
        });

        // initialize a loader for the mMembersListView
        getLoaderManager().initLoader(Constants.LOADER_MEMBER_LIST, null, this);

        // initialize the SwipeRefresh
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshMemberList();
            }
        });

        // create a adapter for the members list
        mMembersListAdapter = new MemberListAdapter(this, R.layout.member_list_item);
        mMembersListView.setAdapter(mMembersListAdapter);

        // set up on click on the memberlist
        mMembersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // handle click on member, load new activity browse the file structure of member
                MobileNode node = mMembersListAdapter.getItem(position);
                // make an intent containing this member info
                // and pass it to the MemberDetailsActivity
                Intent detailsIntent = new Intent(HomeActivity.this, MemberDetailsActivity.class);
                detailsIntent.putExtra(Constants.TAG_MEMBER_ID, node.getId());
                startActivity(detailsIntent);
            }
        });

        //mBgServiceConn = new BackgroundServiceConnection();

        // start the background service
        startService(new Intent(this, BackgroundService.class));

//        //To get the filesystem structure
//        Filesystem fs = ServiceAccessor.getFilesystem();
//        Log.i(LOG_TAG, fs.getFilesystemMetadata(
//                Environment.getExternalStorageDirectory().getAbsolutePath()).toString());

        Log.i(LOG_TAG, "On Create.");
    }
    @Override
    protected void onResume() {
        super.onResume();
        // process the extra information provided
        Intent intent = getIntent();
        mActiontype = intent.getIntExtra(Constants.TAG_ACTION_TYPE, Constants.ACTION_NOTHING);
        intent.removeExtra(Constants.TAG_ACTION_TYPE);
        setIntent(intent);
        Log.i(LOG_TAG, "On Start ActionType: " +mActiontype );

        switch (mActiontype) {
            case Constants.ACTION_CREATE_GROUP_DONE:
                Snackbar.make(mMembersListView, "Created Group.", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                break;
            case Constants.ACTION_JOIN_GROUP_DONE:
                Snackbar.make(mMembersListView, "Joined Group.", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                break;

        }

//        // bind to the background service
//        bindService(new Intent(this,
//                BackgroundService.class), mBgServiceConn, Context.BIND_AUTO_CREATE);

        refreshMemberList();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unbindService(mBgServiceConn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if(Utility.isServiceStarted(this)) {
            menu.findItem(R.id.action_start_server).setVisible(false);
            menu.findItem(R.id.action_stop_server).setVisible(true);
            return true;
        }
        else {
            menu.findItem(R.id.action_start_server).setVisible(true);
            menu.findItem(R.id.action_stop_server).setVisible(false);
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_stop_server:
                // this will stop the service provided all the clients are unbound
                stopService(new Intent(this,BackgroundService.class));
//                unbindService(mBgServiceConn);
                return true;
            case R.id.action_start_server:
                // this will start the service if it is was not started already
                startService(new Intent(this,BackgroundService.class));
//                bindService(new Intent(this,
//                        BackgroundService.class), mBgServiceConn, Context.BIND_AUTO_CREATE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void refreshMemberList() {
        // check if connected to a group and display the appropriate elements
        mSwipeRefreshLayout.setRefreshing(true);
        if(!mNodeManager.isConnectedToGroup())  {
            mNoGroupLayout.setVisibility(View.VISIBLE);
            mMembersListView.setVisibility(View.INVISIBLE);
            mSwipeRefreshLayout.setVisibility(View.INVISIBLE);
            mFab.setVisibility(View.INVISIBLE);
        }
        else {
            mMembersListView.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
            mFab.setVisibility(View.VISIBLE);
            mNoGroupLayout.setVisibility(View.INVISIBLE);
            mMembersListAdapter.clear();
            if(mNodeManager.getCurrentNodes() != null) {
                Log.i(LOG_TAG, "Adding nodes to member list.");
                mMembersListAdapter.addAll(mNodeManager.getCurrentNodes());
            }
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                DataContract.MembersEntry.CONTENT_URI,
                null,   // projection
                null,   // selection
                null,   // selection args
                null);  // sortOrder
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOG_TAG, "Load Finished for loader: " +loader.getId());
        switch (loader.getId()) {
            case Constants.LOADER_MEMBER_LIST:
               // mMembersListAdapter.swapCursor(data);
                break;
            default:
                Log.e(LOG_TAG, "Unknown Loader Finished.");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case Constants.LOADER_MEMBER_LIST:
                //mMembersListAdapter.swapCursor(null);
                break;
            default:
                Log.e(LOG_TAG, "Unknown Loader Reset.");
        }
    }
}

