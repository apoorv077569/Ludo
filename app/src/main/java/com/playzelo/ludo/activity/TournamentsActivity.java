package com.playzelo.ludo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.playzelo.ludo.R;
import com.playzelo.ludo.databinding.ActivityTournamentsBinding;


public class TournamentsActivity extends AppCompatActivity {
    ActivityTournamentsBinding binding;
    private String username,email,token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTournamentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        onClickListener();

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            email = intent.getStringExtra("email");
            token = intent.getStringExtra("token");
        }
    }

    private void onClickListener(){
        binding.btnTwoPlayer.setOnClickListener(view -> {
            Intent intent = new Intent(TournamentsActivity.this, MatchmakingActivity.class);
            intent.putExtra("playerCount", 2);
            intent.putExtra("type", "2p");
            intent.putExtra("roomId", ""); // no room yet
//          intent.putExtra("userId", userId);
            intent.putExtra("username", username);
            intent.putExtra("entryFee", 0);
            intent.putExtra("winPrize", 0);
            startActivity(intent);
            showToast("Two player selected");
        });
        binding.btnFourPlayer.setOnClickListener(view -> {
            Intent intent = new Intent(TournamentsActivity.this, MatchmakingActivity.class);
            intent.putExtra("playerCount", 4);
            intent.putExtra("type", "4p");
            intent.putExtra("roomId", "");
//            intent.putExtra("userId", userId);
           intent.putExtra("username", username);
            intent.putExtra("entryFee", 0);
            intent.putExtra("winPrize", 0);
            startActivity(intent);
            showToast("Four player selected");

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
