package mfs.permission;

import mfs.filesystem.MobileFile;
import mfs.node.MobileNode;

public class PermissionMnagerImpl implements PermissionManager {
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
}
