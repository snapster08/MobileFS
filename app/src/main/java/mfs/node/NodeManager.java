package mfs.node;

import java.util.List;

public interface NodeManager {

    boolean isConnectedToGroup();

    boolean createGroup();

    boolean joinGroup(String groupLink);

    void exitGroup();

    String generateJoiningLink();

    List<MobileNode> getCurrentNodes();

    MobileNode getNode(String id);

    //void addNodeChangeListener();

    //void removeNodeChangeListener();
}
