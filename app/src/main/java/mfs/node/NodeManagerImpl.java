package mfs.node;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import mfs.Utility;
import mfs.filesystem.Filesystem;
import mfs.filesystem.MobileFile;
import mfs.network.Client;
import mfs.network.Message;
import mfs.network.MessageContract;
import mfs.network.ServerContract;
import mfs.service.ServiceAccessor;

public class NodeManagerImpl implements NodeManager {
    private final static String LOG_TAG = NodeManagerImpl.class.getSimpleName();

    boolean isConnectedToGroup = false;
    List<MobileNode> nodeList = new LinkedList<>();
    HashMap<String, MobileNode> nodeMap = new HashMap<>();
        @Override
    public boolean isConnectedToGroup() {
        return isConnectedToGroup;
    }

    @Override
    public boolean createGroup(String username) {
        // add myself to the nodeList
        MobileNode myNode = new MobileNodeImpl(ServiceAccessor.getMyId(),
                username, generateJoiningLink());
        addNode(myNode);

        isConnectedToGroup = true;
        return true;
    }

    @Override
    public boolean joinGroup(String groupLink, String username) {
        Log.i(LOG_TAG, "Joining Group: " +groupLink +" with username: "+username);
        // create a join request
        MobileNode node = new MobileNodeImpl(ServiceAccessor.getMyId(),
                username, generateJoiningLink());
        String requestBody = Utility.convertNodeToJson(node).toString();
        Message requestMessage = new Message(ServiceAccessor.getMyId(),
                MessageContract.Type.MSG_JOIN_REQUEST, requestBody);
        // send a join request to link
        Client.Response<String> response = Client.getInstance()
                .executeRequestString(Utility.getIpFromAddress(groupLink),
                    Utility.getPortFromAddress(groupLink), Utility.convertMessageToString(requestMessage));
        if(response == null) {
            return false;
        }

        Log.i(LOG_TAG, "Received Response: " +response.getResult());
        Message responseMessage = Utility.convertStringToMessage(response.getResult());
        response.close();

        // if response is failure
        if(responseMessage.getType() != MessageContract.Type.MSG_JOIN_SUCCESS){
            return false;
        }

        // add the members in the group to the list
        List<MobileNode> nodeList = Utility.convertJsonToNodeList(responseMessage.getBody());
        for (MobileNode currentNode : nodeList) {
            addNode(currentNode);
        }
        Log.i(LOG_TAG, "Current Nodes: " +Utility.convertNodeListToJson(getCurrentNodes()));
        isConnectedToGroup = true;
        return true;
    }

    @Override
    public void exitGroup() {
        // send leave group message to everyone
        for(MobileNode node : getCurrentNodes()) {
            if(!node.getId().equals(ServiceAccessor.getMyId())) {
                Message leaveGroupMessage = new Message(ServiceAccessor.getMyId(),
                        MessageContract.Type.MSG_LEAVE,
                        ServiceAccessor.getMyId());
                Client.getInstance().sendMessage(
                        Utility.getIpFromAddress(node.getAddress()),
                        Utility.getPortFromAddress(node.getAddress()),
                        Utility.convertMessageToString(leaveGroupMessage)
                );
            }
        }
        // close all my open file
        List<MobileFile> allOpenFiles = getAllOpenFiles();
        for(MobileFile file : allOpenFiles) {
            file.getOwningFilesystem().closeFile(file);
        }
        clearNodes();
        isConnectedToGroup = false;

        // clear my shared files
        ServiceAccessor.getPermissionManager().clearSharedFiles();

    }

    @Override
    public String generateJoiningLink() {

        // check if context is set
        Context context = ServiceAccessor.getContext();
        if(context == null) {
            return null;
        }

        // get my ip address
        WifiManager wm = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        int ipAddress = wm.getConnectionInfo().getIpAddress();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff)));
        sb.append(":");
        sb.append(ServerContract.SERVER_PORT);
        return sb.toString();
    }

    @Override
    public List<MobileNode> getCurrentNodes() {
        return nodeList;
    }

    @Override
    public MobileNode getNode(String id) {
        return nodeMap.get(id);
    }

    @Override
    public void addNode(MobileNode newNode)
    {
        if(!nodeMap.containsKey(newNode.getId())) {
            nodeMap.put(newNode.getId(), newNode);
            nodeList.add(newNode);
        }
        else {
            // update the current node
            MobileNode node = nodeMap.get(newNode.getId());
            node.setName(newNode.getName());
            node.setAddress(newNode.getAddress());
        }

    }

    @Override
    public void removeNode(MobileNode node) {
        if(nodeMap.containsKey(node.getId())) {
            nodeMap.remove(node.getId());
            nodeList.remove(node);
        }
        else {
            Log.i(LOG_TAG, "Trying to remove node id: " +node.getId() +" name: " +node.getName());
        }
    }

    private void clearNodes() {
        nodeList.clear();
        nodeMap.clear();
    }

    @Override
    public List<MobileFile> getAllOpenFiles() {
        List<MobileNode> nodes = getCurrentNodes();
        List<MobileFile> fileList= new ArrayList<>();
        for(MobileNode node : nodes) {
            Filesystem fs = node.getBackingFilesystem();
            if (fs != null) {
                fileList.addAll(fs.getOpenFiles());
            }
        }
        return fileList;
    }

}
