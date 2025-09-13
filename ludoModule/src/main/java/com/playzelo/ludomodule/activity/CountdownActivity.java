package com.playzelo.ludomodule.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.playzelo.ludomodule.R;
import com.playzelo.ludomodule.databinding.ActivityLudoCountdownBinding;

public class CountdownActivity extends AppCompatActivity {
    private MediaPlayer tickSound;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLudoCountdownBinding binding = ActivityLudoCountdownBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvStartingGame.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse_scale));


        // 🌈 Apply gradient shader to the TextView
        Shader textShader = new LinearGradient(
                0, 0, 0, binding.tvStartingGame.getTextSize(),
                new int[]{
                        Color.parseColor("#FFD700"),  // Gold
                        Color.parseColor("#FF8C00")   // Orange
                }, null, Shader.TileMode.CLAMP);
        binding.tvStartingGame.getPaint().setShader(textShader);

        // 🔊 Setup tick sound
        tickSound = MediaPlayer.create(this, R.raw.tick); // Place tick.mp3 in res/raw/
        startTickingSound(6); // 6 ticks

        // 🎞️ Setup Lottie animation
        binding.lottieView.setRepeatCount(0);
        binding.lottieView.setAnimation(R.raw.countdown); // your renamed JSON file
        binding.lottieView.playAnimation();

        // 🎯 Navigate after animation completes
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(CountdownActivity.this, GameRoomActivity.class);
            intent.putExtra("openFragment", "wallet");
            startActivity(intent);
            finish();
        }, 6000);
    }

    private void startTickingSound(int seconds) {
        Handler handler = new Handler();
        for (int i = 0; i < seconds; i++) {
            handler.postDelayed(() -> {
                if (tickSound != null) {
                    tickSound.start();
                }
            }, i * 1000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tickSound != null) {
            tickSound.release();
        }
    }
}
