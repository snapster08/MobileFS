package mfs.filesystem;

import org.json.JSONObject;

import java.util.List;

import mfs.node.MobileNode;

public interface Filesystem {

    MobileFile openFile(String path);

    boolean commitFile(MobileFile fileHandle);

    boolean closeFile(MobileFile fileHandle);

    boolean isOpen(MobileFile file);

    MobileFile getOpenedFile(String path);

    List<MobileFile> getOpenFiles();

    MobileNode getOwningNode();

    String getRootDirectory();

    JSONObject getFilesystemMetadata();

    void setFilesystemMetadata(JSONObject filesystemMetadata);

    void setRootDirectory(String rootDirectory);

    void setOwningNode(MobileNode owningNode);

    List<MobileFile> ls(String path);

}
