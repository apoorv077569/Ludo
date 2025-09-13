package com.playzelo.ludomodule.activity;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.playzelo.ludomodule.R;
import com.playzelo.ludomodule.databinding.ActivityLobbyBinding;

public class LobbyActivity extends AppCompatActivity {

    private ActivityLobbyBinding binding;


    private int entryCoins = 100;
    private String selectedMode = "6P"; // default

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLobbyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Default values
        binding.txtCoins.setText("Coins: 1115");
        binding.txtDiamonds.setText("Diamonds: 150");
        binding.txtEntryCoins.setText(String.valueOf(entryCoins));

        // Mode Selection
        binding.btn2p.setOnClickListener(v -> setMode("2P/3P/4P"));
        binding.btnTeamUp.setOnClickListener(v -> setMode("Team Up"));


        // Create/Join
        binding.btnCreate.setOnClickListener(v -> Toast.makeText(this, "Create Lobby Selected", Toast.LENGTH_SHORT).show());
        binding.btnJoin.setOnClickListener(v -> Toast.makeText(this, "Join Lobby Selected", Toast.LENGTH_SHORT).show());

        // Entry control
        binding.btnMinus.setOnClickListener(v -> {
            if (entryCoins > 50) {
                entryCoins -= 50;
                binding.txtEntryCoins.setText(String.valueOf(entryCoins));
            }
        });

        binding.btnPlus.setOnClickListener(v -> {
            entryCoins += 50;
             binding.txtEntryCoins.setText(String.valueOf(entryCoins));
        });

        // Next button
        binding.btnNext.setOnClickListener(v -> {
            // TODO: Navigate to actual game screen
        });
    }

    private void setMode(String mode) {
        selectedMode = mode;
        int selectedPlayers = getIntent().getIntExtra("selectedPlayers", 2);
// Default = 2
        Toast.makeText(this, "Lobby opened for " + selectedPlayers + " players", Toast.LENGTH_SHORT).show();

        Toast.makeText(this, "Selected Mode: " + mode, Toast.LENGTH_SHORT).show();
    }
}
