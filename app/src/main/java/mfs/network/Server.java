package mfs.network;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mfs.Utility;
import mfs.node.MobileNode;
import mfs.node.NodeManager;
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
        NodeManager nm = ServiceAccessor.getNodeManager();
        // convert the response to message object and handle it
        Message request = Utility.convertStringToMessage(requesMessage);
        switch (request.getType()) {
            case MessageContract.Type.MSG_JOIN_REQUEST:
                if(nm.isConnectedToGroup()) {
                    // add this node to my list
                    MobileNode newNode = Utility.convertJsonToNode(request.getBody());
                    ServiceAccessor.getNodeManager().addNode(newNode);
                    // send the member details in response
                    List<MobileNode> currentNodes = nm.getCurrentNodes();
                    String responseBody = Utility.convertNodeListToJson(currentNodes).toString();
                    Message response = new Message(MessageContract.Type.MSG_JOIN_SUCCESS, responseBody);
                    sendResponse(requestSocket, Utility.convertMessagetoString(response));
                    Log.i(LOG_TAG, "Sent Response: " +response.toString());
                    // TODO send new node info to all the currentNodes
                }
                else {
                    // send failure response
                    Message response = new Message(MessageContract.Type.MSG_JOIN_FAILURE, null);
                    sendResponse(requestSocket, Utility.convertMessagetoString(response));
                    Log.i(LOG_TAG, "Sent Response: " +response.toString());
                }
                break;
            case MessageContract.Type.MSG_GET_FILE:
                String requestedFilepath = request.getBody();

                // get the file object and send the file meta-data if the file is present
                File requestedFile = new File(requestedFilepath);
                if(!requestedFile.isFile()) {
                    // send a failure response
                    Message failureResponse = new Message(MessageContract.Type.MSG_GET_FILE_FAILURE,
                            "File doesn't exist");
                    sendResponse(requestSocket, Utility.convertMessagetoString(failureResponse));
                    break;
                }
                // send success response containing the file meta data
                Message successResponse = new Message(MessageContract.Type.MSG_GET_FILE_SUCCESS,
                        Utility.convertFileMetadataToJson(requestedFile));
                sendResponse(requestSocket, Utility.convertMessagetoString(successResponse));

                // send the file contents
                try {
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
                }
                catch (FileNotFoundException e) {
                    Log.e(LOG_TAG, "File Not Found, but this has been checked already.", e);
                    break;
                }
                catch (IOException e) {
                    Log.e(LOG_TAG, "Unable to get requestsocket input stream. Socket closed already??", e);
                    break;
                }
                break;
            case MessageContract.Type.MSG_GET_FS_METADATA:
                // TODO Implement me
                break;
            default:
                Log.e(LOG_TAG, "Unsupported request ignoring.");
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
