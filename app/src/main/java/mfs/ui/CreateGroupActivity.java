package mfs.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import mobilefs.seminar.pdfs.service.R;


public class CreateGroupActivity extends AppCompatActivity {

    Button copyButton;
    TextView createInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        createInfoTextView = (TextView) findViewById(R.id.textView_createInfo);

        // set up the copy button
        copyButton = (Button) findViewById(R.id.button_copyGroupInfo);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // copy the group info to the clipboard to send to others
                // Gets a handle to the clipboard service.
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Group Link", createInfoTextView.getText());
                // Set the clipboard's primary clip.
                clipboard.setPrimaryClip(clip);

                // show a snackbar
                Snackbar.make(copyButton, "Copied to clipboard.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
