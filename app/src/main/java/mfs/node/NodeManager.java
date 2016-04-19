package mfs.node;

import java.util.List;

/**
 * Created by bala on 4/11/16.
 */
public interface NodeManager {

    boolean isConnectedToGroup();

    void createGroup();

    boolean joinGroup(String groupLink);

    void exitGroup();

    String generateJoiningLink();

    List<MobileNode> getCurrentNodes();

    //void addNodeChangeListener();

    //void removeNodeChangeListener();
}
