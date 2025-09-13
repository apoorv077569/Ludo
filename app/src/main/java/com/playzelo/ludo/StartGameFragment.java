package com.playzelo.ludo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class StartGameFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_start_game, container, false);

        view.findViewById(R.id.btn_10).setOnClickListener(v -> startGame(10));
        view.findViewById(R.id.btn_50).setOnClickListener(v -> startGame(50));
        view.findViewById(R.id.btn_100).setOnClickListener(v -> startGame(100));

        return view;
    }

    private void startGame(int amount) {
        Toast.makeText(getContext(), "Start Game with ₹" + amount, Toast.LENGTH_SHORT).show();
    }
}
