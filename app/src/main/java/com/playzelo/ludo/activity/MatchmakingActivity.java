package com.playzelo.ludo.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.playzelo.ludo.databinding.ActivityMatchmakingBinding;
import com.playzelo.ludo.databinding.FourPlayerMatchmakingBinding;


public class MatchmakingActivity extends AppCompatActivity {

    private ActivityMatchmakingBinding binding;
    private FourPlayerMatchmakingBinding fourPlayerMatchmakingBinding;

    private String username, authToken;
    private int playerCount;
    private String playerType;
    private String roomId;
    private double entryFee, winPrize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extractIntentData();

        if ("2p".equals(playerType)) {
            binding = ActivityMatchmakingBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            setupTwoPlayerUI();

        } else if ("4p".equals(playerType)) {
            fourPlayerMatchmakingBinding =
                    FourPlayerMatchmakingBinding.inflate(getLayoutInflater());
            setContentView(fourPlayerMatchmakingBinding.getRoot());
            setupFourPlayerUI();
        }
    }


    private void extractIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            authToken = intent.getStringExtra("token");
            playerType = intent.getStringExtra("type");
            playerCount = intent.getIntExtra("playerCount", 2);
            roomId = intent.getStringExtra("roomId");
            entryFee = intent.getDoubleExtra("entryFee", 0);
            winPrize = intent.getDoubleExtra("winPrize", 0);
        }

        Log.d("MatchmakingActivity",
                "username=" + username +
                        " type=" + playerType +
                        " count=" + playerCount);
    }
    private void setupTwoPlayerUI() {
        binding.tvYouName.setText(username);
        binding.tvOpponentName.setText("Waiting for opponent...");
        redirectToGameRoomAfterDelay();
        Log.d("MatchmakingActivity", "2 Player UI Loaded");
    }

    @SuppressLint("SetTextI18n")
    private void setupFourPlayerUI() {

        fourPlayerMatchmakingBinding.tvPlayer1Name
                .setText(username);

        fourPlayerMatchmakingBinding.tvOpponentName1
                .setText("Waiting...");

        fourPlayerMatchmakingBinding.tvOpponentName2
                .setText("Waiting...");

        fourPlayerMatchmakingBinding.tvOpponentName3
                .setText("Waiting...");

        redirectToGameRoomAfterDelay();

        Log.d("MatchmakingActivity", "4 Player UI Loaded");
    }
    private void redirectToGameRoomAfterDelay() {

        new Handler().postDelayed(() -> {

            Intent intent = new Intent(
                    MatchmakingActivity.this,
                    GameRoomActivity.class
            );

            intent.putExtra("username", username);
            intent.putExtra("token", authToken);
            intent.putExtra("roomId", roomId);
            intent.putExtra("playerCount", playerCount);
            intent.putExtra("type", playerType);
            intent.putExtra("entryFee", entryFee);
            intent.putExtra("winPrize", winPrize);

            startActivity(intent);
            finish();

        }, 5000); // ⏱️ 5 seconds
    }

}
