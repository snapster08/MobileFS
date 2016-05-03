package mfs.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import mfs.node.MobileNode;
import mfs.service.ServiceAccessor;
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
        ViewHolder viewHolder;

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mResourceId,
                    parent, false);

            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else {
            if (convertView.getVisibility() == View.GONE) {
                convertView.setVisibility(View.VISIBLE);
                convertView.setLayoutParams(new AbsListView.LayoutParams(-1,-2));
            }
            viewHolder = (ViewHolder)convertView.getTag();
        }
        MobileNode node = getItem(position);
        // do not display my node in list
        if (node.getId().equals(ServiceAccessor.getMyId())) {
            convertView.setLayoutParams(new AbsListView.LayoutParams(-1,1));
            convertView.setVisibility(View.GONE);
        }
        viewHolder.nameTextView.setText(node.getName());
        viewHolder.statusTextView.setText(node.isConnected()?"Connected":"Disconnected");
        return convertView;
    }
}
