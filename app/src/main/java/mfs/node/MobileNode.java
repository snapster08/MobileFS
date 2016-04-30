package mfs.node;

import mfs.filesystem.Filesystem;

public interface MobileNode {

    String getId();

    String getName();

    String getAddress();

    boolean isConnected();

    Filesystem getBackingFilesystem();

    // void kickOutOfGroup();
}
