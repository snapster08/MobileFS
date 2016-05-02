package mfs.filesystem;

import java.io.File;

import mfs.service.ServiceAccessor;

public class MobileFileImpl implements MobileFile {
    Filesystem owningFilesystem;
    String originalPath;
    boolean isCached;
    String cachedFileName;
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

    public String getCachedFileName() {
        return cachedFileName;
    }

    public void setCachedFileName(String cachedFileName) {
        this.cachedFileName = cachedFileName;
    }

    public boolean isCached() {
        return isCached;
    }

    public void setCached(boolean cached) {
        isCached = cached;
    }

    @Override
    public String getOriginalPath() {
        return originalPath;
    }

    @Override
    public File getLocalFileObject() {
        if(isCached()) {
            return new File(ServiceAccessor.getCacheDirectory(), cachedFileName);
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
