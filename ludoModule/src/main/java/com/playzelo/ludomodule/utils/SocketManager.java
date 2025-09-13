package com.playzelo.ludomodule.utils;

import android.util.Log;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.json.JSONObject;

import java.util.Arrays;

public class SocketManager {
    private static Socket mSocket;

    public static void initSocket(String serverUrl) {
        try {
            if (mSocket == null) {
                mSocket = IO.socket(serverUrl);
                Log.d("SocketManager", "Socket initialized with URL: " + serverUrl);
            }
        } catch (Exception e) {
            Log.e("SocketManager", "Error initializing socket: " + e.getMessage());
        }
    }

    public static void connect() {
        if (mSocket != null && !mSocket.connected()) {
            mSocket.connect();
            Log.d("SocketManager", "Socket connected");
        }
    }

    public static void disconnect() {
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
            Log.d("SocketManager", "Socket disconnected");
        }
    }

    public static void emit(String event, JSONObject data) {
        if (mSocket != null && mSocket.connected()) {
            mSocket.emit(event, data);
            Log.d("SocketManager", "Emit -> " + event + " : " + data.toString());
        } else {
            Log.e("SocketManager", "Emit failed, socket not connected: " + event);
        }
    }

    public static void on(String event, Emitter.Listener listener) {
        if (mSocket != null) {
            Log.d("SocketManager", "Listening for event: " + event);
            mSocket.on(event, args -> {
                Log.d("SocketManager", "Event received: " + event + " -> " + Arrays.toString(args));
                listener.call(args); // forward event to registered listener
            });
        }
    }

    public static void off(String event) {
        if (mSocket != null) {
            mSocket.off(event);
            Log.d("SocketManager", "Stopped listening for event: " + event);
        }
    }

    public static Socket getSocket() {
        return mSocket;
    }
}
