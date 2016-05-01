package mfs.node;

import java.util.List;

public interface NodeManager {

    boolean isConnectedToGroup();

    boolean createGroup(String username);

    boolean joinGroup(String groupLink, String username);

    void exitGroup();

    String generateJoiningLink();

    List<MobileNode> getCurrentNodes();

    MobileNode getNode(String id);

    //void addNodeChangeListener();

    //void removeNodeChangeListener();
}
