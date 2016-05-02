package mfs.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.List;

import mfs.filesystem.Filesystem;
import mfs.filesystem.MobileFile;
import mfs.filesystem.MobileFileImpl;
import mfs.node.MobileNode;
import mfs.service.ServiceAccessor;
import mobilefs.seminar.pdfs.service.R;

public class MemberDetailsActivity extends AppCompatActivity {
    public final static String LOG_TAG = MemberDetailsActivity.class.getSimpleName();

    ListView mFileListVIew;
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
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMember = ServiceAccessor.getNodeManager().getNode(
                getIntent().getStringExtra(Constants.TAG_MEMBER_ID));

        mFileListVIew = (ListView) findViewById(R.id.list_files);

        // create a adapter for the file list
        mFileListAdapter = new FileListAdapter(this, R.layout.file_list_item);
        mFileListVIew.setAdapter(mFileListAdapter);

        // set up onClick on the file list
        mFileListVIew.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // open if it is a file
                final MobileFile selectedFile = mFileListAdapter.getItem(position);
                final Filesystem filesystem = mMember.getBackingFilesystem();
                if(selectedFile.getType() == MobileFileImpl.Type.directory) {
                    Snackbar.make(mFileListVIew, "Cannot open directories.", Snackbar.LENGTH_LONG)
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
                        }.execute();
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
        refreshMemberDetails();

    }

    void refreshMemberDetails() {
        Log.i(LOG_TAG, "Displaying Member: " +mMember.getId());
        mFileListAdapter.clear();
        if(mMember.isConnected()) {
            List<MobileFile> fileList = mMember.getBackingFilesystem().ls("/");
            if(fileList != null){
                mFileListAdapter.addAll(fileList);
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
            }.execute();
        }
        //try again if connected
        if(mMember.isConnected()) {
            List<MobileFile> fileList = mMember.getBackingFilesystem().ls("/");
            if(fileList != null){
                mFileListAdapter.addAll(fileList);
            }
        }
    }

    void onConnectComplete(boolean success){
        connectionProgressDialog.dismiss();
        isConnecting = false;
        if(success) {
            refreshMemberDetails();
        }
        else {
            connectTask.cancel(true);
            Snackbar.make(mFileListVIew, "Connection Failed.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    void onFileOpened(File file) {
        if(openProgressDialog != null) {
            openProgressDialog.dismiss();
        }
        isOpeningFile = false;
        if(file != null) {
            // start intent to open file
            Log.i(LOG_TAG, "Opening file: " +file.getAbsoluteFile());
            Intent openFileIntent = new Intent();
            openFileIntent.setAction(android.content.Intent.ACTION_VIEW);
            openFileIntent.setDataAndType(Uri.fromFile(file),getMimeType(file.getAbsolutePath()));
            startActivity(openFileIntent);
        }
        else
        {
            openFileTask.cancel(true);
            Snackbar.make(mFileListVIew, "Unable to Open File", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private String getMimeType(String url)
    {
        String parts[]=url.split("\\.");
        String extension=parts[parts.length-1];
        String type = null;
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
