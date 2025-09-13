package com.playzelo.ludo.fragments;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.playzelo.ludo.R;
import com.playzelo.ludo.WithdrawDialog;

public class WalletFragment extends Fragment {

    private AppCompatButton btnAddCash, btnWithdraw;

    public WalletFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnAddCash = view.findViewById(R.id.btnAddCash);
        btnWithdraw = view.findViewById(R.id.btnWithdraw);
        ImageView ivBack = view.findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> requireActivity().onBackPressed());


        btnAddCash.setOnClickListener(v -> openAddMoneyFragment());

        btnWithdraw.setOnClickListener(v -> {
            int currentBalance = 1200; // You can fetch this from backend later

            WithdrawDialog dialog = new WithdrawDialog(requireContext(), currentBalance, new WithdrawDialog.WithdrawCallback() {
                @Override
                public void onWithdraw(String amount, boolean viaBank) {
                    Toast.makeText(getContext(), "Withdrawing ₹" + amount + (viaBank ? " to Bank" : " via UPI"), Toast.LENGTH_SHORT).show();
                    // You can also call backend API here
                }
            });
            dialog.show();
        });
    }

    private void openAddMoneyFragment() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, new AddMoneyFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
