package mfs.node;

import mfs.filesystem.Filesystem;

public class MobileNodeImpl implements MobileNode {
    private String id;
    private String name;
    private String address;
    private Filesystem backingFilesystem;

    MobileNodeImpl(String id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public Filesystem getBackingFilesystem() {
        return backingFilesystem;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MobileNode && ((MobileNode)o).getId() == this.getId();
    }
}
