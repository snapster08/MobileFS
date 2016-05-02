package mfs.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import mfs.filesystem.MobileFile;
import mfs.filesystem.MobileFileImpl;
import mobilefs.seminar.pdfs.service.R;

public class FileListAdapter extends ArrayAdapter<MobileFile> {

    static final String LOG_TAG = FileListAdapter.class.getSimpleName();

    int mResourceId;
    public FileListAdapter(Context context, int resourceId) {
        super(context, 0, new ArrayList<MobileFile>());
        mResourceId = resourceId;
    }

    /**
     * Cache of the children views for a member item.
     */
    public static class ViewHolder {
        public final TextView nameTextView;
        public final TextView typeTextView;

        public ViewHolder(View view) {
            nameTextView = (TextView) view.findViewById(R.id.file_item_name_textView);
            typeTextView = (TextView) view.findViewById(R.id.file_item_type_textView);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param position
     * @param convertView
     * @param parent
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;

        if(view == null) {
            view = LayoutInflater.from(getContext()).inflate(mResourceId, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)view.getTag();
        }
        MobileFile mFile = getItem(position);
        File file = new File(mFile.getOriginalPath());
        viewHolder.nameTextView.setText(file.getName());
        switch (mFile.getType()) {
            case MobileFileImpl.Type.file:
                viewHolder.typeTextView.setText("file");
            case MobileFileImpl.Type.directory:
                viewHolder.typeTextView.setText("directory");
            default:
                viewHolder.typeTextView.setText("unknown");
        }
        return view;
    }
}
