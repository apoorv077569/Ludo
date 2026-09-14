package com.playzelo.ludo.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.playzelo.ludo.R;
import com.playzelo.ludo.databinding.ActivityMatchmakingBinding;
import com.playzelo.ludo.databinding.FourPlayerMatchmakingBinding;
import com.playzelo.ludo.models.RoomDto;
import com.playzelo.ludo.utils.SessionManager;
import com.playzelo.ludo.utils.SocketManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;


public class MatchmakingActivity extends AppCompatActivity {

    private ActivityMatchmakingBinding binding;
    private FourPlayerMatchmakingBinding fourPlayerMatchmakingBinding;

    private String username, authToken, photo;
    private int playerCount;
    private static final String TAG = "MatchmakingActivity";
    private String playerType;
    private String id, roomId;
    private Socket mSocket;
    private boolean isGameStarting = false;
    private boolean hasJoined = false;

    RoomDto room;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Matchmaking Activity Start");
        extractIntentData();

        if ("2p".equals(playerType)) {
            binding = ActivityMatchmakingBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            setupTwoPlayerUI();

        } else if ("4p".equals(playerType)) {
            fourPlayerMatchmakingBinding = FourPlayerMatchmakingBinding.inflate(getLayoutInflater());
            setContentView(fourPlayerMatchmakingBinding.getRoot());
            setupFourPlayerUI();
        }
        connectToSocket();
    }

