package com.playzelo.ludo;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    private LottieAnimationView lottieDice;
    private ImageView bgImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Background Zoom Animation
        bgImage = findViewById(R.id.bgImage);
        Animation zoomAnim = AnimationUtils.loadAnimation(this, R.anim.zoom_in_out);
        bgImage.startAnimation(zoomAnim);

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
            bgImage.setColorFilter(new ColorMatrixColorFilter(cm));
        });
        darkenAnimator.start();

        // Lottie Dice Animation
        lottieDice = findViewById(R.id.lottieDice);
        if (lottieDice != null) {
            lottieDice.playAnimation();
        }

        // Play Now Button
        Button btnPlayNow = findViewById(R.id.btnPlayNow);
        btnPlayNow.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TournamentsActivity.class);
            startActivity(intent);
        });
    }
}
