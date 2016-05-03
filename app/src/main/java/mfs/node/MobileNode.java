package mfs.node;

import mfs.filesystem.Filesystem;

public interface MobileNode {

    String getId();

    String getName();

    String getAddress();

    void setName(String name);

    void setAddress(String address);

    boolean isConnected();

    boolean connect();

    Filesystem getBackingFilesystem();

    // void kickOutOfGroup();
}
