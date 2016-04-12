package mfs.node;

import mfs.filesystem.Filesystem;

/**
 * Created by bala on 4/11/16.
 */
public interface MobileNode {

    String getName();

    String getAddress();

    boolean isConnected();

    Filesystem getBackingFilesystem();

    // void kickOutOfGroup();
}
