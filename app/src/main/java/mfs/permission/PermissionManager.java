package mfs.permission;

import java.io.File;
import java.util.List;

import mfs.filesystem.MobileFile;
import mfs.node.MobileNode;

public interface PermissionManager {

    boolean isReadable(MobileFile file, MobileNode n);

    boolean isWritable(MobileFile file, MobileNode n);

    void setReadable(MobileFile file, MobileNode n, boolean permission);

    void setWriteable(MobileFile file, MobileNode n, boolean permission);

    void setGlobalReadable(MobileFile file, boolean permission);

    void setGlobalWriteable(MobileFile file, boolean permission);

    void initializeSharedFiles(List<File> files);


}
