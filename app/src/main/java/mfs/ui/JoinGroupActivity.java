package mfs.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;

import mfs.node.NodeManager;
import mfs.service.ServiceAccessor;
import mobilefs.seminar.pdfs.service.R;

public class JoinGroupActivity extends AppCompatActivity {

    public final static String LOG_TAG = CreateGroupActivity.class.getSimpleName();

    EditText groupLinkEditText;
    Button joinButton;
    Button pickFilesButton;
    NodeManager mNodeManager = ServiceAccessor.getNodeManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupLinkEditText = (EditText) findViewById(R.id.editText_groupLink);
        joinButton = (Button) findViewById(R.id.button_joinGroup);
        pickFilesButton = (Button) findViewById(R.id.button_pickFiles);

        // set up the join Button
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // join group code goes here.......
                if(mNodeManager.joinGroup(groupLinkEditText.getText().toString())) {
                    // if successful go back to the make screen
                    Intent upIntent = NavUtils.getParentActivityIntent(JoinGroupActivity.this);
                    // if the parent activity needs to be recreated, create it
                    // otherwise bring back the already existing activity
                    if (NavUtils.shouldUpRecreateTask(JoinGroupActivity.this, upIntent)) {
                        TaskStackBuilder.create(JoinGroupActivity.this)
                                .addNextIntentWithParentStack(upIntent).startActivities();
                    }
                    upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(upIntent);
                    Snackbar.make(joinButton, "Joined Group.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else
                {
                    Snackbar.make(joinButton, "Join Failed.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

            }
        });

        pickFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This always works
                Intent i = new Intent(JoinGroupActivity.this, FilePickerActivity.class);

                // Set these depending on your use case. These are the defaults.
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE_AND_DIR);

                // Configure initial directory by specifying a String.
                i.putExtra(FilePickerActivity.EXTRA_START_PATH,
                        Environment.getExternalStorageDirectory().getPath());

                startActivityForResult(i, 0);
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // For JellyBean and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();
                    Log.i(LOG_TAG, "Selected Files: ");
                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            Log.i(LOG_TAG, uri.toString());
                            // Do something with the URI
                        }
                    }
                    // For Ice Cream Sandwich
                } else {
                    Log.i(LOG_TAG, "Selected Files: ");
                    ArrayList<String> paths = data.getStringArrayListExtra
                            (FilePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        for (String path: paths) {
                            Uri uri = Uri.parse(path);
                            Log.i(LOG_TAG, uri.toString());
                            // Do something with the URI
                        }
                    }
                }

            } else {
                Uri uri = data.getData();
                Log.i(LOG_TAG, uri.toString());
                // Do something with the URI
            }
        }
    }

}
