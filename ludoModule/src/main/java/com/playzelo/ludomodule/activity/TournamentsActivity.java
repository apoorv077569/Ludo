////package com.playzelo.ludomodule.activity;
////
////import android.annotation.SuppressLint;
////import android.content.Intent;
////import android.graphics.Typeface;
////import android.os.Bundle;
////import android.os.CountDownTimer;
////import android.util.Log;
////import android.view.View;
////import android.view.ViewGroup;
////import android.widget.AdapterView;
////import android.widget.LinearLayout;
////import android.widget.TextView;
////import android.widget.Toast;
////
////import androidx.annotation.NonNull;
////import androidx.appcompat.app.AppCompatActivity;
////
////import com.playzelo.ludomodule.R;
////import com.playzelo.ludomodule.apiservice.LudoApiHelper;
////import com.playzelo.ludomodule.databinding.ActivityLudoTournamentsBinding;
////import com.playzelo.ludomodule.fragments.ConfirmPaymentBottomSheet;
////import com.playzelo.ludomodule.models.LudoRoomResponse;
////import com.playzelo.ludomodule.utils.SocketManager;
////
////import java.util.ArrayList;
////
////import retrofit2.Call;
////import retrofit2.Callback;
////import retrofit2.Response;
////
////public class TournamentsActivity extends AppCompatActivity {
////
////    private ActivityLudoTournamentsBinding binding;
////
////    private String userId, authToken, username;
////    private String playerType = "2p";
////    private int playerCount = 2; // default
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        binding = ActivityLudoTournamentsBinding.inflate(getLayoutInflater());
////        setContentView(binding.getRoot());
////
////        // Get user data from Intent
////        Intent intent = getIntent();
////        if (intent != null) {
////            userId = intent.getStringExtra("userId");
////            authToken = intent.getStringExtra("auth_token");
////            username = intent.getStringExtra("username");
////        }
////
////        binding.tabAll.setOnClickListener(v -> showToast("All selected"));
////        binding.tabRegular.setOnClickListener(v -> showToast("Regular selected"));
////        binding.btnBack.setOnClickListener(v -> onBackPressed());
////
////        // Player selection spinner
////        binding.spinnerPlayers.setSelection(0);
////        binding.spinnerPlayers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
////            @Override
////            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
////                if (i == 0) {
////                    playerCount = 2;
////                    playerType = "2p";
////                } else {
////                    playerCount = 4;
////                    playerType = "4p";
////                }
////                showToast(playerCount + " Players selected");
////            }
////
////            @Override
////            public void onNothingSelected(AdapterView<?> adapterView) {
////            }
////        });
////
////        loadRecommendedTournaments();
////        loadOtherTournaments();
////
////        // Initialize Socket
////        SocketManager.initSocket("https://ludo-game-co08.onrender.com");
////        SocketManager.getSocket().connect();
////    }
////
////    @SuppressLint("SetTextI18n")
////    private void loadRecommendedTournaments() {
////        ArrayList<Tournament> recommended = new ArrayList<>();
////        recommended.add(new Tournament(5, 3, 9000));
////        recommended.add(new Tournament(10, 5, 8000));
////        recommended.add(new Tournament(20, 10, 7000));
////
////        binding.layoutRecommendedTournaments.removeAllViews();
////
////        TextView title = new TextView(this);
////        title.setText("Recommended Tournaments");
////        title.setTypeface(null, Typeface.BOLD);
////        title.setPadding(16, 16, 16, 8);
////        binding.layoutRecommendedTournaments.addView(title);
////
////        for (Tournament t : recommended) {
////            @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.item_tournament_card, null);
////            TextView tvPrize = view.findViewById(R.id.tvPrizePool);
////            TextView tvEntry = view.findViewById(R.id.tvEntryFee);
////            TextView tvTimer = view.findViewById(R.id.tvCountdown);
////
////            tvPrize.setText(String.valueOf(t.getPrizePool()));
////            tvEntry.setText(String.valueOf(t.getEntryFee()));
////            startCountdown(tvTimer, t.getCountdownTime());
////
////            tvEntry.setOnClickListener(v -> openPaymentSheetAndJoinRoom(t.getEntryFee(), t.getPrizePool()));
////
////            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
////                    ViewGroup.LayoutParams.MATCH_PARENT,
////                    ViewGroup.LayoutParams.WRAP_CONTENT
////            );
////            params.setMargins(0, 0, 0, dpToPx());
////            view.setLayoutParams(params);
////            binding.layoutRecommendedTournaments.addView(view);
////        }
////    }
////
////    @SuppressLint("SetTextI18n")
////    private void loadOtherTournaments() {
////        ArrayList<Tournament> others = new ArrayList<>();
////        others.add(new Tournament(100, 50, 13000));
////        others.add(new Tournament(75, 30, 7000));
////        others.add(new Tournament(60, 20, 5000));
////
////        binding.layoutOtherTournaments.removeAllViews();
////
////        TextView title = new TextView(this);
////        title.setText("Other Tournaments");
////        title.setTypeface(null, Typeface.BOLD);
////        title.setPadding(16, 16, 16, 8);
////        binding.layoutOtherTournaments.addView(title);
////
////        for (Tournament t : others) {
////            @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.item_other_tournament, null);
////            TextView tvPrize = view.findViewById(R.id.tvPrizePool);
////            TextView tvEntry = view.findViewById(R.id.tvEntryFee);
////            TextView tvTimer = view.findViewById(R.id.tvCountdown);
////
////            tvPrize.setText(String.valueOf(t.getPrizePool()));
////            tvEntry.setText(String.valueOf(t.getEntryFee()));
////            startCountdown(tvTimer, t.getCountdownTime());
////
////            tvEntry.setOnClickListener(v -> openPaymentSheetAndJoinRoom(t.getEntryFee(), t.getPrizePool()));
////
////            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
////                    ViewGroup.LayoutParams.MATCH_PARENT,
////                    ViewGroup.LayoutParams.WRAP_CONTENT
////            );
////            params.setMargins(0, 0, 0, dpToPx());
////            view.setLayoutParams(params);
////            binding.layoutOtherTournaments.addView(view);
////        }
////    }
////
////    private void openPaymentSheetAndJoinRoom(double entryFee, double prizePool) {
////        ConfirmPaymentBottomSheet confirmPaymentBottomSheet = ConfirmPaymentBottomSheet.newInstance(userId, username, authToken, playerType, entryFee, prizePool);
////        confirmPaymentBottomSheet.show(getSupportFragmentManager(), confirmPaymentBottomSheet.getTag());
////
////        LudoApiHelper helper = LudoApiHelper.getInstance(authToken);
////        helper.automatch(entryFee, prizePool, playerType, new Callback<>() {
////            @Override
////            public void onResponse(@NonNull Call<LudoRoomResponse> call, @NonNull Response<LudoRoomResponse> response) {
////                if (response.isSuccessful() && response.body() != null) {
////                    LudoRoomResponse roomResponse = response.body();
////                    String roomId = roomResponse.getRoomId();
////                    showToast("Game started. Room ID: " + roomId);
////                    Log.d("API_CALL", "Automatch API successful. Room ID: " + roomId);
////
////                    Intent intent = new Intent(TournamentsActivity.this, MatchmakingActivity.class);
////                    intent.putExtra("userId", userId);
////                    intent.putExtra("username", username);
////                    intent.putExtra("auth_token", authToken);
////                    intent.putExtra("type", playerType);
////                    intent.putExtra("playerCount", playerCount);
////                    intent.putExtra("entryFee", entryFee);
////                    intent.putExtra("winPrize", prizePool);
////                    intent.putExtra("roomId", roomId);
////                    startActivity(intent);
////                } else {
////                    showToast("Failed to start game: " + response.message());
////                    Log.e("API_CALL", "Automatch failed. HTTP code: " + response.code() + ", message: " + response.message());
////                }
////            }
////
////            @Override
////            public void onFailure(@NonNull Call<LudoRoomResponse> call, @NonNull Throwable throwable) {
////                showToast("Network Error: " + throwable.getMessage());
////                Log.e("API_CALL", "Automatch request failed: " + throwable.getMessage(), throwable);
////            }
////        });
////    }
////
////
////
////
////    private void startCountdown(TextView timerView, long time) {
////        new CountDownTimer(time, 1000) {
////            @SuppressLint("DefaultLocale")
////            public void onTick(long millisUntilFinished) {
////                timerView.setText(String.format("00m %02ds", millisUntilFinished / 1000));
////            }
////
////            @SuppressLint("SetTextI18n")
////            public void onFinish() {
////                timerView.setText("00m 00s");
////            }
////        }.start();
////    }
////
////    private void showToast(String msg) {
////        Toast.makeText(TournamentsActivity.this, msg, Toast.LENGTH_SHORT).show();
////    }
////
////    private int dpToPx() {
////        return Math.round(12 * getResources().getDisplayMetrics().density);
////    }
////
////    @Override
////    public void onBackPressed() {
////        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
////            getSupportFragmentManager().popBackStack();
////        } else super.onBackPressed();
////    }
////
////    public static class Tournament {
////        private final double prizePool;
////        private final double entryFee;
////        private final long countdownTime;
////
////        public Tournament(double prizePool, double entryFee, long countdownTime) {
////            this.prizePool = prizePool;
////            this.entryFee = entryFee;
////            this.countdownTime = countdownTime;
////        }
////
////        public double getPrizePool() {
////            return prizePool;
////        }
////
////        public double getEntryFee() {
////            return entryFee;
////        }
////
////        public long getCountdownTime() {
////            return countdownTime;
////        }
////    }
////}
////
//
//package com.playzelo.ludomodule.activity;
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.graphics.Typeface;
//import android.os.Bundle;
//import android.os.CountDownTimer;
//import android.util.Log;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.playzelo.ludomodule.R;
//import com.playzelo.ludomodule.apiservice.LudoApiHelper;
//import com.playzelo.ludomodule.databinding.ActivityLudoTournamentsBinding;
//import com.playzelo.ludomodule.fragments.ConfirmPaymentBottomSheet;
//import com.playzelo.ludomodule.fragments.GameStartBottomSheet;
//import com.playzelo.ludomodule.models.LudoRoomResponse;
//import com.playzelo.ludomodule.utils.SocketManager;
//
//import java.util.ArrayList;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class TournamentsActivity extends AppCompatActivity {
//
//    private ActivityLudoTournamentsBinding binding;
//
//    private String userId, authToken, username;
//    private String playerType = "2p";
//    private int playerCount = 2; // default
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        binding = ActivityLudoTournamentsBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        // Get user data from Intent
//        Intent intent = getIntent();
//        if (intent != null) {
//            userId = intent.getStringExtra("userId");
//            authToken = intent.getStringExtra("auth_token");
//            username = intent.getStringExtra("username");
//        }
//
//        binding.tabAll.setOnClickListener(v -> showToast("All selected"));
//        binding.tabRegular.setOnClickListener(v -> showToast("Regular selected"));
//        binding.btnBack.setOnClickListener(v -> onBackPressed());
//
//        // Player selection spinner
//        binding.spinnerPlayers.setSelection(0);
//        binding.spinnerPlayers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
////                playerCount = (i == 0) ? 2 : 4;
//                if (i == 0) {
//                    playerCount = 2;
//                    playerType = "2p";
//                } else {
//                    playerCount = 4;
//                    playerType = "4p";
//                }
//                showToast(playerCount + " Players selected");
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//            }
//        });
//
//        loadRecommendedTournaments();
//        loadOtherTournaments();
//
//        // Initialize Socket
//        SocketManager.initSocket("https://playzelo-nrwt.onrender.com");
//        SocketManager.getSocket().connect();
//    }
//
//    @SuppressLint("SetTextI18n")
//    private void loadRecommendedTournaments() {
//        ArrayList<Tournament> recommended = new ArrayList<>();
//        recommended.add(new Tournament(5, 3, 9000));
//        recommended.add(new Tournament(10, 5, 8000));
//        recommended.add(new Tournament(20, 10, 7000));
//
//        binding.layoutRecommendedTournaments.removeAllViews();
//
//        TextView title = new TextView(this);
//        title.setText("Recommended Tournaments");
//        title.setTypeface(null, Typeface.BOLD);
//        title.setPadding(16, 16, 16, 8);
//        binding.layoutRecommendedTournaments.addView(title);
//
//        for (Tournament t : recommended) {
//            @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.item_tournament_card, null);
//            TextView tvPrize = view.findViewById(R.id.tvPrizePool);
//            TextView tvEntry = view.findViewById(R.id.tvEntryFee);
//            TextView tvTimer = view.findViewById(R.id.tvCountdown);
//
//            tvPrize.setText(String.valueOf(t.getPrizePool()));
//            tvEntry.setText(String.valueOf(t.getEntryFee()));
//            startCountdown(tvTimer, t.getCountdownTime());
//
//            tvEntry.setOnClickListener(v -> openPaymentSheetAndJoinRoom(t.getEntryFee(), t.getPrizePool()));
//
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT
//            );
//            params.setMargins(0, 0, 0, dpToPx());
//            view.setLayoutParams(params);
//            binding.layoutRecommendedTournaments.addView(view);
//        }
//    }
//
//    @SuppressLint("SetTextI18n")
//    private void loadOtherTournaments() {
//        ArrayList<Tournament> others = new ArrayList<>();
//        others.add(new Tournament(100, 50, 13000));
//        others.add(new Tournament(75, 30, 7000));
//        others.add(new Tournament(60, 20, 5000));
//
//        binding.layoutOtherTournaments.removeAllViews();
//
//        TextView title = new TextView(this);
//        title.setText("Other Tournaments");
//        title.setTypeface(null, Typeface.BOLD);
//        title.setPadding(16, 16, 16, 8);
//        binding.layoutOtherTournaments.addView(title);
//
//        for (Tournament t : others) {
//            @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.item_other_tournament, null);
//            TextView tvPrize = view.findViewById(R.id.tvPrizePool);
//            TextView tvEntry = view.findViewById(R.id.tvEntryFee);
//            TextView tvTimer = view.findViewById(R.id.tvCountdown);
//
//            tvPrize.setText(String.valueOf(t.getPrizePool()));
//            tvEntry.setText(String.valueOf(t.getEntryFee()));
//            startCountdown(tvTimer, t.getCountdownTime());
//
//            tvEntry.setOnClickListener(v -> openPaymentSheetAndJoinRoom(t.getEntryFee(), t.getPrizePool()));
//
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT
//            );
//            params.setMargins(0, 0, 0, dpToPx());
//            view.setLayoutParams(params);
//            binding.layoutOtherTournaments.addView(view);
//        }
//    }
//
//    private void openPaymentSheetAndJoinRoom(double entryFee, double prizePool) {
//        ConfirmPaymentBottomSheet confirmPaymentBottomSheet = ConfirmPaymentBottomSheet.newInstance(userId, username, authToken, playerType, entryFee, prizePool);
//        confirmPaymentBottomSheet.setPaymentListener(() -> {
//            // When payment is confirmed, show the GameStartBottomSheet
//            GameStartBottomSheet gameStartBottomSheet = GameStartBottomSheet.newInstance(userId, username, authToken, playerType, entryFee, prizePool);
//            gameStartBottomSheet.show(getSupportFragmentManager(), "GameStartBottomSheet");
//
//            // Set a listener to launch matchmaking after the timer finishes
//            gameStartBottomSheet.setTimerListener(() -> {
//                LudoApiHelper helper = LudoApiHelper.getInstance(authToken);
//                helper.automatch(entryFee, prizePool, playerType, new Callback<>() {
//                    @Override
//                    public void onResponse(@NonNull Call<LudoRoomResponse> call, @NonNull Response<LudoRoomResponse> response) {
//                        if (response.isSuccessful() && response.body() != null) {
//                            LudoRoomResponse roomResponse = response.body();
//                            String roomId = roomResponse.getRoomId();
//                            showToast("Game started. Room ID: " + roomId);
//                            Log.d("API_CALL", "Automatch API successful. Room ID: " + roomId);
//
//                            Intent intent = TournamentsActivity.this.getIntent(roomId, entryFee, prizePool);
//                            startActivity(intent);
//                        } else {
//                            showToast("Failed to start game: " + response.message());
//                            Log.e("API_CALL", "Automatch failed. HTTP code: " + response.code() + ", message: " + response.message());
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(@NonNull Call<LudoRoomResponse> call, @NonNull Throwable throwable) {
//                        showToast("Network Error: " + throwable.getMessage());
//                        Log.e("API_CALL", "Automatch request failed: " + throwable.getMessage(), throwable);
//                    }
//                });
//            });
//        });
//
//        confirmPaymentBottomSheet.show(getSupportFragmentManager(), confirmPaymentBottomSheet.getTag());
//    }
//
//    @NonNull
//    private Intent getIntent(String roomId, double entryFee, double prizePool) {
//        Intent intent = new Intent(TournamentsActivity.this, MatchmakingActivity.class);
//        intent.putExtra("userId", userId);
//        intent.putExtra("username", username);
//        intent.putExtra("auth_token", authToken);
//        intent.putExtra("type", playerType);
//        intent.putExtra("playerCount", playerCount);
//        intent.putExtra("entryFee", entryFee);
//        intent.putExtra("winPrize", prizePool);
//        intent.putExtra("roomId", roomId);
//        return intent;
//    }
//
//
//    private void startCountdown(TextView timerView, long time) {
//        new CountDownTimer(time, 1000) {
//            @SuppressLint("DefaultLocale")
//            public void onTick(long millisUntilFinished) {
//                timerView.setText(String.format("00m %02ds", millisUntilFinished / 1000));
//            }
//
//            @SuppressLint("SetTextI18n")
//            public void onFinish() {
//                timerView.setText("00m 00s");
//            }
//        }.start();
//    }
//
//    private void showToast(String msg) {
//        Toast.makeText(TournamentsActivity.this, msg, Toast.LENGTH_SHORT).show();
//    }
//
//    private int dpToPx() {
//        return Math.round(12 * getResources().getDisplayMetrics().density);
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
//            getSupportFragmentManager().popBackStack();
//        } else super.onBackPressed();
//    }
//
//    public static class Tournament {
//        private final double prizePool;
//        private final double entryFee;
//        private final long countdownTime;
//
//        public Tournament(double prizePool, double entryFee, long countdownTime) {
//            this.prizePool = prizePool;
//            this.entryFee = entryFee;
//            this.countdownTime = countdownTime;
//        }
//
//        public double getPrizePool() {
//            return prizePool;
//        }
//
//        public double getEntryFee() {
//            return entryFee;
//        }
//
//        public long getCountdownTime() {
//            return countdownTime;
//        }
//    }
//}
