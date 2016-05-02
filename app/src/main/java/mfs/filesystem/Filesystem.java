package mfs.filesystem;

import java.util.List;

public interface Filesystem {

    MobileFile openFile(String path);

    void commitFile(MobileFile fileHandle);

    void closeFile(MobileFile fileHandle);

    boolean isOpen(MobileFile file);

    MobileFile getOpenedFile(String path);

    List<MobileFile> getOpenFiles();

    List<MobileFile> ls(String path);

}
