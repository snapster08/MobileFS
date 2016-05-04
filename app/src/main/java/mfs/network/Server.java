package mfs.network;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mfs.Utility;
import mfs.filesystem.Filesystem;
import mfs.filesystem.MobileFile;
import mfs.node.MobileNode;
import mfs.node.NodeManager;
import mfs.permission.PermissionManager;
import mfs.service.ServiceAccessor;

public class Server {
    private static final String LOG_TAG = Server.class.getSimpleName();
    private Thread mListenerThread;
    private ExecutorService mRequestExecutor;
    private volatile boolean mIsRunning;

    public boolean start(final int port) {
        mIsRunning = true;
        // started the listener thread
        try {
            mListenerThread = new Thread(new Listener(port));
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "IOException while trying to create server socket.", e);
            mIsRunning = false;
            return false;
        }
        mRequestExecutor = Executors.newFixedThreadPool(ServerContract.SERVER_POOL_SIZE);
        mListenerThread.start();
        Log.i(LOG_TAG, "Server started, listening on port " +port);
        return true;
    }

    public void stop() {
        mIsRunning = false;
        // wait till the listener thread stops
        try {
            mListenerThread.join();
        }
        catch (InterruptedException e) {
            Log.e(LOG_TAG, "Interrupted while waiting for Listener Thread to stop.", e);
        }
        // shut down the request Executor
        mRequestExecutor.shutdownNow();
        mRequestExecutor = null;
        mListenerThread = null;
        Log.i(LOG_TAG, "Server stopped.");
    }

    private class Listener implements Runnable {
        private final String LOG_TAG = Listener.class.getSimpleName();
        private final ServerSocket mServerSocket;
        Listener(int port) throws IOException {
            mServerSocket = new ServerSocket(port);
        }
        @Override
        public void run() {
            while (mIsRunning) {
                try {
                    Socket requestSocket = mServerSocket.accept();
                    requestSocket.setSoTimeout(ServerContract.REQUEST_TIMEOUT);
                    // provide the request to the Executor
                    mRequestExecutor.execute(new RequestHandler(requestSocket));
                } catch (IOException e) {
                    Log.e(LOG_TAG, "IOException while trying to accept socket.", e);
                }
            }
            // close the server socket while exiting
            try {
                mServerSocket.close();
            }
            catch (IOException e) {
                Log.e(LOG_TAG, "IOException while trying to close server socket.", e);
            }
        }
    }

    private class RequestHandler implements Runnable {
        final String LOG_TAG = RequestHandler.class.getSimpleName();
        private final Socket mRequestSocket;
        RequestHandler(Socket requestSocket) {
            mRequestSocket = requestSocket;
        }
        // make sure to close the socket in the request, after handling the request
        @Override
        public void run() {
            handleRequest(mRequestSocket);
        }
    }

    void handleRequest(Socket requestSocket) {
        String requesMessage;
        try {
            // read the message
            DataInputStream in = new DataInputStream(requestSocket.getInputStream());
            requesMessage = in.readUTF();
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "IOException while trying read from request socket.", e);
            return;
        }
        Log.i(LOG_TAG, "Received Request: "+requesMessage);
        NodeManager nodeManager = ServiceAccessor.getNodeManager();
        PermissionManager permissionManager = ServiceAccessor.getPermissionManager();
        // convert the response to message object and handle it
        Message request = Utility.convertStringToMessage(requesMessage);
        boolean closeSocket = false;
        switch (request.getType()) {
            case MessageContract.Type.MSG_JOIN_REQUEST:
                if(nodeManager.isConnectedToGroup()) {
                    // add this node to my list
                    MobileNode newNode = Utility.convertJsonToNode(request.getBody());
                    ServiceAccessor.getNodeManager().addNode(newNode);
                    // send all node info in response
                    List<MobileNode> currentNodes = nodeManager.getCurrentNodes();
                    String responseBody = Utility.convertNodeListToJson(currentNodes).toString();
                    Message response = new Message(ServiceAccessor.getMyId(),
                            MessageContract.Type.MSG_JOIN_SUCCESS, responseBody);
                    sendResponse(requestSocket, Utility.convertMessageToString(response));
                    Log.i(LOG_TAG, "Sent Response: " +response.toString());

                    // send new node info to all the currentNodes
                    // create a new node info message
                    String requestBody = Utility.convertNodeToJson(newNode).toString();
                    Message requestMessage = new Message(ServiceAccessor.getMyId(),
                            MessageContract.Type.MSG_NEW_NODE_INFO, requestBody);
                    for (MobileNode node: ServiceAccessor.getNodeManager().getCurrentNodes()) {
                        Client.getInstance().sendMessage(Utility.getIpFromAddress(node.getAddress()),
                                Utility.getPortFromAddress(node.getAddress()),
                                Utility.convertMessageToString(requestMessage));
                    }
                }
                else {
                    // send failure response
                    Message response = new Message(ServiceAccessor.getMyId(),
                            MessageContract.Type.MSG_JOIN_FAILURE, "");
                    sendResponse(requestSocket, Utility.convertMessageToString(response));
                    Log.i(LOG_TAG, "Sent Response: " +response.toString());
                }
                break;


            case MessageContract.Type.MSG_NEW_NODE_INFO:
                if(nodeManager.isConnectedToGroup()) {
                    // add this node to my list
                    MobileNode newNode = Utility.convertJsonToNode(request.getBody());
                    ServiceAccessor.getNodeManager().addNode(newNode);
                }
                break;
            case MessageContract.Type.MSG_GET_FILE:
                String requestedFilepath = request.getBody();
                // get the file object and send the file meta-data if the file is present
                File requestedFile = new File(requestedFilepath);

                // check if the file is shared
                if(!permissionManager.isShared(requestedFile)) {
                    // send a failure response
                    Message failureResponse = new Message(ServiceAccessor.getMyId(),
                            MessageContract.Type.MSG_GET_FILE_FAILURE,
                            "File is not shared.");
                    sendResponse(requestSocket, Utility.convertMessageToString(failureResponse));
                    closeSocket = true;
                    break;
                }

                // check if the file exists
                if(!requestedFile.isFile()) {
                    // send a failure response
                    Message failureResponse = new Message(ServiceAccessor.getMyId(),
                            MessageContract.Type.MSG_GET_FILE_FAILURE,
                            "File doesn't exist");
                    sendResponse(requestSocket, Utility.convertMessageToString(failureResponse));
                    closeSocket = true;
                    break;
                }

                // acquireLock the file
                if(!permissionManager.acquireLock(requestedFile, request.getSenderId())) {
                    // send a failure response
                    Message failureResponse = new Message(ServiceAccessor.getMyId(),
                            MessageContract.Type.MSG_GET_FILE_FAILURE,
                            "File is locked.");
                    sendResponse(requestSocket, Utility.convertMessageToString(failureResponse));
                    closeSocket = true;
                    break;
                }

                // send success response containing the file meta data
                Message successResponse = new Message(ServiceAccessor.getMyId(),
                        MessageContract.Type.MSG_GET_FILE_SUCCESS,
                        Utility.convertFileMetadataToJson(requestedFile.getAbsolutePath(),
                                requestedFile.length()));
                sendResponse(requestSocket, Utility.convertMessageToString(successResponse));

                // send the file contents
                try {
                    Log.i(LOG_TAG, "Starting to send file: " +requestedFilepath);
                    final int BUFFER_SIZE = 10 * 1024;
                    byte [] buffer = new byte[BUFFER_SIZE];
                    long remainingFileSize = requestedFile.length();
                    InputStream fileInputStream = new FileInputStream(requestedFile);
                    OutputStream socketOutputStream = requestSocket.getOutputStream();
                    while(remainingFileSize > 0) {
                        int bytesRead = fileInputStream.read(buffer);
                        socketOutputStream.write(buffer, 0, bytesRead);
                        remainingFileSize -= bytesRead;
                    }
                    fileInputStream.close();
                    Log.i(LOG_TAG, "Done sending file: " +requestedFilepath);
                }
                catch (FileNotFoundException e) {
                    Log.e(LOG_TAG, "File Not Found, but this has been checked already.", e);
                    closeSocket = true;
                    break;
                }
                catch (IOException e) {
                    Log.e(LOG_TAG, "Unable to get requestsocket input stream. Socket closed already??", e);
                    closeSocket = true;
                    break;
                }
                break;
            case MessageContract.Type.MSG_GET_FS_METADATA:
                List<File> sharedFiles = permissionManager.getSharedFiles();
                // response Body
                JSONObject metadata = Utility.convertFileListToJson(sharedFiles);
                JSONObject responseBody = new JSONObject();
                try {
                    responseBody.put(MessageContract.Field.FIELD_FS_METADATA, metadata);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error building response.", e);
                    // send a failure response
                    Message failureResponse = new Message(ServiceAccessor.getMyId(),
                            MessageContract.Type.MSG_GET_FS_METADATA_FAILURE,
                            "Unable to build response");
                    sendResponse(requestSocket, Utility.convertMessageToString(failureResponse));
                    break;
                }
                // send response
                Message metadataResponse = new Message(ServiceAccessor.getMyId(),
                        MessageContract.Type.MSG_GET_FS_METADATA_SUCCESS,
                        responseBody.toString());

                sendResponse(requestSocket, Utility.convertMessageToString(metadataResponse));
                break;
            case MessageContract.Type.MSG_COMMIT_REQUEST:
                // TODO check if this is a shared file
                // parse request containing the file meta-data
                try {
                    String[] fileMetadata = Utility.convertJsonToFileMetadata(request.getBody());
                    if (fileMetadata == null) {
                        requestSocket.close();
                        break;
                    }

                    // send the commit accept response
                    Message commitAcceptResponse = new Message(ServiceAccessor.getMyId(),
                            MessageContract.Type.MSG_COMMIT_ACCEPT,
                            "");
                    sendResponse(requestSocket, Utility.convertMessageToString(commitAcceptResponse));

                    // deleting file
                    File localFile = new File(fileMetadata[0]);
                    localFile.delete();

                    // get the file and write to a temporary file
                    Log.i(LOG_TAG, "Starting to receive file: " + fileMetadata[0]);
                    File receivedFile = new File(fileMetadata[0]);
                    receivedFile.getParent();
                    String tempFilepath = receivedFile.getParent() +"/" +Utility.genHash(fileMetadata[0]);
                    Log.i(LOG_TAG, "Writing file: " + fileMetadata[0] +" to tempfile: "+ tempFilepath);
                    final int BUFFER_SIZE = 10 * 1024;
                    byte[] buffer = new byte[BUFFER_SIZE];
                    InputStream socketInputStream = requestSocket.getInputStream();
                    OutputStream fileOutputStream = new FileOutputStream(tempFilepath);
                    long remainingFileSize = Long.parseLong(fileMetadata[1]);
                    try {
                        while (remainingFileSize > 0) {
                            int bytesRead = socketInputStream.read(buffer);
                            fileOutputStream.write(buffer, 0, bytesRead);
                            remainingFileSize -= bytesRead;
                        }
                        fileOutputStream.close();
                        Log.i(LOG_TAG, "Done writing file: " + fileMetadata[0] +" to tempfile: "+ tempFilepath);

                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Did not receive the complete file. Received: " + (Long.parseLong(fileMetadata[1]) - remainingFileSize)
                                + " Remaining: " + remainingFileSize, e);
                        break;
                    }

                    // delete original file and rename the newly received file
                    receivedFile.delete();
                    File tempFileObj = new File(tempFilepath);
                    tempFileObj.renameTo(receivedFile);
                    Log.i(LOG_TAG, "Renamed tempfile as: " + receivedFile.getAbsolutePath());
                }
                catch (InterruptedIOException e) {
                    Log.e(LOG_TAG, "Read timed-out.", e);
                    break;
                }
                catch (IOException e) {
                    Log.e(LOG_TAG, "IOException.", e);
                    break;
                }
                // send commit completed message
                Message commitCompleteMessage = new Message(ServiceAccessor.getMyId(),
                        MessageContract.Type.MSG_COMMIT_COMPLETE, "");
                sendResponse(requestSocket, Utility.convertMessageToString(commitCompleteMessage));
                break;
            case MessageContract.Type.MSG_LEAVE:
                MobileNode leavingNode = nodeManager.getNode(request.getBody());
                if(leavingNode == null) {
                    try {
                        requestSocket.close();
                    }catch (IOException e) {
                        Log.e(LOG_TAG, "IOException.", e);
                    }
                    break;
                }
                Log.i(LOG_TAG, "In MSG_LEAVE " + Utility.convertNodeToJson(leavingNode));
                // close all files of this node
                Filesystem leavingFS = leavingNode.getBackingFilesystem();
                if(leavingFS != null) {
                    List<MobileFile> leavingFiles = leavingFS.getOpenFiles();
                    for(MobileFile file: leavingFiles) {
                        leavingFS.closeFile(file);
                    }
                }
                // remove this node from the list
                nodeManager.removeNode(leavingNode);
                Log.i(LOG_TAG, "Current Nodes after remove in leave: "
                        +Utility.convertNodeListToJson(nodeManager.getCurrentNodes()));
                try {
                    requestSocket.close();
                }catch (IOException e) {
                    Log.e(LOG_TAG, "IOException.", e);
                }
                Log.i(LOG_TAG, "Leaving MSG_LEAVE");
                break;
            case MessageContract.Type.MSG_FILE_CLOSE:
                String filenameToClose = request.getBody();
                if(filenameToClose == null) {
                    closeSocket = true;
                    break;
                }

                File file = new File(filenameToClose);
                // release the lock
                permissionManager.releaseLock(file, request.getSenderId());
                break;
            case MessageContract.Type.MSG_GET_GROUP_INFO:
                if (NetworkChangeReceiver.isResyncing()) {
                    Log.i(LOG_TAG, "Resync in progress here so not responding.");
                    closeSocket = true;
                    break;
                }
                String requesterAddress = request.getBody();
                // send all node info in response
                List<MobileNode> currentNodes = nodeManager.getCurrentNodes();
                String allNodeInfo = Utility.convertNodeListToJson(currentNodes).toString();
                Message groupInfoResponse = new Message(ServiceAccessor.getMyId(),
                        MessageContract.Type.MSG_GROUP_INFO, allNodeInfo);
                Client.getInstance().sendMessage(
                        Utility.getIpFromAddress(requesterAddress),
                        Utility.getPortFromAddress(requesterAddress),
                        Utility.convertMessageToString(groupInfoResponse)
                );
                Log.i(LOG_TAG, "Sent Response: " +groupInfoResponse.toString());
                closeSocket = true;
                break;

            case MessageContract.Type.MSG_GROUP_INFO:
                // update my node list
                List<MobileNode> nodeList = Utility.convertJsonToNodeList(request.getBody());

                synchronized (nodeManager.getCurrentNodes()) {
                    for (MobileNode currentNode : nodeList) {
                        nodeManager.addNode(currentNode);
                    }
                }
                if (NetworkChangeReceiver.isResyncing()) {
                    Log.i(LOG_TAG, "Re-syncing complete.");
                }
                NetworkChangeReceiver.setIsResyncing(false);
                Log.i(LOG_TAG, "Current Nodes: " +Utility.convertNodeListToJson(nodeManager.getCurrentNodes()));
                break;
            default:
                Log.e(LOG_TAG, "Unsupported request ignoring.");
        }
        if(closeSocket) {
            try {
                requestSocket.close();
            }
            catch (IOException e) {
                Log.e(LOG_TAG, "IOException.", e);
            }
        }
    }

    boolean sendResponse(Socket socket, String response){
        // send message
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(response);
            return true;
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "IOException while trying send response.", e);
            return false;
        }
    }
}
