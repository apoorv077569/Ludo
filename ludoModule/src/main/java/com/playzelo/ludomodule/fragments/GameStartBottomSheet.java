package com.playzelo.ludomodule.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.playzelo.ludomodule.databinding.FragmentGameStartBottomSheetBinding;

public class GameStartBottomSheet extends BottomSheetDialogFragment {

    private CountDownTimer countDownTimer;
    private FragmentGameStartBottomSheetBinding binding;
    private String userId, username;
    private String authToken;
    private int playerCount = 2;
    private double entry_fee, prize_pool;
    private boolean isDismissed = false;

    // ✅ Callback to notify Activity when timer finishes
    public interface TimerListener {
        void onTimerFinished();
    }

    private TimerListener listener;

    public void setTimerListener(TimerListener listener) {
        this.listener = listener;
    }

    public static GameStartBottomSheet newInstance(String userId, String username, String authToken, String playerType, double entryFee, double prizePool) {
        GameStartBottomSheet fragment = new GameStartBottomSheet();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("username", username);
        args.putString("auth_token", authToken);
        args.putString("type", playerType);
        args.putDouble("entryFee", entryFee);
        args.putDouble("winPrize", prizePool);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            username = getArguments().getString("username");
            authToken = getArguments().getString("auth_token");
            playerCount = getArguments().getInt("playerCount", 2);
            entry_fee = getArguments().getDouble("entryFee");
            prize_pool = getArguments().getDouble("winPrize");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGameStartBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startTimer(10000); // e.g. 10 sec demo timer (set 30000 for 30 sec)
    }

    @Override
    public void dismiss() {
        if (!isDismissed) {
            isDismissed = true;
            super.dismiss();
        }
    }

    private void startTimer(long time) {
        countDownTimer = new CountDownTimer(time, 1000) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long millisUntilFinished) {
                long sec = millisUntilFinished / 1000;
                long min = sec / 60;
                sec = sec % 60;
                String time = String.format("%02dm:%02ds", min, sec);
                if (binding != null) {
                    binding.tvTimer.setText(time);
                }
            }

            @Override
            public void onFinish() {
                if (binding != null) {
                    binding.tvTimer.setText("00m:00s");
                }

                if (isAdded() && !isDismissed) {
                    dismiss();

                    // ✅ Notify Activity instead of starting Matchmaking here
                    if (listener != null) {
                        listener.onTimerFinished();
                    }
                }
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        binding = null;
        isDismissed = true;
    }
}
