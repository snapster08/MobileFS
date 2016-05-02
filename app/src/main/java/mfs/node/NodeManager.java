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

    void addNode(MobileNode node);

    String getSharedFile();

    void setSharedFile(String sharedFile);


    //void addNodeChangeListener();

    //void removeNodeChangeListener();
}
