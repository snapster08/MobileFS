package mfs.ui.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.List;

import mfs.Utility;
import mfs.filesystem.Filesystem;
import mfs.filesystem.MobileFile;
import mfs.filesystem.MobileFileImpl;
import mfs.node.MobileNode;
import mfs.service.ServiceAccessor;
import mfs.ui.Constants;
import mfs.ui.adapters.FileListAdapter;
import mobilefs.seminar.pdfs.service.R;

public class MemberDetailsActivity extends AppCompatActivity {
    public final static String LOG_TAG = MemberDetailsActivity.class.getSimpleName();

    ListView mFileListView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    FileListAdapter mFileListAdapter;
    ProgressDialog connectionProgressDialog;
    ProgressDialog openProgressDialog;
    AsyncTask connectTask;
    AsyncTask openFileTask;
    boolean isConnecting, isOpeningFile;
    MobileNode mMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mMember = ServiceAccessor.getNodeManager().getNode(
                getIntent().getStringExtra(Constants.TAG_MEMBER_ID));
        if(toolbar != null) {
            toolbar.setTitle(mMember.getName() +"\'s Files");
        }
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mFileListView = (ListView) findViewById(R.id.list_files);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.list_memberDetails_SwipeRefresh);

        // create a adapter for the file list
        mFileListAdapter = new FileListAdapter(this, R.layout.file_list_item);
        mFileListView.setAdapter(mFileListAdapter);

        // initialize the SwipeRefresh
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshMemberDetails(true);
            }
        });

        // set up onClick on the file list
        mFileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // open if it is a file
                final MobileFile selectedFile = mFileListAdapter.getItem(position);
                final Filesystem filesystem = mMember.getBackingFilesystem();
                if(selectedFile.getType() == MobileFileImpl.Type.directory) {
                    Snackbar.make(mFileListView, "Cannot open directories.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else {
                    MobileFile openedFile = filesystem.getOpenedFile(selectedFile.getOriginalPath());
                    if(openedFile != null) {
                        onFileOpened(openedFile.getLocalFileObject());
                    }
                    else {
                        isOpeningFile = true;
                        // show opening dialog
                        openProgressDialog = new ProgressDialog(MemberDetailsActivity.this);
                        openProgressDialog.setIndeterminate(true);
                        openProgressDialog.setMessage("Opening...");
                        openProgressDialog.setCancelable(false);
                        openProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MemberDetailsActivity.this.onFileOpened(null);
                                    }
                                });
                        openProgressDialog.show();

                        // start a open file task
                        openFileTask = new AsyncTask<Void, Void, File>() {
                            @Override
                            protected File doInBackground(Void... params) {
                                Log.i(LOG_TAG, "Starting Open File Task.");
                                MobileFile openedFile = filesystem.openFile(
                                        selectedFile.getOriginalPath());
                                if(openedFile == null) {
                                    return null;
                                } else {
                                    return openedFile.getLocalFileObject();
                                }
                            }

                            @Override
                            protected void onPostExecute(File file) {
                                MemberDetailsActivity.this.onFileOpened(file);
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isOpeningFile) {
            openProgressDialog.dismiss();
        }
        if(isConnecting) {
            connectionProgressDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isOpeningFile) {
            openProgressDialog.dismiss();
        }
        if(isConnecting) {
            connectionProgressDialog.dismiss();
        }
        refreshMemberDetails(false);

    }

    void refreshMemberDetails(Boolean force) {
        Log.i(LOG_TAG, "Displaying Member: " +mMember.getId());
        mSwipeRefreshLayout.setRefreshing(true);
        mFileListAdapter.clear();
        if(mMember.isConnected() && !force) {
            List<MobileFile> fileList = mMember.getBackingFilesystem().ls("/");
            if(fileList != null){
                mFileListAdapter.clear();
                mFileListAdapter.addAll(fileList);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
        else {
            // try to connect to member
            isConnecting = true;
            // show a progress dialog
            connectionProgressDialog = new ProgressDialog(MemberDetailsActivity.this);
            connectionProgressDialog.setIndeterminate(true);
            connectionProgressDialog.setMessage("connecting...");
            connectionProgressDialog.setCancelable(false);
            connectionProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MemberDetailsActivity.this.onConnectComplete(false);
                        }
                    });
            connectionProgressDialog.show();

            // start connect action
            connectTask = new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    Log.i(LOG_TAG, "Starting Connect Task.");
                    return mMember.connect();
                }

                @Override
                protected void onPostExecute(Boolean success) {
                    MemberDetailsActivity.this.onConnectComplete(success);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    void onConnectComplete(boolean success){
        connectionProgressDialog.dismiss();
        if(success) {
            refreshMemberDetails(false);
        }
        else {
            connectTask.cancel(true);
            Snackbar.make(mFileListView, "Connection Failed.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        isConnecting = false;
        mSwipeRefreshLayout.setRefreshing(false);
    }

    void onFileOpened(File file) {
        if(openProgressDialog != null) {
            openProgressDialog.dismiss();
        }
        if(file != null) {
            // start intent to open file
            Log.i(LOG_TAG, "Opening file: " +file.getAbsoluteFile());
            Intent openFileIntent = new Intent();
            openFileIntent.setAction(android.content.Intent.ACTION_VIEW);
            openFileIntent.setDataAndType(Uri.fromFile(file), Utility.getMimeType(file.getAbsolutePath()));
            startActivity(openFileIntent);
        }
        else
        {
            openFileTask.cancel(true);
            Snackbar.make(mFileListView, "Unable to Open File", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        isOpeningFile = false;
    }

}
