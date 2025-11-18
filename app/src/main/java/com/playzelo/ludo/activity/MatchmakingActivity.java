package com.playzelo.ludo.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.playzelo.ludo.databinding.ActivityMatchmakingBinding;
import com.playzelo.ludomodule.activity.GameRoomActivity;
import com.playzelo.ludomodule.apiservice.LudoApiHelper;
import com.playzelo.ludomodule.databinding.FourPlayerMatchmakingBinding;
import com.playzelo.ludomodule.models.LudoRoomResponse;
import com.playzelo.ludomodule.models.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchmakingActivity extends AppCompatActivity {

    private ActivityMatchmakingBinding binding;
    private FourPlayerMatchmakingBinding fourPlayerBinding;

    private String username, userId, auth_token;
    private int playerCount = 2;
    private String playerType;
    private String roomId;
    private double entryFee, winPrize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extractIntentData();

        if (roomId != null && !roomId.isEmpty()) {
            Log.d("MatchmakingActivity", "Room ID provided: " + roomId);
            fetchRoomDetails();
        } else {
            Log.d("MatchmakingActivity", "Starting automatch...");

        }
    }

    private void extractIntentData() {
        if (getIntent() != null) {
            userId = getIntent().getStringExtra("userId");
            username = getIntent().getStringExtra("username");
            auth_token = getIntent().getStringExtra("auth_token");
            playerType = getIntent().getStringExtra("type");
            playerCount = getIntent().getIntExtra("playerCount", 2);
            roomId = getIntent().getStringExtra("roomId");
            entryFee = getIntent().getDoubleExtra("entryFee", 0);
            winPrize = getIntent().getDoubleExtra("winPrize", 0);
            Log.d("MatchmakingActivity", "Intent data - UserID: " + userId + ", PlayerType: " + playerType + ", PlayerCount: " + playerCount);
        }
    }


    private void fetchRoomDetails() {
        if (roomId == null || roomId.isEmpty()) {
            Log.e("MatchmakingActivity", "Room ID is null or empty");
            handleApiFailure("Room ID not found");
            return;
        }

        Log.d("MatchmakingActivity", "Fetching room details for Room ID: " + roomId);

        LudoApiHelper ludoApiHelper = LudoApiHelper.getInstance(auth_token);
        ludoApiHelper.getGameById(roomId, "Bearer " + auth_token, new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LudoRoomResponse> call, @NonNull Response<LudoRoomResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LudoRoomResponse roomResponse = response.body();
                    List<Player> players = roomResponse.getPlayers();

                    Log.d("MatchmakingActivity", "Room details fetched successfully");
                    Log.d("MatchmakingActivity", "Room Status: " + roomResponse.getStatus());
                    Log.d("MatchmakingActivity", "Players count: " + (players != null ? players.size() : 0));

                    initializeUI(players);

                    if (roomResponse.getEntryFee() == 0) roomResponse.setEntryFee(entryFee);
                    if (roomResponse.getWinPrize() == 0) roomResponse.setWinPrize(winPrize);

                    redirectToGameRoom(roomResponse);

                } else {
                    try {
                        Log.e("API_CALL", "Get game by ID failed. Code: " + response.code() +
                                ", message: " + response.message() +
                                ", errorBody: " + (response.errorBody() != null ? response.errorBody().string() : "null"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    handleApiFailure("Failed to fetch room details: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<LudoRoomResponse> call, @NonNull Throwable t) {
                Log.e("MatchmakingActivity", "Get game by ID network error: " + t.getMessage(), t);
                handleApiFailure("Network error while fetching room details.");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initializeUI(List<Player> players) {
        if (players == null || players.isEmpty()) {
            Log.w("MatchmakingActivity", "No players data available");
            return;
        }

        if (playerType.equals("2p")) {
            binding = ActivityMatchmakingBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            Player opponent = null;

            for (Player player : players) {
                if (!player.getUserId().equals(userId)) {
                    opponent = player;
                    break;
                }
            }

            // Always set self
            binding.tvYouName.setText("You\n" + userId);
            Log.d("MatchmakingActivity", "You: " + userId);

            if (opponent != null) {
                binding.tvOpponentName.setText("Opponent\n" + opponent.getUserId());
                Log.d("MatchmakingActivity", "Opponent: " + opponent.getUserId());
            } else {
                binding.tvOpponentName.setText("Waiting for opponent...");
                Log.w("MatchmakingActivity", "Opponent not found in players list");
            }

        } else {
            // 4-player UI logic (can be updated same way if needed)
            fourPlayerBinding = FourPlayerMatchmakingBinding.inflate(getLayoutInflater());
            setContentView(fourPlayerBinding.getRoot());

            Player currentUser = null;
            List<Player> opponents = new ArrayList<>();

            for (Player player : players) {
                if (player.getUserId().equals(userId)) {
                    currentUser = player;
                } else {
                    opponents.add(player);
                }
            }

            fourPlayerBinding.tvPlayer1Name.setText("You\n" + userId);
            Log.d("MatchmakingActivity", "You: " + userId);

            if (!opponents.isEmpty()) {
                fourPlayerBinding.tvOpponentName1.setText("Opponent1\n" + opponents.get(0).getUserId());
                Log.d("MatchmakingActivity", "Opponent1: " + opponents.get(0).getUserId());
            }
            if (opponents.size() >= 2) {
                fourPlayerBinding.tvOpponentName2.setText("Opponent2\n" + opponents.get(1).getUserId());
                Log.d("MatchmakingActivity", "Opponent2: " + opponents.get(1).getUserId());
            }
            if (opponents.size() >= 3) {
                fourPlayerBinding.tvOpponentName3.setText("Opponent3\n" + opponents.get(2).getUserId());
                Log.d("MatchmakingActivity", "Opponent3: " + opponents.get(2).getUserId());
            }
        }

        Log.d("MatchmakingActivity", "UI initialized for " + playerType + " game");
    }

    private void redirectToGameRoom(LudoRoomResponse roomResponse) {
        Log.d("MatchmakingActivity", "Preparing to redirect to game room...");

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MatchmakingActivity.this, GameRoomActivity.class);

            intent.putExtra("userId", userId);
            intent.putExtra("username", username);
            intent.putExtra("auth_token", auth_token);
            intent.putExtra("roomId", roomResponse.getRoomId());
            intent.putExtra("playerCount", playerCount);
            intent.putExtra("playerType", playerType);

            intent.putExtra("entryFee", roomResponse.getEntryFee());
            intent.putExtra("winPrize", roomResponse.getWinPrize());
            intent.putExtra("gameStatus", roomResponse.getStatus());

            ArrayList<String> playerIds = new ArrayList<>();
            ArrayList<String> playerColors = new ArrayList<>();

            if (roomResponse.getPlayers() != null) {
                for (Player player : roomResponse.getPlayers()) {
                    playerIds.add(player.getUserId());
                    playerColors.add(player.getColor());
                }
            }

            intent.putStringArrayListExtra("playerIds", playerIds);
            intent.putStringArrayListExtra("playerColors", playerColors);

            Log.d("MatchmakingActivity", "Redirecting to GameRoomActivity with " + playerIds.size() + " players");

            startActivity(intent);
            finish();
        }, 3000);
    }

    private void handleApiFailure(String message) {
        Log.e("MatchmakingActivity", "API Failure: " + message);
        Toast.makeText(MatchmakingActivity.this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MatchmakingActivity", "Activity destroyed");

        if (playerCount == 4) {
            fourPlayerBinding = null;
        } else {
            binding = null;
        }
    }
}
