package mfs.ui.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import mfs.service.ServiceAccessor;
import mfs.ui.Constants;
import mobilefs.seminar.pdfs.service.R;

public class JoinGroupActivity extends AppCompatActivity {

    public final static String LOG_TAG = CreateGroupActivity.class.getSimpleName();

    EditText groupLinkEditText;
    Button joinButton;
    ProgressDialog joinProgressDialog;
    boolean isJoining;
    AsyncTask joinTask;
    String mUserName;
    String mSelectedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mUserName = getIntent().getStringExtra(Constants.TAG_NAME);
        mSelectedFile = getIntent().getStringExtra(Constants.TAG_SELECTED_FILE);

        groupLinkEditText = (EditText) findViewById(R.id.editText_groupLink);
        joinButton = (Button) findViewById(R.id.button_joinGroup);

        // set up the join Button
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isJoining = true;
                // show a progress dialog
                joinProgressDialog = new ProgressDialog(JoinGroupActivity.this);
                joinProgressDialog.setIndeterminate(true);
                joinProgressDialog.setMessage("Joining...");
                joinProgressDialog.setCancelable(false);
                joinProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JoinGroupActivity.this.onJoinComplete(false);
                    }
                });
                joinProgressDialog.show();

                // set the shared file
                List<File> selectedFileList = new LinkedList<File>();
                selectedFileList.add(new File(mSelectedFile));
                ServiceAccessor.getPermissionManager().clearSharedFiles();
                ServiceAccessor.getPermissionManager().initializeSharedFiles(selectedFileList);

                // start join action
                final String groupLink = groupLinkEditText.getText().toString();
                joinTask = new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... params) {
                        Log.i(LOG_TAG, "Starting Join Task.");
                        // update the permission manager with the files shared
                        return ServiceAccessor.getNodeManager().joinGroup(groupLink, mUserName);
                    }

                    @Override
                    protected void onPostExecute(Boolean success) {
                        JoinGroupActivity.this.onJoinComplete(success);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isJoining) {
            joinProgressDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isJoining) {
            joinProgressDialog.show();
        }
    }

    void onJoinComplete(Boolean success) {
        if(success) {
            joinProgressDialog.dismiss();
            // go back to the main screen
            Intent upIntent = new Intent(JoinGroupActivity.this, HomeActivity.class);
            upIntent.putExtra(Constants.TAG_ACTION_TYPE, Constants.ACTION_JOIN_GROUP_DONE);
            upIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(upIntent);
        }
        else
        {
            joinProgressDialog.dismiss();
            joinTask.cancel(true);
            Snackbar.make(joinButton, "Join Failed.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        isJoining = false;
    }

}
