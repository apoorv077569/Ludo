package com.playzelo.ludo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class SplashActivity extends AppCompatActivity {

    private TextView appName;
    private ImageView logo;
    private LottieAnimationView diceAnim, boyAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Ensure this is your updated splash layout

        // Initialize views
        appName = findViewById(R.id.appName);
        logo = findViewById(R.id.logo);
        diceAnim = findViewById(R.id.ludoDiceAnim);
        boyAnim = findViewById(R.id.boyRunningAnim);

        // Optional: Logo animation
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        logo.startAnimation(slideDown);

        // Fade in the app name
        appName.animate()
                .alpha(1f)
                .setDuration(1500)
                .setStartDelay(1000)
                .start();

        // Ensure both animations start
        diceAnim.playAnimation();
        boyAnim.playAnimation();

        // Auto-stop Lottie animations after 6 sec (optional)
        new Handler().postDelayed(() -> {
            diceAnim.cancelAnimation();
            boyAnim.cancelAnimation();
        }, 6000);

        // Move to next screen after 6 seconds
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 6000);
    }
}
