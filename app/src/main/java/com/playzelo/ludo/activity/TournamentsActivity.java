package com.playzelo.ludo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.playzelo.ludo.R;
import com.playzelo.ludo.databinding.ActivityTournamentsBinding;


public class TournamentsActivity extends AppCompatActivity {
    ActivityTournamentsBinding binding;

    private TextView tabAll, tabRegular, tab2Players;
    private LinearLayout layoutRecommendedTournaments, layoutOtherTournaments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTournamentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        onClickListener();
    }

    private void onClickListener(){
        binding.btnTwoPlayer.setOnClickListener(view -> {
            Intent intent = new Intent(TournamentsActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        });
        binding.btnFourPlayer.setOnClickListener(view -> {
            Intent intent = new Intent(TournamentsActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }




    // ✅ Back press to show main content again
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            findViewById(R.id.mainContentLayout).setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
