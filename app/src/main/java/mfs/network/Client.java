package mfs.network;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

    class Response<T> {
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

    }

    Response<String> executeRequestString(String dstName, int dstPort, String rqtMessage) {
        Socket socket;
        try {
            socket = new Socket(dstName, dstPort);
            // send message
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(rqtMessage);
            out.close();
            // read response
            DataInputStream in = new DataInputStream(socket.getInputStream());
            String response = in.readUTF();
            in.close();
            return new Response<>(socket, response);
        }
        catch (IOException e) {
            Log.v(LOG_TAG, "IOException.", e);
            return null;
        }
    }
}
