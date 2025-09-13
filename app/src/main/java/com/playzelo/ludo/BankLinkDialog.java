package com.playzelo.ludo;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class BankLinkDialog extends Dialog {

    EditText etAccount, etIFSC;
    Button btnSubmit;

    public BankLinkDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.dialog_bank_link);

        etAccount = findViewById(R.id.et_account_number);
        etIFSC = findViewById(R.id.et_ifsc_code);
        btnSubmit = findViewById(R.id.btn_submit_bank);

        btnSubmit.setOnClickListener(v -> {
            String acc = etAccount.getText().toString().trim();
            String ifsc = etIFSC.getText().toString().trim();
            if (!acc.isEmpty() && !ifsc.isEmpty()) {
                dismiss();
                Toast.makeText(getContext(), "Bank Linked Successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

