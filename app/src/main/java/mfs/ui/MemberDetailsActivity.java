package mfs.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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
    AsyncTask connectTask;
    boolean isConnecting;
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
                MobileFile selectedFile = mFileListAdapter.getItem(position);
                Filesystem filesystem = mMember.getBackingFilesystem();
                if(selectedFile.getType() == MobileFileImpl.Type.directory) {
                    Snackbar.make(mFileListVIew, "Cannot open directories.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else {
                    if(filesystem.isOpen(selectedFile)) {
                        MobileFile openedFile = filesystem.openFile(selectedFile.getOriginalPath(), mMember);
                        File localFile = openedFile.getLocalFileObject();

                    }
                    else {
                        // TODO create a open file tak
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        if(success) {
            connectionProgressDialog.dismiss();
            refreshMemberDetails();
        }
        else {
            connectionProgressDialog.dismiss();
            connectTask.cancel(true);
            Snackbar.make(mFileListVIew, "Connection Failed.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        isConnecting = false;
    }
}
