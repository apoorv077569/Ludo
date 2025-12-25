package com.playzelo.ludomodule.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.playzelo.ludomodule.databinding.ConfirmPaymentBottomSheetBinding;

public class ConfirmPaymentBottomSheet extends BottomSheetDialogFragment {

    private String userId, username, authToken;
    private int playerCount;
    private String playerType = "2p";
    private double entryFee,prizePool;

    // ✅ Payment callback interface
    public interface PaymentListener {
        void onPaymentConfirmed();
    }

    private PaymentListener listener;

    public void setPaymentListener(PaymentListener listener) {
        this.listener = listener;
    }

    public static ConfirmPaymentBottomSheet newInstance(String userId, String username, String authToken, String playerType,double entryFee,double prizePool) {
        ConfirmPaymentBottomSheet fragment = new ConfirmPaymentBottomSheet();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("username", username);
        args.putString("auth_token", authToken);
        args.putString("type", playerType);
        args.putDouble("entryFee", entryFee);
        args.putDouble("winPrize",prizePool);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        ConfirmPaymentBottomSheetBinding binding = ConfirmPaymentBottomSheetBinding.inflate(getLayoutInflater(), container, false);

        // Get passed arguments
        if (getArguments() != null) {
            userId = getArguments().getString("userId", "");
            username = getArguments().getString("username", "");
            authToken = getArguments().getString("auth_token", "");
            playerCount = getArguments().getInt("playerCount", 2);
            entryFee = getArguments().getDouble("entryFee", 0);
            prizePool = getArguments().getDouble("winPrize", 0);


        }

        // Close button
        binding.btnClose.setOnClickListener(v -> dismiss());

        // Join Now click
        binding.btnJoinNow.setOnClickListener(v -> {
            // ✅ Notify Activity that payment is confirmed
            if (listener != null) {
                listener.onPaymentConfirmed();
            }

            // Launch GameStartBottomSheet as before
            GameStartBottomSheet bottomSheet = GameStartBottomSheet.newInstance(userId, username, authToken, playerType,entryFee,prizePool);
            bottomSheet.show(getParentFragmentManager(), "GameStartBottomSheet");
            dismiss();
        });

        return binding.getRoot();
    }
}
