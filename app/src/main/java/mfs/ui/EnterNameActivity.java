package mfs.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import mobilefs.seminar.pdfs.service.R;


public class EnterNameActivity extends AppCompatActivity {

    int actionType;
    Button nextButton;
    EditText nameEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_name);
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

        // set up the next button
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (actionType) {
                    case Constants.ACTION_CREATE_GROUP: {
                        Intent intent = new Intent(EnterNameActivity.this, CreateGroupActivity.class);
                        intent.putExtra(Constants.TAG_NAME, nameEditText.getText().toString());
                        startActivity(intent);
                        break;
                    }
                    case Constants.ACTION_JOIN_GROUP: {
                        Intent intent = new Intent(EnterNameActivity.this, JoinGroupActivity.class);
                        intent.putExtra(Constants.TAG_NAME, nameEditText.getText().toString());
                        startActivity(intent);
                        break;
                    }
                }
            }
        });
    }

}
