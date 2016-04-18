package mfs.ui;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import mobilefs.seminar.pdfs.service.R;

public class JoinGroupActivity extends AppCompatActivity {

    Button joinButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set up the join Button
        joinButton = (Button) findViewById(R.id.button_joinGroup);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // join group code goes here.......


                //temp
                Snackbar.make(joinButton, getString(R.string.label_notImplemented), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });
    }

}
