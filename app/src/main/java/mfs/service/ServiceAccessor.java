package mfs.service;

import android.content.Context;
import android.os.Environment;

import java.io.File;

import mfs.node.NodeManager;
import mfs.node.NodeManagerImpl;
import mfs.permission.PermissionManager;
import mfs.permission.PermissionManagerImpl;

public class  ServiceAccessor {
    public final static String LOG_TAG = ServiceAccessor.class.getSimpleName();

    private static NodeManager sNodeManager;
    private static PermissionManager sPermissionManager;
    private static String sMyId;
    private static Context sContext;
    private static File cachedDirectory;

    public static File getCacheDirectory() {
        if(cachedDirectory != null) {
            return cachedDirectory;
        }
        else {
            cachedDirectory = new File(Environment.getExternalStorageDirectory(), "MobileFS");
            cachedDirectory.mkdir();
            return cachedDirectory;
        }
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
            sPermissionManager = new PermissionManagerImpl();
        }
        return sPermissionManager;
    }



}
