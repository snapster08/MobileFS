package mfs.node;

import java.util.List;

import mfs.filesystem.MobileFile;

public interface NodeManager {

    boolean isConnectedToGroup();

    boolean createGroup(String username);

    boolean joinGroup(String groupLink, String username);

    void exitGroup();

    String generateJoiningLink();

    List<MobileNode> getCurrentNodes();

    MobileNode getNode(String id);

    void addNode(MobileNode node);

    void removeNode(MobileNode node);

    List<MobileFile> getAllOpenFiles();

        //void addNodeChangeListener();

    //void removeNodeChangeListener();
}
