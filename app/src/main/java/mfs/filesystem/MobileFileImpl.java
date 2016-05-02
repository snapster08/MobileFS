package mfs.filesystem;

import java.io.File;

import mfs.service.ServiceAccessor;

public class MobileFileImpl implements MobileFile {
    Filesystem owningFilesystem;
    String originalPath;
    boolean isCached;
    String cachedFileName;

    public MobileFileImpl(Filesystem owningFilesystem, String originalPath) {
        this.owningFilesystem = owningFilesystem;
        this.originalPath = originalPath;
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
        return null;
    }

    @Override
    public File getLocalFileObject() {
        if(isCached()) {
            return new File(ServiceAccessor.getCacheDirectory(), cachedFileName);
        }
        return null;
    }
}
