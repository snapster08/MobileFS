package mfs.network;

import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            try {
                DataInputStream in = new DataInputStream(mRequestSocket.getInputStream());
                String requestMessage = in.readUTF();
                Log.i(LOG_TAG, "Received Request: " +requestMessage);
            }
            catch (IOException e) {
                Log.e(LOG_TAG, "IOException while trying read from request socket.", e);
            }
        }
    }
}
