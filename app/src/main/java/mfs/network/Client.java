package mfs.network;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;

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
}
