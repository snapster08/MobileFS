package mfs.node;

import android.content.Context;
import android.net.wifi.WifiManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import mfs.network.ServerContract;
import mfs.service.ServiceAccessor;

public class NodeManagerImpl implements NodeManager {
    private final static String LOG_TAG = NodeManagerImpl.class.getSimpleName();

    boolean isConnectedToGroup = false;
    List<MobileNode> nodesInGroup = new LinkedList<>();

    @Override
    public boolean isConnectedToGroup() {
        return isConnectedToGroup;
    }

    @Override
    public boolean createGroup() {
        return true;
    }

    @Override
    public boolean joinGroup(String groupLink) {
        // send a join request to link

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
        return null;
    }

    @Override
    public MobileNode getNode(String id) {
        return null;
    }
}
