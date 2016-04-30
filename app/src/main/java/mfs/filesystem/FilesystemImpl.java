package mfs.filesystem;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import mfs.node.MobileNode;

public class FilesystemImpl implements Filesystem {
    private final static String LOG_TAG = FilesystemImpl.class.getSimpleName();

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
}
