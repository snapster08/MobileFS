package mfs.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import mfs.data.DataContract;
import mobilefs.seminar.pdfs.service.R;

public class MemberListAdapter extends CursorAdapter {
    static final String LOG_TAG = MemberListAdapter.class.getSimpleName();

    public MemberListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
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
    /**
     * Makes a new view to hold the data pointed to by cursor.
     *
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.member_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    /**
     * Bind an existing view to the data pointed to by cursor
     *
     * @param view    Existing view, returned earlier by newView
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.nameTextView.setText(
                cursor.getString(
                        cursor.getColumnIndex(
                                DataContract.MembersEntry.COLUMN_NAME)));

        // statusTextView is default for now
    }
}
