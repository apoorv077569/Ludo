package com.playzelo.ludomodule.activity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.playzelo.ludomodule.R;
import com.playzelo.ludomodule.databinding.ActivityLudoMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityLudoMainBinding binding;
    private String userId, username;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLudoMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Retrieve user data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("userId");
            authToken = intent.getStringExtra("auth_token");
            username = intent.getStringExtra("username");
        }

        // Background Zoom Animation
        Animation zoomAnim = AnimationUtils.loadAnimation(this, R.anim.zoom_in_out);
        binding.bgImage.startAnimation(zoomAnim);

        showTermsPopup();
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

        // Lottie Dice Animation
        binding.lottieDice.playAnimation();

        // Play Now Button
        Button btnPlayNow = findViewById(R.id.btnPlayNow);
        btnPlayNow.setOnClickListener(v -> {
            Intent next = new Intent(MainActivity.this, TournamentsActivity.class);

            // Pass user credentials forward
            if (userId != null) next.putExtra("userId", userId);
            if (authToken != null) next.putExtra("auth_token", authToken);
            if (username != null) next.putExtra("username", username);

            startActivity(next);
        });
    }

    @SuppressLint("SetTextI18n")
    private void showTermsPopup() {
        // Create TextView with scrollable terms
        TextView termsText = getTextView();

        // Put inside ScrollView
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(termsText);

        // Build Dialog
        new AlertDialog.Builder(this)
                .setTitle("Terms & Conditions")
                .setView(scrollView)
                .setCancelable(false)
                .setPositiveButton("I Agree", (dialog, which) -> {
                    dialog.dismiss(); // proceed with game
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    finish(); // exit app or activity
                })
                .show();
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    private TextView getTextView() {
        TextView termsText = new TextView(this);
        termsText.setText(
                "📌 Ludo Game – Terms & Conditions\n\n" +
                        "1. You must be 18+ and not from restricted states.\n" +
                        "2. This is a real money game and involves financial risk. Play responsibly.\n" +
                        "3. Fair play only – cheating or multiple accounts may lead to suspension.\n" +
                        "4. Winnings are withdrawable to verified bank/UPI accounts (KYC & taxes apply).\n\n" +
                        "👉 By tapping 'I Agree', you accept our full Terms & Conditions."
        );
        termsText.setPadding(40, 30, 40, 30);
        termsText.setTextSize(15);
        return termsText;
    }


}

