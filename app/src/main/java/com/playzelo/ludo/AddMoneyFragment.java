package com.playzelo.ludo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AddMoneyFragment extends Fragment {

    private EditText etAmount;
    private Button btnAddCash;
    private Button btn100, btn50, btn10;
    private ImageView ivBack;

    public AddMoneyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_money, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etAmount = view.findViewById(R.id.etAmount);
        btnAddCash = view.findViewById(R.id.btnAddCash);
        btn100 = view.findViewById(R.id.btn100);
        btn50 = view.findViewById(R.id.btn50);
        btn10 = view.findViewById(R.id.btn10);
        ivBack = view.findViewById(R.id.ivBack);

        // Back button
        ivBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Text input watcher
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnAddCash.setEnabled(s.length() > 0 && Integer.parseInt(s.toString()) > 0);
                if (btnAddCash.isEnabled()) {
                    btnAddCash.setBackgroundResource(R.drawable.bg_green_rounded_btn);
                    btnAddCash.setTextColor(getResources().getColor(android.R.color.white));
                } else {
                    btnAddCash.setBackgroundResource(R.drawable.bg_button_disabled);
                    btnAddCash.setTextColor(getResources().getColor(R.color.gray));
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Quick amount buttons
        btn100.setOnClickListener(v -> setAmount(100));
        btn50.setOnClickListener(v -> setAmount(50));
        btn10.setOnClickListener(v -> setAmount(10));

        btnAddCash.setOnClickListener(v -> {
            String amount = etAmount.getText().toString().trim();
            if (!amount.isEmpty()) {
                // Replace with actual payment flow
                // Example: Toast.makeText(getContext(), "Adding ₹" + amount, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAmount(int amount) {
        etAmount.setText(String.valueOf(amount));
        etAmount.setSelection(etAmount.getText().length()); // move cursor to end
    }
}
