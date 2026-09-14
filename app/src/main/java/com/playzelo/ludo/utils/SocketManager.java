package com.playzelo.ludo.utils;

import android.util.Log;

import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketManager {

    private static final String TAG = "SocketManager";
    private static final String SERVER_URL = "https://ludo-plum.vercel.app/";

    private static Socket socket;

    public static Socket getSocket() {
        if (socket == null) {
            try {
                IO.Options options = new IO.Options();
                options.forceNew = false;  // 🔥 Reuse connection
                options.reconnection = true;
                options.reconnectionAttempts = 5;
                options.reconnectionDelay = 1000;

                socket = IO.socket(SERVER_URL, options);
                Log.d(TAG, "Socket instance created");
            } catch (URISyntaxException e) {
                Log.e(TAG, "Socket URI error: " + e.getMessage());
            }
        }
        return socket;
    }

    public static void connect() {
        if (socket != null && !socket.connected()) {
            socket.connect();
            Log.d(TAG, "Socket connecting...");
        }
    }

    public static void disconnect() {
        if (socket != null) {
            socket.disconnect();
            Log.d(TAG, "Socket disconnected");
        }
    }

    public static boolean isConnected() {
        return socket != null && socket.connected();
    }

    // 🔥 IMPORTANT: Reset socket when needed
    public static void reset() {
        if (socket != null) {
            socket.disconnect();
            socket.off();  // Remove all listeners
            socket = null;
        }
    }
}
