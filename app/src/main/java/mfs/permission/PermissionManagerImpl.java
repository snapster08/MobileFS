package mfs.permission;

import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import mfs.filesystem.MobileFile;
import mfs.node.MobileNode;

public class PermissionManagerImpl implements PermissionManager {
    private final static String LOG_TAG = PermissionManagerImpl.class.getSimpleName();

    List<File> sharedFiles = new LinkedList<>();
    HashMap<File, Permission> permissionMap = new HashMap<>();

    public static class Permission {
        boolean isReadable;
        boolean isWritable;
        String lockHolder;

        Permission(boolean isReadable, boolean isWritable, String lockHolder) {
            this.isReadable = isReadable;
            this.isWritable = isWritable;
            this.lockHolder = lockHolder;
        }
    }


    @Override
    public boolean isReadable(MobileFile file, MobileNode n) {
        return false;
    }

    @Override
    public boolean isWritable(MobileFile file, MobileNode n) {
        return false;
    }

    @Override
    public void setReadable(MobileFile file, MobileNode n, boolean permission) {

    }
    @Override
    public void setWritable(MobileFile file, MobileNode n, boolean permission) {

    }

    @Override
    public void setGlobalReadable(MobileFile file, boolean permission) {

    }

    @Override
    public void setGlobalWritable(MobileFile file, boolean permission) {

    }

    @Override
    public void initializeSharedFiles(List<File> files) {
        for(File file : files) {
            // add the files
            if(file.isFile()) {
                sharedFiles.add(file);
                permissionMap.put(file, new Permission(true, true, null));
            }
            else {
                // add the files in the directories
                for(File innerFile : file.listFiles()) {
                    if(innerFile.isFile() && !innerFile.isHidden()) {
                        sharedFiles.add(innerFile);
                        permissionMap.put(innerFile, new Permission(true, true, null));
                    }
                }
            }
        }
    }

    @Override
    public List<File> getSharedFiles() {
        return sharedFiles;
    }

    @Override
    public void clearSharedFiles() {
        sharedFiles.clear();
        permissionMap.clear();
    }

    @Override
    public boolean isLocked(File file) {
        Permission filePermission = permissionMap.get(file);
        if(filePermission == null) {
            // the file is not shared
            return false;
        }
        if(!filePermission.isWritable) {
            // file is not writable so there is no acquireLock
            return false;
        }
        if(filePermission.lockHolder != null) {
            Log.i(LOG_TAG, "File is locked:" +file.getAbsolutePath()
                    +" by " +filePermission.lockHolder);
            return true;
        }
        return false;
    }

    @Override
    public boolean isShared(File file) {
        if(permissionMap.get(file) != null) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean acquireLock(File file, String lockHolderId) {
        if(isLocked(file)) {
            return false;
        }
        else {
            Permission filePermission = permissionMap.get(file);
            if(filePermission == null) {
                return false;
            }
            filePermission.lockHolder = lockHolderId;
            Log.i(LOG_TAG, "Acquired lock on file:" +file.getAbsolutePath() +" by " +lockHolderId);
            return true;
        }
    }

    @Override
    public boolean releaseLock(File file, String lockHolderId) {
        if(!isLocked(file)) {
            Log.i(LOG_TAG, "Try to unlock a file which is not locked: " +file.getAbsolutePath()
                    +" by " +lockHolderId);
            return true;
        }
        else {
            Permission filePermission = permissionMap.get(file);
            if(filePermission == null) {
                return false;
            }
            filePermission.lockHolder = null;
            Log.i(LOG_TAG, "Locked release on file:" +file.getAbsolutePath() +" by " +lockHolderId);
            return true;
        }
    }
}
