package mfs.ui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import mfs.node.NodeManager;
import mfs.service.ServiceAccessor;
import mfs.ui.Constants;
import mobilefs.seminar.pdfs.service.R;


public class CreateGroupActivity extends AppCompatActivity {

    public final static String LOG_TAG = CreateGroupActivity.class.getSimpleName();

    Button copyButton;
    Button homeButton;
    TextView mCreateInfoTextView;
    TextView mFileInfoTextView;
    String mUserName;
    String mSelectedFile;
    TextView mCreateStatusTextView;
    RelativeLayout mCreateInfoLayout;
    NodeManager mNodeManager = ServiceAccessor.getNodeManager();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mUserName = getIntent().getStringExtra(Constants.TAG_NAME);
        mSelectedFile = getIntent().getStringExtra(Constants.TAG_SELECTED_FILE);
        // set the shared file
        ServiceAccessor.getNodeManager().setSharedFile(mSelectedFile);
        List<File> selectedFileList = new LinkedList<File>();
        selectedFileList.add(new File(mSelectedFile));
        ServiceAccessor.getPermissionManager().initializeSharedFiles(selectedFileList);

        mCreateInfoTextView = (TextView) findViewById(R.id.textView_createInfo);
        mCreateStatusTextView = (TextView) findViewById(R.id.textView_creatGroupStatus);
        mFileInfoTextView = (TextView) findViewById(R.id.textViewFileInfo);
        mFileInfoTextView.setText("Sharing file: " +mSelectedFile);
        mCreateInfoLayout = (RelativeLayout) findViewById(R.id.layout_groupInfo);
        copyButton = (Button) findViewById(R.id.button_copyGroupInfo);
        homeButton = (Button) findViewById(R.id.button_homeFromCreateGroup);

        // set up the copy button
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // copy the group info to the clipboard to send to others
                // Gets a handle to the clipboard service.
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Group Link", mCreateInfoTextView.getText().toString());
                // Set the clipboard's primary clip.
                clipboard.setPrimaryClip(clip);

                // show a snackbar
                Snackbar.make(copyButton, "Copied to clipboard.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // set up the home button
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go back to the main screen
                Intent upIntent = new Intent(CreateGroupActivity.this, HomeActivity.class);
                upIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(upIntent);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mNodeManager.createGroup(mUserName)) {
            mCreateInfoLayout.setVisibility(View.VISIBLE);
            mCreateStatusTextView.setText(R.string.label_createGroupSuccess);
            mCreateInfoTextView.setText(mNodeManager.generateJoiningLink());
        }
        else {
            mCreateInfoLayout.setVisibility(View.INVISIBLE);
            mCreateStatusTextView.setText(R.string.label_createGroupFailure);
        }
    }
}
