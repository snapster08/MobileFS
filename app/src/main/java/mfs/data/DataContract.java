package mfs.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by barry on 4/19/16.
 */
public class DataContract {

    public static final String CONTENT_AUTHORITY = "mfs.data";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // path to access corresponding data
    public static final String PATH_MEMBERS = "members";
    public static final String PATH_FILESYSTEMS = "filesystems";

    // defines the members table
    public static final class MembersEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MEMBERS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEMBERS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEMBERS;

        // Table name
        public static final String TABLE_NAME = "members";

        //columns
        public static final String COLUMN_MEMBER_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_FILESYSTEM = "filesystem";

        public static Uri buildMemberUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    // defines the filesystems table
    public static final class FilesystemsEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FILESYSTEMS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FILESYSTEMS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FILESYSTEMS;

        // Table name
        public static final String TABLE_NAME = "filesystems";

        //columns
        public static final String COLUMN_ID = "_id";  // required to use cursor adaptor
        public static final String COLUMN_MEMBER_ID = "member_id";
        public static final String COLUMN_FILESYSTEM_ROOT = "filesystem_root";
        public static final String COLUMN_FILESYSTEM_SRUCTURE = "filesystem_root_structure";

        public static Uri buildfilesystemUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
