package mfs.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import mfs.filesystem.MobileFile;
import mfs.service.ServiceAccessor;
import mobilefs.seminar.pdfs.service.R;

public class OpenFilesActivity extends AppCompatActivity {
    public final static String LOG_TAG = OpenFilesActivity.class.getSimpleName();

    ListView mOpenFileListView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    OpenFileListAdapter mOpenFileListAdapter;
    ProgressDialog commitProgressDialog;
    AsyncTask commitTask;
    boolean isCommitting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_files);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mOpenFileListView = (ListView) findViewById(R.id.list_openFiles);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.list_openFiles_SwipeRefresh);

        // create a adapter for the file list
        mOpenFileListAdapter = new OpenFileListAdapter(this, R.layout.open_file_list_item);
        mOpenFileListView.setAdapter(mOpenFileListAdapter);

        // initialize the SwipeRefresh
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshOpenFileList();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isCommitting) {
            commitProgressDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isCommitting) {
            commitProgressDialog.show();
        }
        refreshOpenFileList();
    }

    void refreshOpenFileList() {
        mSwipeRefreshLayout.setRefreshing(true);
        mOpenFileListAdapter.clear();
        mOpenFileListAdapter.addAll(ServiceAccessor.getNodeManager().getAllOpenFiles());
        mSwipeRefreshLayout.setRefreshing(false);
    }

    void onFileClosed(MobileFile file) {
        if(file.getOwningFilesystem().closeFile(file)) {
            mOpenFileListAdapter.remove(file);
            mOpenFileListAdapter.notifyDataSetChanged();
            Snackbar.make(mOpenFileListView, "File Closed.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        else {
            Snackbar.make(mOpenFileListView, "Unable to close file.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }

    void onFileCommitted(final MobileFile file) {

        isCommitting = true;
        // show a progress dialog
        commitProgressDialog = new ProgressDialog(OpenFilesActivity.this);
        commitProgressDialog.setIndeterminate(true);
        commitProgressDialog.setMessage("Committing...");
        commitProgressDialog.setCancelable(false);
        commitProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OpenFilesActivity.this.onCommitComplete(false);
                    }
                });
        commitProgressDialog.show();

        // start connect action
        commitTask = new AsyncTask<Void, Void, MobileFile>() {
            @Override
            protected MobileFile doInBackground(Void... params) {
                Log.i(LOG_TAG, "Starting Commit Task.");
                if(file.getOwningFilesystem().commitFile(file)) {
                    return file;
                }
                else {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(MobileFile file) {
                if(file == null) {
                    OpenFilesActivity.this.onCommitComplete(false);
                }
                else {
                    mOpenFileListAdapter.remove(file);
                    OpenFilesActivity.this.onCommitComplete(true);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    void onCommitComplete(boolean success) {
        commitProgressDialog.dismiss();
        if(success) {
            mOpenFileListAdapter.notifyDataSetChanged();
            Snackbar.make(mOpenFileListView, "File committed.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        else {
            commitTask.cancel(true);
            Snackbar.make(mOpenFileListView, "Commit Failed.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        isCommitting = false;
    }

}
