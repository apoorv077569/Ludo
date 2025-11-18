package com.playzelo.ludo.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;
import com.playzelo.ludo.R;
import com.playzelo.ludo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private String username,email,token;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Retrieve user data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            email = intent.getStringExtra("email");
            token = intent.getStringExtra("token");
        }

        Log.d("UserData", "Username: " + username);
        Log.d("UserData", "Email: " + email);
        Log.d("UserData", "Token: " + token);


        // Background Zoom Animation
        Animation zoomAnim = AnimationUtils.loadAnimation(this, R.anim.zoom_in_out);
        binding.bgImage.startAnimation(zoomAnim);


        // Darken effect during zoom out
        ValueAnimator darkenAnimator = ValueAnimator.ofFloat(1f, 0.7f);
        darkenAnimator.setDuration(2000);
        darkenAnimator.setRepeatCount(ValueAnimator.INFINITE);
        darkenAnimator.setRepeatMode(ValueAnimator.REVERSE);

        darkenAnimator.addUpdateListener(animation -> {
            float brightness = (float) animation.getAnimatedValue();
            ColorMatrix cm = new ColorMatrix(new float[]{
                    brightness, 0, 0, 0, 0,
                    0, brightness, 0, 0, 0,
                    0, 0, brightness, 0, 0,
                    0, 0, 0, 1, 0
            });
            binding.bgImage.setColorFilter(new ColorMatrixColorFilter(cm));
        });
        darkenAnimator.start();

        binding.lottieDice.playAnimation();

        binding.btnPlayNow.setOnClickListener(v -> {
            Intent next = new Intent(MainActivity.this, TournamentsActivity.class);
            startActivity(next);
        });
    }
}
