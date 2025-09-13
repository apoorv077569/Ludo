package com.playzelo.ludo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.playzelo.ludo.R;

public class CountdownActivity extends AppCompatActivity {

    private LottieAnimationView lottieView;
    private MediaPlayer tickSound;
    private TextView tvStartingGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ludo_countdown);

        lottieView = findViewById(R.id.lottieView);
        tvStartingGame = findViewById(R.id.tvStartingGame);
        tvStartingGame.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse_scale));


        // 🌈 Apply gradient shader to the TextView
        Shader textShader = new LinearGradient(
                0, 0, 0, tvStartingGame.getTextSize(),
                new int[]{
                        Color.parseColor("#FFD700"),  // Gold
                        Color.parseColor("#FF8C00")   // Orange
                }, null, Shader.TileMode.CLAMP);
        tvStartingGame.getPaint().setShader(textShader);

        // 🔊 Setup tick sound
        tickSound = MediaPlayer.create(this, R.raw.tick); // Place tick.mp3 in res/raw/
        startTickingSound(6); // 6 ticks

        // 🎞️ Setup Lottie animation
        lottieView.setRepeatCount(0);
        lottieView.setAnimation(R.raw.countdown); // your renamed JSON file
        lottieView.playAnimation();

        // 🎯 Navigate after animation completes
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(CountdownActivity.this, MainActivity.class);
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
