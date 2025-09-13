package com.playzelo.ludo.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.playzelo.ludo.ConfirmPaymentBottomSheet;
import com.playzelo.ludo.R;

import java.util.ArrayList;

public class TournamentsActivity extends AppCompatActivity {

    private TextView tabAll, tabRegular, tab2Players;
    private LinearLayout layoutRecommendedTournaments, layoutOtherTournaments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ludo_tournaments);

        tabAll = findViewById(R.id.tabAll);
        tabRegular = findViewById(R.id.tabRegular);
        tab2Players = findViewById(R.id.tab2Players);
        layoutRecommendedTournaments = findViewById(R.id.layoutRecommendedTournaments);
        layoutOtherTournaments = findViewById(R.id.layoutOtherTournaments);

        // ✅ Wallet & Add Money Containers
        FrameLayout btnWalletBalanceContainer = findViewById(R.id.btnWalletBalanceContainer);
        FrameLayout btnAddMoneyContainer = findViewById(R.id.btnAddMoneyContainer);

        // Optional: Set icons (already in XML most likely)
        ImageView ivWalletBalance = findViewById(R.id.ivWalletIcon);
        ivWalletBalance.setImageResource(R.drawable.withdraw);
        ImageView btnAddMoney = findViewById(R.id.btnAddMoney);
        btnAddMoney.setImageResource(R.drawable.deposit);

        // ✅ Click Listeners
        btnWalletBalanceContainer.setOnClickListener(v -> {
            Toast.makeText(this, "Wallet Clicked", Toast.LENGTH_SHORT).show();
            loadFragment(new WalletFragment());
        });

        btnAddMoneyContainer.setOnClickListener(v -> {
            Toast.makeText(this, "Add Money Clicked", Toast.LENGTH_SHORT).show();
            loadFragment(new AddMoneyFragment());
        });

        // Tab click listeners
        tabAll.setOnClickListener(v -> showToast("All selected"));
        tabRegular.setOnClickListener(v -> showToast("Regular selected"));
        tab2Players.setOnClickListener(v -> showToast("2 Players selected"));

        // Load tournament lists
        loadRecommendedTournaments();
        loadOtherTournaments();
    }

    private void loadRecommendedTournaments() {
        ArrayList<Tournament> recommended = new ArrayList<>();
        recommended.add(new Tournament("₹3", "₹5", 9000));
        recommended.add(new Tournament("₹5", "₹10", 8000));
        recommended.add(new Tournament("₹10", "₹20", 7000));

        layoutRecommendedTournaments.removeAllViews();

        TextView title = new TextView(this);
        title.setText("Recommended Tournaments");
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(16, 16, 16, 8);
        layoutRecommendedTournaments.addView(title);

        for (Tournament t : recommended) {
            View view = getLayoutInflater().inflate(R.layout.item_tournament_card, null);

            TextView tvPrize = view.findViewById(R.id.tvPrizePool);
            TextView tvEntry = view.findViewById(R.id.tvEntryFee);
            TextView tvTimer = view.findViewById(R.id.tvCountdown);

            tvPrize.setText(t.getPrizePool());
            tvEntry.setText(t.getEntryFee());

            startCountdown(tvTimer, t.getCountdownTime());

            tvEntry.setOnClickListener(v -> {
                String entryFee = tvEntry.getText().toString();
                ConfirmPaymentBottomSheet bottomSheet = ConfirmPaymentBottomSheet.newInstance(entryFee);
                bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, dpToPx(12));
            view.setLayoutParams(params);

            layoutRecommendedTournaments.addView(view);
        }
    }

    private void loadOtherTournaments() {
        ArrayList<Tournament> others = new ArrayList<>();
        others.add(new Tournament("₹100", "₹50", 13000));
        others.add(new Tournament("₹75", "₹30", 7000));
        others.add(new Tournament("₹60", "₹20", 5000));

        layoutOtherTournaments.removeAllViews();

        TextView title = new TextView(this);
        title.setText("Other Tournaments");
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(16, 16, 16, 8);
        layoutOtherTournaments.addView(title);

        for (Tournament t : others) {
            View view = getLayoutInflater().inflate(R.layout.item_other_tournament, null);

            TextView tvPrize = view.findViewById(R.id.tvPrizePool);
            TextView tvEntry = view.findViewById(R.id.tvEntryFee);
            TextView tvTimer = view.findViewById(R.id.tvCountdown);

            tvPrize.setText(t.getPrizePool());
            tvEntry.setText(t.getEntryFee());

            startCountdown(tvTimer, t.getCountdownTime());

            tvEntry.setOnClickListener(v -> {
                ConfirmPaymentBottomSheet bottomSheet = ConfirmPaymentBottomSheet.newInstance(t.getEntryFee());
                bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, dpToPx(12));
            view.setLayoutParams(params);

            layoutOtherTournaments.addView(view);
        }
    }

    private void startCountdown(TextView timerView, long time) {
        new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                timerView.setText(String.format("00m %02ds", seconds));
            }

            public void onFinish() {
                timerView.setText("00m 00s");
            }
        }.start();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void loadFragment(Fragment fragment) {
        findViewById(R.id.mainContentLayout).setVisibility(View.GONE); // Hide main content
        findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE); // Show fragment

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    // ✅ Back press to show main content again
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            findViewById(R.id.fragmentContainer).setVisibility(View.GONE);
            findViewById(R.id.mainContentLayout).setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }


    public class Tournament {
        private String prizePool;
        private String entryFee;
        private long countdownTime;

        public Tournament(String prizePool, String entryFee, long countdownTime) {
            this.prizePool = prizePool;
            this.entryFee = entryFee;
            this.countdownTime = countdownTime;
        }

        public String getPrizePool() {
            return prizePool;
        }

        public String getEntryFee() {
            return entryFee;
        }

        public long getCountdownTime() {
            return countdownTime;
        }
    }

    public static class WalletFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_wallet, container, false);
        }
    }

    public static class AddMoneyFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_add_money, container, false);
        }
    }
}
