package com.playzelo.ludomodule.activity;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.playzelo.ludomodule.databinding.ActivityOnlineLobbyBinding;

public class OnlineLobbyActivity extends AppCompatActivity {

    ActivityOnlineLobbyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnlineLobbyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String roomCode = binding.edtRoomCode.getText().toString();

        // Copy Room Code
        binding.btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Room Code", roomCode);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Room Code Copied!", Toast.LENGTH_SHORT).show();
        });

        // Share Room Code
        binding.btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Join my room! Code: " + roomCode);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        // Share to WhatsApp
        binding.btnWhatsapp.setOnClickListener(v -> {
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("text/plain");
            whatsappIntent.setPackage("com.whatsapp");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Join my room! Code: " + roomCode);
            try {
                startActivity(whatsappIntent);
            } catch (Exception e) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            }
        });

        // Back Button
        binding.btnBack.setOnClickListener(v -> finish());
    }
}