package mfs.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import mfs.filesystem.MobileFile;
import mfs.node.MobileNode;
import mobilefs.seminar.pdfs.service.R;

public class OpenFileListAdapter extends ArrayAdapter<MobileFile> {
    static final String LOG_TAG = OpenFileListAdapter.class.getSimpleName();

    int mResourceId;
    Context mContext;
    public OpenFileListAdapter(Context context, int resourceId) {
        super(context, 0, new ArrayList<MobileFile>());
        mContext = context;
        mResourceId = resourceId;
    }

    public static class ViewHolder {
        public final TextView nameTextView;
        public final TextView ownerTextView;
        public final Button commitButton;
        public final Button closeButton;

        public ViewHolder(View view) {
            nameTextView = (TextView) view.findViewById(R.id.open_file_item_name_textView);
            ownerTextView = (TextView) view.findViewById(R.id.open_file_item_owner_textView);
            commitButton = (Button) view.findViewById(R.id.open_file_item_commit_button);
            closeButton = (Button) view.findViewById(R.id.open_file_item_close_button);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final ViewHolder viewHolder;

        if(view == null) {
            view = LayoutInflater.from(getContext()).inflate(mResourceId, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)view.getTag();
        }
        final MobileFile mFile = getItem(position);
        MobileNode node =  mFile.getOwningFilesystem().getOwningNode();
        File file = new File(mFile.getOriginalPath());
        viewHolder.nameTextView.setText(file.getName());
        viewHolder.ownerTextView.setText(node.getName());
        viewHolder.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OpenFilesActivity)mContext).onFileClosed(mFile);
            }
        });

        viewHolder.commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OpenFilesActivity)mContext).onFileCommitted(mFile);
            }
        });

        return view;
    }
}
