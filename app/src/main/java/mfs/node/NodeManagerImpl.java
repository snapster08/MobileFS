package mfs.node;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import mfs.Utility;
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
                username, groupLink);
        String requestBody = Utility.nodeToJson(node).toString();
        Message requestMessage = new Message(MessageContract.Type.MSG_JOIN_REQUEST, requestBody);
        // send a join request to link
        Client.Response<String> response = Client.getInstance()
                .executeRequestString(Utility.getIpFromLink(groupLink),
                    Utility.getPortFromLink(groupLink), Utility.convertMessagetoString(requestMessage));
        if(response == null) {
            return false;
        }

        Log.i(LOG_TAG, "Received Response: " +response.getResult());
        response.close();
        // add the members in the group to the list

        isConnectedToGroup = true;
        return true;
    }

    @Override
    public void exitGroup() {

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

    private void addNode(MobileNode node)
    {
        if(!nodeMap.containsKey(node.getId())) {
            nodeMap.put(node.getId(), node);
            nodeList.add(node);
        }
    }
    private void removeNode(MobileNode node) {
        if(nodeMap.containsKey(node.getId())) {
            nodeMap.remove(node.getId());
            nodeList.remove(node);
        }
    }
}
