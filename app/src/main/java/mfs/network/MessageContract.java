package mfs.network;

public class MessageContract {

    public static class Type {
        // types for join operation
        public final static int MSG_JOIN_REQUEST = 100;
        public final static int MSG_JOIN_SUCCESS = 101;
        public final static int MSG_JOIN_FAILURE = 102;

        // types for get file operation
        public final static int MSG_GET_FS_METADATA = 200;
        public final static int MSG_GET_FS_METADATA_SUCCESS = 201;
        public final static int MSG_GET_FS_METADATA_FAILURE = 302;

        // types for get file operation
        public final static int MSG_GET_FILE = 300;
        public final static int MSG_GET_FILE_SUCCESS = 301;
        public final static int MSG_GET_FILE_FAILURE = 302;
    }


    public static class Field {
        public final static String FIELD_MSG_TYPE = "MSG_TYPE";
        public final static String FIELD_MSG_BODY = "MSG_BODY";

        public final static String FIELD_NODE_ID = "NODE_ID";
        public final static String FIELD_NODE_NAME = "NODE_NAME";
        public final static String FIELD_NODE_ADDRESS = "NODE_ADDRESS";

        public final static String FIELD_FILE_PATH = "FILE_PATH";
        public final static String FIELD_FILE_SIZE = "FILE_SIZE";

        public final static String FIELD_FS_ROOT = "FS_ROOT";
        public final static String FIELD_FS_METADATA = "FS_METADATA";
    }
}
