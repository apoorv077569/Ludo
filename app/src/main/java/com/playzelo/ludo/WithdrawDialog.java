package com.playzelo.ludo;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class WithdrawDialog extends Dialog {

    public interface WithdrawCallback {
        void onWithdraw(String amount, boolean viaBank);
    }

    public WithdrawDialog(@NonNull Context context, double balance, WithdrawCallback callback) {
        super(context);
        setContentView(R.layout.dialog_withdraw);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvBalance = findViewById(R.id.tvBalance);
        EditText etAmount = findViewById(R.id.etAmount);
        CheckBox checkBank = findViewById(R.id.checkBank);
        Button btnWithdraw = findViewById(R.id.btnWithdraw);
        Button btnLinkUpi = findViewById(R.id.btnLinkUpi);

        tvBalance.setText("Withdrawable Balance: ₹" + balance);

        btnWithdraw.setOnClickListener(v -> {
            String amount = etAmount.getText().toString().trim();
            if (amount.isEmpty()) {
                etAmount.setError("Enter amount");
                return;
            }
            callback.onWithdraw(amount, checkBank.isChecked());
            dismiss();
        });

        // On clicking the Link UPI button, call the method to show the UPI dialog
        btnLinkUpi.setOnClickListener(v -> showUpiDialog(context)); // Pass the context to showUpiDialog
    }

    // Method to show the UPI dialog
    private void showUpiDialog(Context context) {
        // Create and set up the dialog
        Dialog dialog = new Dialog(context); // Use the context passed from the constructor
        dialog.setContentView(R.layout.dialog_upi_id); // Custom dialog layout
        dialog.setCancelable(true); // User can dismiss the dialog by tapping outside

        // Get references to the views
        EditText upiEditText = dialog.findViewById(R.id.upi_id_edittext);
        TextView instructionTextView = dialog.findViewById(R.id.upi_instruction);
        Button proceedButton = dialog.findViewById(R.id.proceed_button);

        // Set the Proceed button click listener
        proceedButton.setOnClickListener(v -> {
            String upiId = upiEditText.getText().toString();

            if (upiId.isEmpty()) {
                upiEditText.setError("UPI ID is required");
            } else {
                // Handle the UPI ID (e.g., pass it to the next screen or API)
                System.out.println("UPI ID entered: " + upiId);
                dialog.dismiss(); // Dismiss the dialog
            }
        });

        // Show the dialog
        dialog.show();
    }
}
