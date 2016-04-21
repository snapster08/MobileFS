package mfs.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.util.List;

import mfs.data.DataContract.FilesystemsEntry;
import mfs.data.DataContract.MembersEntry;

public class DataProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DatabaseHelper mOpenHelper;

    // URI types
    static final int MEMBERS = 100;
    static final int MEMBER_WITH_ID = 101;
    static final int FILESYSTEMS = 200;
    static final int FILESYSTEMS_WITH_MEMEBER_ID = 201;
    static final int FILESYSTEMS_WITH_MEMEBER_ID_AND_FILESYSTEM_ROOT = 202;

    static UriMatcher buildUriMatcher() {

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, DataContract.PATH_MEMBERS, MEMBERS);
        matcher.addURI(authority, DataContract.PATH_MEMBERS +"/#", MEMBER_WITH_ID);

        matcher.addURI(authority, DataContract.PATH_FILESYSTEMS, FILESYSTEMS);
        matcher.addURI(authority, DataContract.PATH_FILESYSTEMS +"/#", FILESYSTEMS_WITH_MEMEBER_ID);
        matcher.addURI(authority, DataContract.PATH_FILESYSTEMS +"/#/*",
                FILESYSTEMS_WITH_MEMEBER_ID_AND_FILESYSTEM_ROOT);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MEMBERS:
                return MembersEntry.CONTENT_TYPE;
            case MEMBER_WITH_ID:
                return MembersEntry.CONTENT_ITEM_TYPE;
            case FILESYSTEMS:
                return FilesystemsEntry.CONTENT_TYPE;
            case FILESYSTEMS_WITH_MEMEBER_ID:
                return FilesystemsEntry.CONTENT_TYPE;
            case FILESYSTEMS_WITH_MEMEBER_ID_AND_FILESYSTEM_ROOT:
                return FilesystemsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor returnCursor;
        switch (sUriMatcher.match(uri)){
            case MEMBERS:
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        MembersEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MEMBER_WITH_ID: {
                String memberId = uri.getLastPathSegment();
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        MembersEntry.TABLE_NAME,
                        projection,
                        MembersEntry.COLUMN_MEMBER_ID + " = ?",
                        new String[]{memberId},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case FILESYSTEMS:
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        FilesystemsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case FILESYSTEMS_WITH_MEMEBER_ID: {
                String memberId = uri.getLastPathSegment();
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        FilesystemsEntry.TABLE_NAME,
                        projection,
                        FilesystemsEntry.COLUMN_MEMBER_ID + " = ?",
                        new String[]{memberId},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case FILESYSTEMS_WITH_MEMEBER_ID_AND_FILESYSTEM_ROOT: {
                List<String> uriSegments = uri.getPathSegments();
                String filesystemRoot = uriSegments.get(uriSegments.size()-1); // last segment
                String memberId = uriSegments.get(uriSegments.size()-2); // last but one segment
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        FilesystemsEntry.TABLE_NAME,
                        projection,
                        FilesystemsEntry.COLUMN_MEMBER_ID + " = ?" + " AND "
                        + FilesystemsEntry.COLUMN_FILESYSTEM_ROOT + " = ?",
                        new String[]{memberId, filesystemRoot},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Tells the Cursor what URI to watch, so it knows when its source data changes
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        long _id;
        switch (sUriMatcher.match(uri)) {
            case MEMBERS:
                _id = db.insertWithOnConflict(MembersEntry.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 )
                    returnUri = MembersEntry.buildMemberUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case FILESYSTEMS:
                _id = db.insertWithOnConflict(FilesystemsEntry.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 )
                    returnUri = FilesystemsEntry.buildfilesystemUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long _id;
        int returnCount;
        switch (sUriMatcher.match(uri)) {
            case MEMBERS:
                db.beginTransaction();
                returnCount = 0;
                for (ContentValues value : values){
                    _id = db.insertWithOnConflict(MembersEntry.TABLE_NAME, null,
                            value, SQLiteDatabase.CONFLICT_REPLACE);
                    if ( _id != -1) {
                        returnCount++;
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                break;
            case FILESYSTEMS:
                db.beginTransaction();
                returnCount = 0;
                for (ContentValues value : values){
                    _id = db.insertWithOnConflict(FilesystemsEntry.TABLE_NAME, null,
                            value, SQLiteDatabase.CONFLICT_REPLACE);
                    if ( _id != -1) {
                        returnCount++;
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                break;
            default:
                return super.bulkInsert(uri, values);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;
        switch (sUriMatcher.match(uri)) {
            case MEMBERS:
                rowsDeleted = db.delete(
                        MembersEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MEMBER_WITH_ID: {
                String memberId = uri.getLastPathSegment();
                rowsDeleted = db.delete(
                        MembersEntry.TABLE_NAME, MembersEntry.COLUMN_MEMBER_ID + " = ?",
                        new String[]{memberId});
                break;
            }
            case FILESYSTEMS:
                rowsDeleted = db.delete(
                        FilesystemsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FILESYSTEMS_WITH_MEMEBER_ID: {
                String memberId = uri.getLastPathSegment();
                rowsDeleted = db.delete(
                        MembersEntry.TABLE_NAME,
                        FilesystemsEntry.COLUMN_MEMBER_ID +" = ?",
                        new String[]{memberId});
                break;
            }
            case FILESYSTEMS_WITH_MEMEBER_ID_AND_FILESYSTEM_ROOT: {
                List<String> uriSegments = uri.getPathSegments();
                String filesystemRoot = uriSegments.get(uriSegments.size()-1); // last segment
                String memberId = uriSegments.get(uriSegments.size()-2); // last but one segment
                rowsDeleted = db.delete(
                        MembersEntry.TABLE_NAME,
                        FilesystemsEntry.COLUMN_MEMBER_ID + " = ?" + " AND "
                                + FilesystemsEntry.COLUMN_FILESYSTEM_ROOT + " = ?",
                        new String[]{memberId, filesystemRoot});
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;
        switch (sUriMatcher.match(uri)) {
            case MEMBERS:
                rowsUpdated = db.update(
                        MembersEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MEMBER_WITH_ID: {
                String memberId = uri.getLastPathSegment();
                rowsUpdated = db.update(
                        MembersEntry.TABLE_NAME, values, MembersEntry.COLUMN_MEMBER_ID + " = ?",
                        new String[]{memberId});
                break;
            }
            case FILESYSTEMS:
                rowsUpdated = db.update(
                        FilesystemsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case FILESYSTEMS_WITH_MEMEBER_ID: {
                String memberId = uri.getLastPathSegment();
                rowsUpdated = db.update(
                        MembersEntry.TABLE_NAME,
                        values,
                        FilesystemsEntry.COLUMN_MEMBER_ID +" = ?",
                        new String[]{memberId});
                break;
            }
            case FILESYSTEMS_WITH_MEMEBER_ID_AND_FILESYSTEM_ROOT: {
                List<String> uriSegments = uri.getPathSegments();
                String filesystemRoot = uriSegments.get(uriSegments.size()-1); // last segment
                String memberId = uriSegments.get(uriSegments.size()-2); // last but one segment
                rowsUpdated = db.update(
                        MembersEntry.TABLE_NAME,
                        values,
                        FilesystemsEntry.COLUMN_MEMBER_ID + " = ?" + " AND "
                                + FilesystemsEntry.COLUMN_FILESYSTEM_ROOT + " = ?",
                        new String[]{memberId, filesystemRoot});
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
