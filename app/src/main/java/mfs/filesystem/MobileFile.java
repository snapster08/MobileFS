package mfs.filesystem;

import java.io.File;

public interface MobileFile {

    Filesystem getOwningFilesystem();

    String getOriginalPath();

    File getLocalFileObject();

    void setLocalFileName(String name);

    public String getLocalFileName();

    int getType();
}
