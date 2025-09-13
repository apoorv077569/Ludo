package com.playzelo.ludo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ConfirmPaymentBottomSheet extends BottomSheetDialogFragment {

    private TextView tvEntryAmount;
    private Button btnJoinNow;
    private String selectedEntryFee = "₹3"; // Default

    public static ConfirmPaymentBottomSheet newInstance(String entryFee) {
        ConfirmPaymentBottomSheet fragment = new ConfirmPaymentBottomSheet();
        Bundle args = new Bundle();
        args.putString("entry_fee", entryFee);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.confirm_payment_bottom_sheet, container, false);

        // Views
        ImageView btnClose = view.findViewById(R.id.btnClose);
        btnJoinNow = view.findViewById(R.id.btnJoinNow);
        LinearLayout spinnerEntryFee = view.findViewById(R.id.spinnerEntryFee);
        tvEntryAmount = view.findViewById(R.id.tvEntryAmount);

        // Get passed entry fee
        selectedEntryFee = getArguments() != null ? getArguments().getString("entry_fee", "₹3") : "₹3";
        tvEntryAmount.setText(selectedEntryFee);

        // Close
        btnClose.setOnClickListener(v -> dismiss());

        // Join Now click
        btnJoinNow.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CountdownActivity.class);
            intent.putExtra("entry_fee", selectedEntryFee); // pass entry fee
            startActivity(intent);
            dismiss();
        });

        // Entry fee picker
        spinnerEntryFee.setOnClickListener(v -> {
            List<String> recommendedEntryFees = Arrays.asList("₹3", "₹5", "₹10");
            List<String> otherTournamentEntryFees = Arrays.asList("₹15", "₹25", "₹50");

            Set<String> allEntryFeesSet = new LinkedHashSet<>();
            allEntryFeesSet.addAll(recommendedEntryFees);
            allEntryFeesSet.addAll(otherTournamentEntryFees);
            String[] allEntryFees = allEntryFeesSet.toArray(new String[0]);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Select Entry Fee");
            builder.setItems(allEntryFees, (dialog, which) -> {
                selectedEntryFee = allEntryFees[which];
                tvEntryAmount.setText(selectedEntryFee);

                // ✅ Button color fix
                btnJoinNow.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_blue_button));
                btnJoinNow.setBackgroundTintList(null);
                btnJoinNow.setTextColor(getResources().getColor(android.R.color.white));
            });
            builder.show();

        });

        return view;
    }
}
