package mfs.ui;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import mobilefs.seminar.pdfs.service.R;

public class MemberDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks {

    ListView mFileListVIew;
    FileListAdapter mFileListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memeber_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFileListVIew = (ListView) findViewById(R.id.list_files);

        // initialize a loader for the fileListView
        getLoaderManager().initLoader(Constants.LOADER_FILE_LIST, null, this);

        // create a adapter for the file list
        mFileListAdapter = new FileListAdapter(this, R.layout.file_list_item, null);
        mFileListVIew.setAdapter(mFileListAdapter);

        // set up onClick on the fileList
        mFileListVIew.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // handle click on file, update adapter with subdirectories if its a directory
                // else open the file


            }
        });
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {

    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader loader) {

    }
}
