package com.playzelo.ludo.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.playzelo.ludo.R;
import com.playzelo.ludo.databinding.ActivityMainBinding;
import com.playzelo.ludo.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private String username,email,token,avatar;
    private String id;
    private String photo;

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
            id = intent.getStringExtra("userId");
            photo = intent.getStringExtra("photo");

        }

        Log.d("UserData", "Username: " + username);
        Log.d("UserData", "Email: " + email);
        Log.d("UserData", "Token: " + token);
        Log.d("UserData", "Id: " + id);
        Log.d("UserData", "Photo: " + photo);



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

        SessionManager session = new SessionManager(this);

        if (!session.isSessionValid()) {
            Log.e("UserData", "❌ Session expired. Redirecting to login.");
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }


        binding.btnPlayNow.setOnClickListener(v -> {
            Intent next = new Intent(MainActivity.this, TournamentsActivity.class);
            next.putExtra("username",username);
            next.putExtra("token",token);
            next.putExtra("email",email);
            next.putExtra("userId",id);
            next.putExtra("photo",photo);
            startActivity(next);
        });
        binding.btnLogout.setOnClickListener(v-> redirectToLogin());
    }

    private void redirectToLogin() {
        SessionManager session = new SessionManager(this);
        session.logout();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }



}
