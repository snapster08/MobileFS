package mfs.network;

public class MessageContract {

    public static class Type {
        // types for join operation
        public final static int MSG_JOIN_REQUEST = 100;
        public final static int MSG_JOIN_SUCCESS = 101;
        public final static int MSG_JOIN_FAILURE = 102;
    }


    public static class Field {
        public final static String FIELD_MSG_TYPE = "MSG_TYPE";
        public final static String FIELD_MSG_BODY = "MSG_BODY";
        public final static String FIELD_NODE_ID = "NODE_ID";
        public final static String FIELD_NODE_NAME = "NODE_NAME";
        public final static String FIELD_NODE_ADDRESS = "NODE_ADDRESS";

    }
}