//    private void connectToSocket() {
//        try {
//            String serverUrl = "https://ludo-plum.vercel.app/";
//            Log.d(TAG, "Connecting to socket: " + serverUrl);
//
//            mSocket = IO.socket(serverUrl);
//
//            mSocket.on(Socket.EVENT_CONNECT, args -> {
//                if (hasJoined) return;
//                hasJoined = true;
//
//                Log.d(TAG, "Socket Connected! Id: " + mSocket.id());
//                JSONObject joinData = new JSONObject();
//                try {
//                    joinData.put("userId", id);
//                    joinData.put("type", playerCount);
//                    joinData.put("username", username);
//                    joinData.put("avtar", photo);
//
//                    if (roomId != null && !roomId.isEmpty()) {
//                        joinData.put("roomId", roomId);
//                        Log.d(TAG, "Joining EXISTING room: " + roomId);
//                    } else {
//                        Log.d(TAG, "Searching for NEW room...");
//                    }
//
//                    Log.d(TAG, "Emmiting 'joingame': " + joinData);
//                    mSocket.emit("joingame", joinData);
//                } catch (JSONException e) {
//                    Log.e(TAG, "JSON Error in joingame: " + e.getLocalizedMessage());
//                }
//            });
//            mSocket.on("roomUpdate", args -> runOnUiThread(() -> {
//                try {
//                    JSONObject data = (JSONObject) args[0];
//                    roomId = data.getString("roomId");
//                    String status = data.getString("status");
//                    JSONArray players = data.getJSONArray("players");
//
//                    Log.d(TAG, "Received 'roomUpdate' -> RoomId:" + roomId + "| Status: " + status + " | Players: " + players.length());
//                    updateUI(players);
//                    if ("full".equals(status)
//                            && !isGameStarting
//                            && id.equals(players.getJSONObject(0).getString("userId"))) {
//                        Log.d(TAG, "Host starting game");
//                        isGameStarting = true;
//                        new android.os.Handler().postDelayed(() -> {
//                                try{
//                                    JSONObject startData = new JSONObject();
//                                    startData.put("roomId", roomId);
//                                    mSocket.emit("startGame", startData);
//                                }catch(Exception e){
//                                    Toast.makeText(this, "Failed to start game due to: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                                }
//                            },500);
//                    }
//                } catch (JSONException e) {
//                    Log.e(TAG, "Error parsing roomUpdate: " + e.getLocalizedMessage());
//                }
//            }));
//            mSocket.on("gameStarted", args -> runOnUiThread(() -> {
//                try {
//                    JSONObject gameObj = (JSONObject) args[0];
//                    String gameId = gameObj.getString("_id");
//                    Log.d(TAG, "Received 'gameStarted' Event!");
//                    Log.d(TAG, "Game Data: " + gameId);
//                    Log.d(TAG, "Game Data: " + gameObj);
//
//                    navigateToGameRoom(gameObj);
//                } catch (Exception e) {
//                    Log.e(TAG, "Error in gameStarted handler: " + e.getLocalizedMessage());
//                }
//            }));
//            mSocket.on("error", args -> runOnUiThread(() -> {
//                Log.e(TAG, "Server error: " + args[0]);
//                Toast.makeText(this, "Error: " + args[0], Toast.LENGTH_SHORT).show();
//            }));
//            mSocket.on("leftRoom", args -> {
//                Log.d(TAG, "Left room Event Received");
//                runOnUiThread(this::finish);
//            });
//            mSocket.connect();
//        } catch (URISyntaxException e) {
//            Log.e(TAG, "URI Syntax error: " + e.getLocalizedMessage());
//        }
//    }

    private void connectToSocket() {
        Log.d(TAG, "Connecting to socket via SocketManager...");

        // 🔥 USE SOCKET MANAGER INSTEAD
        mSocket = SocketManager.getSocket();

        // 🔥 Clear old listeners first
        mSocket.off();

        mSocket.on(Socket.EVENT_CONNECT, args -> {
            if (hasJoined) return;
            hasJoined = true;

            Log.d(TAG, "Socket Connected! Id: " + mSocket.id());
            JSONObject joinData = new JSONObject();
            try {
                joinData.put("userId", id);
                joinData.put("type", playerCount);
                joinData.put("username", username);
                joinData.put("avtar", photo);

                if (roomId != null && !roomId.isEmpty()) {
                    joinData.put("roomId", roomId);
                    Log.d(TAG, "Joining EXISTING room: " + roomId);
                } else {
                    Log.d(TAG, "Searching for NEW room...");
                }

                Log.d(TAG, "Emitting 'joingame': " + joinData);
                mSocket.emit("joingame", joinData);
            } catch (JSONException e) {
                Log.e(TAG, "JSON Error in joingame: " + e.getLocalizedMessage());
            }
        });

        mSocket.on("roomUpdate", args -> runOnUiThread(() -> {
            try {
                JSONObject data = (JSONObject) args[0];
                roomId = data.getString("roomId");
                String status = data.getString("status");
                JSONArray players = data.getJSONArray("players");

                Log.d(TAG, "Received 'roomUpdate' -> RoomId:" + roomId + "| Status: " + status + " | Players: " + players.length());
                updateUI(players);
                if ("full".equals(status)
                        && !isGameStarting
                        && id.equals(players.getJSONObject(0).getString("userId"))) {
                    Log.d(TAG, "Host starting game");
                    isGameStarting = true;
                    new android.os.Handler().postDelayed(() -> {
                        try {
                            JSONObject startData = new JSONObject();
                            startData.put("roomId", roomId);
                            mSocket.emit("startGame", startData);
                        } catch (Exception e) {
                            Toast.makeText(this, "Failed to start game: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }, 500);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing roomUpdate: " + e.getLocalizedMessage());
            }
        }));

        mSocket.on("gameStarted", args -> runOnUiThread(() -> {
            try {
                JSONObject gameObj = (JSONObject) args[0];
                String gameId = gameObj.getString("_id");
                Log.d(TAG, "Received 'gameStarted' Event!");
                Log.d(TAG, "Game Data: " + gameId);

                // 🔥 DON'T disconnect - keep socket alive!
                navigateToGameRoom(gameObj);
            } catch (Exception e) {
                Log.e(TAG, "Error in gameStarted handler: " + e.getLocalizedMessage());
            }
        }));

        mSocket.on("error", args -> runOnUiThread(() -> {
            Log.e(TAG, "Server error: " + args[0]);
            Toast.makeText(this, "Error: " + args[0], Toast.LENGTH_SHORT).show();
        }));

        mSocket.on("leftRoom", args -> {
            Log.d(TAG, "Left room Event Received");
            runOnUiThread(this::finish);
        });

        // 🔥 Connect via SocketManager
        SocketManager.connect();
    }

    private void updateUI(JSONArray players) throws JSONException {
        ArrayList<JSONObject> opponents = new ArrayList<>();
        for (int i = 0; i < players.length(); i++) {
            JSONObject p = players.getJSONObject(i);
            if (!p.getString("userId").equals(id)) {
                opponents.add(p);
            }
        }
        if (playerCount == 2 && !opponents.isEmpty()) {
            JSONObject opp = opponents.get(0);
            String name = opp.getString("username");
            String avatar = opp.optString("avtar", "");

            binding.tvOpponentName.setText(name);

            Glide.with(this).load(avatar).placeholder(R.drawable.ic_user_placeholder).into(binding.imgOpponent);

            Log.d(TAG, "Opponent Name: " + opponents.get(0).getString("username"));
            Log.d(TAG, "Opponent Avatar: " + avatar);
        }
    }

    //    private void navigateToGameRoom(JSONObject gameObj) throws JSONException {
//
//        Log.d(TAG, "Redirecting to GameRoomActivity...");
//
//        Intent intent = new Intent(MatchmakingActivity.this, GameRoomActivity.class);
//
//        intent.putExtra("roomId", gameObj.getString("roomId"));
//        intent.putExtra("userId", id);
//        intent.putExtra("username", username);
//        intent.putExtra("token", authToken);
//        intent.putExtra("photo", photo);
//        intent.putExtra("gameId", gameObj.getString("_id"));
//
//        int currentPlayerIndex = gameObj.optInt("currentPlayerIndex",0);
//        intent.putExtra("currentPlayerIndex",currentPlayerIndex);
//
//
//        JSONArray playersArray = gameObj.getJSONArray("players");
//
//        ArrayList<String> playersIds = new ArrayList<>();
//        ArrayList<String> playersColors = new ArrayList<>();
//        ArrayList<String> playersNames = new ArrayList<>();
//
//        String firstPlayerTurnId = "";
//        if(currentPlayerIndex < playersArray.length()){
//            firstPlayerTurnId = playersArray.getJSONObject(currentPlayerIndex).getString("userId");
//        }
//        intent.putExtra("firstPlayerTurnId",firstPlayerTurnId);
//
//        Log.d(TAG, "Processing " + playersArray.length() + " players for GameRoom:");
//
//        // ✅ ONLY collect data here
//        for (int i = 0; i < playersArray.length(); i++) {
//
//            JSONObject p = playersArray.getJSONObject(i);
//
//            String pId = p.getString("userId");
//            String pColor = p.optString("color");
//            String pName = p.getString("username");
//
//
//            playersIds.add(pId);
//            playersColors.add(pColor);
//            playersNames.add(pName);
//
//
//            Log.d(TAG, "👉 Player " + i + ": " + pId + " | Color: " + pColor);
//        }
//
//        intent.putStringArrayListExtra("playerIds", playersIds);
//        intent.putStringArrayListExtra("playerColors", playersColors);
//        intent.putStringArrayListExtra("playerNames", playersNames);
//
//        startActivity(intent);
//        finish();
//    }

    private void navigateToGameRoom(JSONObject gameObj) throws JSONException {

        Log.d(TAG, "═══════════════════════════════════════");
        Log.d(TAG, "🎮 GAME STARTED EVENT RECEIVED");
        Log.d(TAG, "═══════════════════════════════════════");

        // 🔥 LOG FULL GAME OBJECT
        try {
            Log.d(TAG, "📦 Full Game Object:");
            Log.d(TAG, gameObj.toString(2)); // Pretty print with indent
        } catch (JSONException e) {
            Log.d(TAG, "Game Object: " + gameObj.toString());
        }

        Log.d(TAG, "═══════════════════════════════════════");

        Log.d(TAG, "Redirecting to GameRoomActivity...");

        Intent intent = new Intent(MatchmakingActivity.this, GameRoomActivity.class);

        intent.putExtra("roomId", gameObj.getString("roomId"));
        intent.putExtra("userId", id);
        intent.putExtra("username", username);
        intent.putExtra("token", authToken);
        intent.putExtra("photo", photo);
        intent.putExtra("gameId", gameObj.getString("_id"));

        // 🔥 SAFE EXTRACTION - Check if fields exist
        int currentPlayerIndex = gameObj.optInt("currentPlayerIndex", -1);
        String firstTurnPlayerId = null;

        JSONArray playersArray = gameObj.getJSONArray("players");

        // Try to get turn info from different possible fields
        if (currentPlayerIndex >= 0 && currentPlayerIndex < playersArray.length()) {
            JSONObject firstPlayer = playersArray.getJSONObject(currentPlayerIndex);
            firstTurnPlayerId = firstPlayer.optString("userId", "");
            Log.d(TAG, "✅ Found turn from currentPlayerIndex: " + currentPlayerIndex);
        }
        else if (gameObj.has("currentTurn")) {
            // Alternative: server might send "currentTurn"
            firstTurnPlayerId = gameObj.optString("currentTurn", "");
            Log.d(TAG, "✅ Found turn from currentTurn field");
        }
        else if (gameObj.has("turn")) {
            // Alternative: server might send "turn"
            firstTurnPlayerId = gameObj.optString("turn", "");
            Log.d(TAG, "✅ Found turn from turn field");
        }
        else {
            // Default: First player in array
            if (playersArray.length() > 0) {
                currentPlayerIndex = 0;
                firstTurnPlayerId = playersArray.getJSONObject(0).optString("userId", "");
                Log.d(TAG, "⚠️ No turn info, defaulting to first player");
            }
        }

        intent.putExtra("currentPlayerIndex", currentPlayerIndex);
        intent.putExtra("firstTurnPlayerId", firstTurnPlayerId);

        Log.d(TAG, "🎯 Passing Initial Turn:");
        Log.d(TAG, "   Index: " + currentPlayerIndex);
        Log.d(TAG, "   Player ID: " + firstTurnPlayerId);

        ArrayList<String> playersIds = new ArrayList<>();
        ArrayList<String> playersColors = new ArrayList<>();
        ArrayList<String> playersNames = new ArrayList<>();

        for (int i = 0; i < playersArray.length(); i++) {
            JSONObject p = playersArray.getJSONObject(i);

            String pId = p.getString("userId");
            String pColor = p.optString("color");
            String pName = p.getString("username");

            playersIds.add(pId);
            playersColors.add(pColor);
            playersNames.add(pName);

            Log.d(TAG, "👉 Player " + i + ": " + pName + " (" + pColor + ") | ID: " + pId);
        }

        intent.putStringArrayListExtra("playerIds", playersIds);
        intent.putStringArrayListExtra("playerColors", playersColors);
        intent.putStringArrayListExtra("playerNames", playersNames);

        Log.d(TAG, "═══════════════════════════════════════");

        startActivity(intent);
        finish();
    }
    private void extractIntentData() {

        SessionManager session = new SessionManager(this);
        Intent intent = getIntent();

        if (intent != null) {
            username = intent.getStringExtra("username");
            id = intent.getStringExtra("userId");
            authToken = intent.getStringExtra("token");
            room = (RoomDto) intent.getSerializableExtra("room");
            photo = intent.getStringExtra("photo");
        }

        // 🔥 FALLBACK TO SESSION (MOST IMPORTANT)
        if (username == null || username.isEmpty()) {
            username = session.getUsername();
        }

        if (authToken == null || authToken.isEmpty()) {
            authToken = session.getToken();
        }

        if (id == null || id.isEmpty()) {
            // TEMP fallback (better: store userId in session)
            id = session.getEmail();
        }

        if (room != null) {
            roomId = room.getRoomId();
            playerCount = room.getType();
            playerType = room.getType() == 2 ? "2p" : "4p";
        }

        Log.d(TAG, "username=" + username + " userId=" + id + " type=" + playerType + " count=" + playerCount);

        Log.d("UserDataMA", "Token: " + authToken);
        Log.d("UserDataMA", "Photo " + photo);
    }

    @SuppressLint("SetTextI18n")
    private void setupTwoPlayerUI() {
        binding.tvYouName.setText(username);
        Glide.with(this).load(photo).circleCrop().placeholder(R.drawable.ic_user_placeholder).into(binding.imgYou);
        binding.tvOpponentName.setText("Waiting for opponent...");
        Log.d("MatchmakingActivity", "2 Player UI Loaded");
    }

    @SuppressLint("SetTextI18n")
    private void setupFourPlayerUI() {

        fourPlayerMatchmakingBinding.tvPlayer1Name.setText(username);

        fourPlayerMatchmakingBinding.tvOpponentName1.setText("Waiting...");

        fourPlayerMatchmakingBinding.tvOpponentName2.setText("Waiting...");

        fourPlayerMatchmakingBinding.tvOpponentName3.setText("Waiting...");


        Log.d("MatchmakingActivity", "4 Player UI Loaded");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "💀 MatchmakingActivity Destroyed");
        super.onDestroy();
        hasJoined = false;
        if (mSocket != null) {
            mSocket.off("roomUpdate");
            mSocket.off("gameStarted");
            mSocket.off("leftRoom");
        }
    }
}
