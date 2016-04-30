package mfs.node;

import mfs.filesystem.Filesystem;

public class MobileNodeImpl implements MobileNode {
    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getAddress() {
        return null;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public Filesystem getBackingFilesystem() {
        return null;
    }
}
