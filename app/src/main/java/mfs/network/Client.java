package mfs.network;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;

import mfs.Utility;
import mfs.service.ServiceAccessor;

public class Client {
    private static final String LOG_TAG = Client.class.getSimpleName();
    private static Client mInstance;
    private Client() {

    }

    public static Client getInstance() {
        if(mInstance == null) {
            mInstance = new Client();
        }
        return mInstance;
    }

    public static class Response<T> {
        private final Socket socket;
        private final T result;

        Response(Socket socket, T result) {
            this.socket = socket;
            this.result = result;
        }

        public Socket getSocket() {
            return socket;
        }

        public T getResult() {
            return result;
        }

        public void close() {
            try {
                getSocket().close();
            }
            catch (IOException e) {
                Log.e(LOG_TAG, "Unable to Close Request's socket");
            }
        }

    }

    public Response<String> executeRequestString(String dstName, int dstPort, String rqtMessage) {
        if(dstName == null || rqtMessage == null) {
            Log.e(LOG_TAG, "Called executeRequestString() with null parameters.");
            return null;
        }
        Socket socket;
        try {
            Log.i(LOG_TAG, "Connecting to IP:" +dstName +" Port: " +dstPort);
            socket = new Socket(dstName, dstPort);
            socket.setSoTimeout(ServerContract.REQUEST_TIMEOUT);
            // send message
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(rqtMessage);
            Log.i(LOG_TAG, "Sent: " +rqtMessage);
            // read response
            DataInputStream in = new DataInputStream(socket.getInputStream());
            String response = in.readUTF();
            Log.i(LOG_TAG, "Received: " +response);
            return new Response<>(socket, response);
        }
        catch (InterruptedIOException e) {
            Log.e(LOG_TAG, "Read timed-out.", e);
            return null;
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "IOException.", e);
            return null;
        }
    }

    public void sendMessage(String dstName, int dstPort, String message) {
        if(dstName == null || message == null) {
            Log.e(LOG_TAG, "Called sendMessage() with null parameters.");
            return;
        }
        Socket socket;
        try {
            Log.i(LOG_TAG, "Connecting to IP:" +dstName +" Port: " +dstPort);
            socket = new Socket(dstName, dstPort);
            socket.setSoTimeout(ServerContract.REQUEST_TIMEOUT);
            // send message
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(message);
            Log.i(LOG_TAG, "Sent: " +message);
            socket.close();
        }
        catch (InterruptedIOException e) {
            Log.e(LOG_TAG, "Read timed-out.", e);
            return;
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "IOException.", e);
            return;
        }
    }


    public Response<File> executeRequestFile(String dstName, int dstPort, String filePath) {
        if(dstName == null || filePath == null) {
            Log.e(LOG_TAG, "Called executeRequestString() with null parameters.");
            return null;
        }
        Socket socket;
        try {
            // create file request message
            Message fileRequest = new Message(ServiceAccessor.getMyId(),
                    MessageContract.Type.MSG_GET_FILE, filePath);
            String requestString = Utility.convertMessageToString(fileRequest);

            // connect to destination
            Log.i(LOG_TAG, "Connecting to IP:" +dstName +" Port: " +dstPort);
            socket = new Socket(dstName, dstPort);
            socket.setSoTimeout(ServerContract.REQUEST_TIMEOUT);

            // send message
            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
            dout.writeUTF(requestString);
            Log.i(LOG_TAG, "Sent: " +requestString);

            // read response which contains metadata
            DataInputStream din = new DataInputStream(socket.getInputStream());
            String responseString = din.readUTF();
            Log.i(LOG_TAG, "Received: " +responseString);

            // check if the request was successful
            Message response = Utility.convertStringToMessage(responseString);
            if(response.getType() == MessageContract.Type.MSG_GET_FILE_FAILURE) {
                socket.close();
                return null;
            }

            // parse file meta-data
            String [] fileMetadata = Utility.convertJsonToFileMetadata(response.getBody());
            if(fileMetadata == null){
                socket.close();
                return null;
            }

            // read the file from socket and store it in a local file
            Log.i(LOG_TAG, "Starting to receive file: " +fileMetadata[0]);

            final int BUFFER_SIZE = 10*1024;
            byte[] buffer = new byte[BUFFER_SIZE];
            InputStream socketInputStream = socket.getInputStream();
            String originFilename = fileMetadata[0];
            String [] parts = originFilename.split("\\.");
            String extension = parts[parts.length -1];
            String cachedFilename = Utility.genHash(originFilename) +"." +extension;
            Log.i(LOG_TAG, "Storing it as file: " +cachedFilename);
            OutputStream fileOutputStream = new FileOutputStream(
                    ServiceAccessor.getCacheDirectory() +"/" +cachedFilename);
            long remainingFileSize = Long.parseLong(fileMetadata[1]);
            try {
                while (remainingFileSize > 0) {
                    int bytesRead = socketInputStream.read(buffer);
                    fileOutputStream.write(buffer, 0, bytesRead);
                    remainingFileSize -= bytesRead;
                }
                fileOutputStream.close();
                Log.i(LOG_TAG, "Done receiving file: " +fileMetadata[0]);

            }
            catch (IOException e) {
                Log.e(LOG_TAG, "Did not receive the complete file. Received: " +(Long.parseLong(fileMetadata[1]) - remainingFileSize)
                    +" Remaining: " +remainingFileSize, e);
                return null;
            }

            // return the cached file object in the response
            return new Response<>(socket, new File(ServiceAccessor.getCacheDirectory() ,cachedFilename));
        }
        catch (InterruptedIOException e) {
            Log.e(LOG_TAG, "Read timed-out.", e);
            return null;
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "IOException.", e);
            return null;
        }
    }
}
