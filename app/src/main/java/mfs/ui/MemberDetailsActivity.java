package mfs.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import mfs.node.MobileNode;
import mfs.service.ServiceAccessor;
import mobilefs.seminar.pdfs.service.R;

public class MemberDetailsActivity extends AppCompatActivity {

    ListView mFileListVIew;
    FileListAdapter mFileListAdapter;
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
                // handle click on file, update adapter with subdirectories if its a directory
                // else open the file
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    void refreshMemberDetails() {
        mFileListAdapter.clear();
        mMember.getBackingFilesystem().ls("/");
    }

}
