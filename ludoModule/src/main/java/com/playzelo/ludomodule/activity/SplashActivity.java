package com.playzelo.ludomodule.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.playzelo.ludomodule.R;
import com.playzelo.ludomodule.databinding.ActivityLudoSplashBinding;

public class SplashActivity extends AppCompatActivity {

    private ActivityLudoSplashBinding binding;
    private String userId, username, authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLudoSplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        binding.logo.startAnimation(slideDown);

        // Fade in the app name
        binding.appName.animate()
                .alpha(1f)
                .setDuration(1500)
                .setStartDelay(1000)
                .start();

        // Start Lottie animations
        binding.ludoDiceAnim.playAnimation();
        binding.boyRunningAnim.playAnimation();

        // Auto-stop Lottie animations after 3 seconds
        new Handler().postDelayed(() -> {
            binding.ludoDiceAnim.cancelAnimation();
            binding.boyRunningAnim.cancelAnimation();
        }, 3000);

        // Get user data from Intent extras (from parent app)
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("userId");
            authToken = intent.getStringExtra("auth_token");
            username = intent.getStringExtra("username");


            if (userId != null && authToken != null) {
                // यह टोस्ट सही से नाम दिखा रहा होगा।
                Toast.makeText(this, "Ludo started for user: " + username, Toast.LENGTH_LONG).show();
            } else {
                Log.e("ludoModule", "User data missing from parent Intent.");
                Toast.makeText(this, "Error: User data missing!", Toast.LENGTH_SHORT).show();
            }
        }

        // Move to MainActivity after 3 seconds, pass the same Intent extras
        new Handler().postDelayed(() -> {
            Intent next = new Intent(SplashActivity.this, MainActivity.class);
            if (userId != null) next.putExtra("userId", userId);
            if (authToken != null) next.putExtra("auth_token", authToken);
            if (username != null) next.putExtra("username", username);
            startActivity(next);
            finish();
        }, 3000);
    }
}
