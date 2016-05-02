package mfs.filesystem;

import java.io.File;

import mfs.service.ServiceAccessor;

public class MobileFileImpl implements MobileFile {
    Filesystem owningFilesystem;
    String originalPath;
    String localFileName;
    int type;

    public MobileFileImpl(Filesystem owningFilesystem, String originalPath, int type) {
        this.owningFilesystem = owningFilesystem;
        this.originalPath = originalPath;
        this.type = type;
    }

    public static class Type {
        public static final int file = 0;
        public static final int directory = 1;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public Filesystem getOwningFilesystem() {
        return owningFilesystem;
    }

    @Override
    public String getLocalFileName() {
        return localFileName;
    }

    @Override
    public void setLocalFileName(String localFileName) {
        this.localFileName = localFileName;
    }

    @Override
    public String getOriginalPath() {
        return originalPath;
    }

    @Override
    public File getLocalFileObject() {
        if(localFileName != null) {
            return new File(ServiceAccessor.getCacheDirectory(), localFileName);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MobileFile &&
                this.owningFilesystem.equals(((MobileFile) o).getOwningFilesystem()) &&
                this.getOriginalPath().equals(((MobileFile) o).getOriginalPath());
    }
}
