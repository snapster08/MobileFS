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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;

import mobilefs.seminar.pdfs.service.R;


public class NameAndFilesActivity extends AppCompatActivity {
    public final static String LOG_TAG = NameAndFilesActivity.class.getSimpleName();
    int actionType;
    Button nextButton;
    EditText nameEditText;
    Button pickFilesButton;
    String mSelectedFile = Environment.getExternalStorageDirectory().getAbsolutePath();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_and_files);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        actionType = getIntent().getIntExtra(Constants.TAG_ACTION_TYPE, Constants.ACTION_NOTHING);
        switch (actionType){
            case Constants.ACTION_CREATE_GROUP:
                toolbar.setTitle(R.string.title_activity_create_group);
                break;
            case Constants.ACTION_JOIN_GROUP:
                toolbar.setTitle(R.string.title_activity_join_group);
                break;
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nextButton = (Button) findViewById(R.id.button_name_next);
        nameEditText = (EditText) findViewById(R.id.editText_enterName);
        pickFilesButton = (Button) findViewById(R.id.button_pickFiles);

        // set up the next button
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameEditText.getText().toString().equals("")) {
                    Snackbar.make(nextButton, "Please enter a name.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
                switch (actionType) {
                    case Constants.ACTION_CREATE_GROUP: {
                        Intent intent = new Intent(NameAndFilesActivity.this, CreateGroupActivity.class);
                        intent.putExtra(Constants.TAG_NAME, nameEditText.getText().toString());
                        intent.putExtra(Constants.TAG_SELECTED_FILE, mSelectedFile);
                        startActivity(intent);
                        break;
                    }
                    case Constants.ACTION_JOIN_GROUP: {
                        Intent intent = new Intent(NameAndFilesActivity.this, JoinGroupActivity.class);
                        intent.putExtra(Constants.TAG_NAME, nameEditText.getText().toString());
                        intent.putExtra(Constants.TAG_SELECTED_FILE, mSelectedFile);
                        startActivity(intent);
                        break;
                    }
                }
            }
        });

        pickFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This always works
                Intent i = new Intent(NameAndFilesActivity.this, FilePickerActivity.class);

                // Set these depending on your use case. These are the defaults.
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
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
                // removes the "file://" part int the uri
                mSelectedFile = uri.toString().substring(7);
            }
        }
    }

}
