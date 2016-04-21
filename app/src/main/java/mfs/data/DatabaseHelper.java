package mfs.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import mfs.data.DataContract.MembersEntry;
import mfs.data.DataContract.FilesystemsEntry;

public class DatabaseHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "members.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create tables for members
        final String SQL_CREATE_MEMBERS_TABLE = "CREATE TABLE " + MembersEntry.TABLE_NAME + " (" +
                MembersEntry.COLUMN_MEMBER_ID + " INTEGER PRIMARY KEY, " +
                MembersEntry.COLUMN_NAME + " TEXT, " +
                MembersEntry.COLUMN_ADDRESS + " TEXT, " +
                MembersEntry.COLUMN_FILESYSTEM + " TEXT " +
                " );";


        // Create tables for  the filesystems
        final String SQL_CREATE_FILESYSTEMS_TABLE = "CREATE TABLE " + FilesystemsEntry.TABLE_NAME + " (" +
                FilesystemsEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                FilesystemsEntry.COLUMN_MEMBER_ID + " INTEGER, " +
                FilesystemsEntry.COLUMN_FILESYSTEM_ROOT + " TEXT, " +
                FilesystemsEntry.COLUMN_FILESYSTEM_SRUCTURE + " TEXT, " +
                "UNIQUE(" + FilesystemsEntry.COLUMN_MEMBER_ID + ", " +
                FilesystemsEntry.COLUMN_FILESYSTEM_ROOT + "), " +
                " FOREIGN KEY(" + FilesystemsEntry.COLUMN_MEMBER_ID + ") REFERENCES " +
                MembersEntry.TABLE_NAME + "(" + MembersEntry.COLUMN_MEMBER_ID + ")" +
                " );";

        db.execSQL(SQL_CREATE_FILESYSTEMS_TABLE);
        db.execSQL(SQL_CREATE_MEMBERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // discarding old data for now, will need to upgrade based on the change in DB scheme
        db.execSQL("DROP TABLE IF EXISTS " + MembersEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FilesystemsEntry.TABLE_NAME);
        onCreate(db);
    }
}
