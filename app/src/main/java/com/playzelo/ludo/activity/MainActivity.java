package com.playzelo.ludo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.playzelo.ludo.fragments.AddMoneyFragment;
import com.playzelo.ludo.R;
import com.playzelo.ludo.fragments.WalletFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private LottieAnimationView lottieDice, lottieConflict;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ludo_main); // Your layout file

        // Lottie Animations
        lottieDice = findViewById(R.id.lottieDice);
        lottieConflict = findViewById(R.id.lottieConflict);

        lottieDice.playAnimation();
        lottieConflict.playAnimation();

        // Bottom Navigation Logic
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show();
                return true;

            } else if (itemId == R.id.nav_refer) {
                Toast.makeText(this, "Refer clicked", Toast.LENGTH_SHORT).show();
                // Replace with actual Refer Fragment if needed
                return true;

            } else if (itemId == R.id.nav_account) {
                Toast.makeText(this, "Account clicked", Toast.LENGTH_SHORT).show();
                // Replace with actual Account Fragment if needed
                return true;
            }
            return false;
        });

        // Top Wallet Button
        LinearLayout btnWalletBalanceContainer = findViewById(R.id.btnWalletBalanceContainer);
        btnWalletBalanceContainer.setOnClickListener(v -> openWalletFragment());

        // Top "Deposit Now" Button
        LinearLayout btnDepositNow = findViewById(R.id.btnDepositNow);
        btnDepositNow.setOnClickListener(v -> openAddMoneyFragment());

        // Play Now Button in Banner
        Button btnPlayNow = findViewById(R.id.btnPlayNow);
        btnPlayNow.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TournamentsActivity.class);
            startActivity(intent);
        });

        // "Deposit Now" button inside horizontal card
        // This card button is a child inside ScrollView, so it's directly accessible if declared properly
        Button cardDepositNowBtn = findViewById(R.id.cardDepositNowButton); // We'll add this ID next
        if (cardDepositNowBtn != null) {
            cardDepositNowBtn.setOnClickListener(v -> openAddMoneyFragment());
        }

        Button cardPlayNowBtn = findViewById(R.id.cardPlayNowButton); // We'll add this ID next
        if (cardPlayNowBtn != null) {
            cardPlayNowBtn.setOnClickListener(v -> openWalletFragment());
        }

        Button cardPlayNowBtn1 = findViewById(R.id.cardPlayNowButton1); // We'll add this ID next
        if (cardPlayNowBtn1 != null) {
            cardPlayNowBtn1.setOnClickListener(v -> openWalletFragment());
        }

    }

    private void openWalletFragment() {
        Fragment walletFragment = new WalletFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, walletFragment)
                .addToBackStack(null)
                .commit();
    }

    private void openAddMoneyFragment() {
        Fragment addMoneyFragment = new AddMoneyFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, addMoneyFragment)
                .addToBackStack(null)
                .commit();
    }
}
