package mfs.filesystem;

import java.io.File;

/**
 * Created by bala on 4/11/16.
 */
public interface MobileFile {

    String getOriginalPath();

    File getLocalFileObject();
}
