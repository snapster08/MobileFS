package mfs.permission;

import java.io.File;
import java.util.List;

import mfs.filesystem.MobileFile;
import mfs.node.MobileNode;

public interface PermissionManager {

    boolean isReadable(MobileFile file, MobileNode n);

    boolean isWritable(MobileFile file, MobileNode n);

    void setReadable(MobileFile file, MobileNode n, boolean permission);

    void setWritable(MobileFile file, MobileNode n, boolean permission);

    void setGlobalReadable(MobileFile file, boolean permission);

    void setGlobalWritable(MobileFile file, boolean permission);

    boolean isLocked(File file);

    void initializeSharedFiles(List<File> files);

    List<File> getSharedFiles();

    void clearSharedFiles();

    boolean isShared(File file);

    boolean acquireLock(File file, String lockHolderId);

    boolean releaseLock(File file, String lockHolderId);

}
