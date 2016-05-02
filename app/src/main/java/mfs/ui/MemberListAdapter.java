package mfs.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import mfs.node.MobileNode;
import mobilefs.seminar.pdfs.service.R;

public class MemberListAdapter extends ArrayAdapter<MobileNode> {
    static final String LOG_TAG = MemberListAdapter.class.getSimpleName();

    int mResourceId;
    public MemberListAdapter(Context context, int resourceId) {
        super(context, 0, new ArrayList<MobileNode>());
        mResourceId = resourceId;
    }

    /**
     * Cache of the children views for a member item.
     */
    public static class ViewHolder {
        public final TextView nameTextView;
        public final TextView statusTextView;

        public ViewHolder(View view) {
            nameTextView = (TextView) view.findViewById(R.id.member_item_name_textView);
            statusTextView = (TextView) view.findViewById(R.id.member_item_status_textView);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;

        if(view == null) {
            view = LayoutInflater.from(getContext()).inflate(mResourceId,
                    parent, false);

            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)view.getTag();
        }
        viewHolder.nameTextView.setText(getItem(position).getName());
        viewHolder.statusTextView.setText(getItem(position).isConnected()?"Connected":"Disconnected");
        return view;
    }
}
