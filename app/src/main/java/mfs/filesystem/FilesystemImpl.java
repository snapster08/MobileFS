package mfs.filesystem;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import mfs.node.MobileNode;

public class FilesystemImpl implements Filesystem {
    private final static String LOG_TAG = FilesystemImpl.class.getSimpleName();

    private JSONObject filesystemMetadata;
    private String rootDirectory;
    private MobileNode owningNode;

    public FilesystemImpl(String rootDirectory, JSONObject filesystemMetadata, MobileNode owningNode) {
        this.rootDirectory = rootDirectory;
        this.filesystemMetadata = filesystemMetadata;
        this.owningNode = owningNode;
    }

    List<MobileFile> openfileList = new LinkedList<>();

    public MobileNode getOwningNode() {
        return owningNode;
    }

    @Override
    public MobileFile openFile(String path, MobileNode node) {
        return null;
    }

    @Override
    public void commitFile(MobileFile fileHandle) {

    }

    @Override
    public void closeFile(MobileFile fileHandle) {

    }

    @Override
    public boolean isOpen(MobileFile file) {
        return false;
    }

    @Override
    public List<String> ls(String path) {
        return null;
    }

}
