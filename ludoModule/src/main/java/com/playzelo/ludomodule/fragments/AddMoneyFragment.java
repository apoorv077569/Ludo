package com.playzelo.ludomodule.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.playzelo.ludomodule.R;
import com.playzelo.ludomodule.databinding.FragmentAddMoneyBinding;

public class AddMoneyFragment extends Fragment {

    private FragmentAddMoneyBinding binding;


    public AddMoneyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddMoneyBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Back button
        binding.ivBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // Text input watcher
        binding.etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.btnAddCash.setEnabled(s.length() > 0 && Integer.parseInt(s.toString()) > 0);
                if (binding.btnAddCash.isEnabled()) {
                    binding.btnAddCash.setBackgroundResource(R.drawable.bg_green_rounded_btn);
                    binding.btnAddCash.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                } else {
                    binding.btnAddCash.setBackgroundResource(R.drawable.bg_button_disabled);
                    binding.btnAddCash.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Quick amount buttons
        binding.btn100.setOnClickListener(v -> setAmount(100));
        binding.btn50.setOnClickListener(v -> setAmount(50));
        binding.btn10.setOnClickListener(v -> setAmount(10));

        binding.btnAddCash.setOnClickListener(v -> {
            String amount = binding.etAmount.getText().toString().trim();
            if (!amount.isEmpty()) {
                // Replace with actual payment flow
                Toast.makeText(getContext(), "Adding ₹" + amount, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAmount(int amount) {
        binding.etAmount.setText(String.valueOf(amount));
        binding.etAmount.setSelection(binding.etAmount.getText().length()); // move cursor to end
    }
}
