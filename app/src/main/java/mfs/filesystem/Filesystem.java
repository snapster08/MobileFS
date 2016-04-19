package mfs.filesystem;

import org.json.JSONObject;

import java.util.List;

import mfs.node.MobileNode;

/**
 * Created by bala on 4/11/16.
 */
public interface Filesystem {

    MobileFile openFile(String path, MobileNode node);

    void commitFile(MobileFile fileHandle);

    void closeFile(MobileFile fileHandle);

    boolean isOpen(MobileFile file);

    List<String> ls(String path);

    JSONObject getFileSystemStructure(String path, boolean includeHidden);

    JSONObject getFileSystemStructure(String path);

}
