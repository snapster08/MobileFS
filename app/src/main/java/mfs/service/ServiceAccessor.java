package mfs.service;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import mfs.filesystem.Filesystem;
import mfs.filesystem.MobileFile;
import mfs.node.MobileNode;
import mfs.node.NodeManager;
import mfs.permission.PermissionManager;

/**
 * Created by barry on 4/18/16.
 */
public class  ServiceAccessor {

    public final static String LOG_TAG = ServiceAccessor.class.getSimpleName();

    public static NodeManager getNodeManager() {
        return new NodeManager() {
            @Override
            public boolean isConnectedToGroup() {
                return false;
            }

            @Override
            public boolean createGroup() {

                return true;
            }

            @Override
            public boolean joinGroup(String groupLink) {

                return true;
            }

            @Override
            public void exitGroup() {

            }

            @Override
            public String generateJoiningLink() {
                return null;
            }

            @Override
            public List<MobileNode> getCurrentNodes() {
                return null;
            }
        };
    }

    public static PermissionManager getPermissionManager() {
        return new PermissionManager() {
            @Override
            public boolean isReadable(MobileFile file, MobileNode n) {
                return false;
            }

            @Override
            public boolean isWriteable(MobileFile file, MobileNode n) {
                return false;
            }

            @Override
            public void setReadable(MobileFile file, MobileNode n, boolean permission) {

            }

            @Override
            public void setWriteable(MobileFile file, MobileNode n, boolean permission) {

            }

            @Override
            public void setGlobalReadable(MobileFile file, boolean permission) {

            }

            @Override
            public void setGlobalWriteable(MobileFile file, boolean permission) {

            }
        };
    }

    public static Filesystem getFilesystem() {
        return new Filesystem() {
            @Override
            public MobileFile openFile(String path, MobileNode node) {
                return null;
            }

            @Override
            public void commitFile(MobileFile fileHandle) {

            }

            @Override
            public void closeFile(MobileFile fileHandle) {

            }

            @Override
            public boolean isOpen(MobileFile file) {
                return false;
            }

            @Override
            public List<String> ls(String path) {
                return null;
            }

            @Override
            public JSONObject getFileSystemStructure(String path) {
                return getFileSystemStructure(path, false);
            }

            @Override
            public JSONObject getFileSystemStructure(String path, boolean includeHidden) {

                File root = new File(path);

                // return null if the path is not a directory
                if(!root.isDirectory()) {
                    return null;
                }

                JSONObject fileSystemStructure = new JSONObject();
                try {

                    File [] fileList = root.listFiles();

                    JSONArray fileSystemArray = new JSONArray();
                    for (File currentFile: fileList) {

                        // ignore hidden files
                        if(!includeHidden && currentFile.isHidden()){
                            continue;
                        }

                        // for files just add the name
                        if(currentFile.isFile()){
                            fileSystemArray.put(currentFile.getName());
                            continue;
                        }

                        // for directories create a JSONObject {"directoryname" : [<contents>]}
                        fileSystemArray.put(getFileSystemStructure(currentFile.getAbsolutePath()));
                    }

                    fileSystemStructure.put(root.getName(), fileSystemArray);
                }
                catch (JSONException e) {
                    Log.i(LOG_TAG, "Improper format", e);
                    return null;
                }
                return fileSystemStructure;
            }
        };
    }
}
