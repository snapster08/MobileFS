package mfs.permission;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import mfs.filesystem.MobileFile;
import mfs.node.MobileNode;

public class PermissionManagerImpl implements PermissionManager {

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
    public void setWriteable(MobileFile file, MobileNode n, boolean permission) {

    }

    @Override
    public void setGlobalReadable(MobileFile file, boolean permission) {

    }

    @Override
    public void setGlobalWriteable(MobileFile file, boolean permission) {

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
                    if(file.isFile()) {
                        sharedFiles.add(file);
                        permissionMap.put(file, new Permission(true, true, null));
                    }
                }
            }
        }
    }
}
