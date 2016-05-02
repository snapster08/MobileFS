package mfs.service;

import android.content.Context;

import java.io.File;

import mfs.node.NodeManager;
import mfs.node.NodeManagerImpl;
import mfs.permission.PermissionManager;
import mfs.permission.PermissionMnagerImpl;

public class  ServiceAccessor {
    public final static String LOG_TAG = ServiceAccessor.class.getSimpleName();

    private static NodeManager sNodeManager;
    private static PermissionManager sPermissionManager;
    private static String sMyId;
    private static Context sContext;

    public static File getCacheDirectory() {
        return sContext.getFilesDir();
    }

    public static String getMyId() {
        return sMyId;
    }

    public static void setMyId(String myId) {
        ServiceAccessor.sMyId = myId;
    }

    public static Context getContext() {
        return sContext;
    }

    public static void setContext(Context sContext) {
        ServiceAccessor.sContext = sContext;
    }

    public static NodeManager getNodeManager() {
        if(sNodeManager == null) {
            sNodeManager = new NodeManagerImpl();
        }
        return sNodeManager;
    }

    public static PermissionManager getPermissionManager() {
        if(sPermissionManager == null) {
            sPermissionManager = new PermissionMnagerImpl();
        }
        return sPermissionManager;
    }



}
