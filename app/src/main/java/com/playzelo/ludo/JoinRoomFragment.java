package com.playzelo.ludo;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


public class JoinRoomFragment extends Fragment {

    private EditText roomIdInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_join_room, container, false);
        roomIdInput = view.findViewById(R.id.room_id_input);

        view.findViewById(R.id.join_button).setOnClickListener(v -> {
            String id = roomIdInput.getText().toString();
            if (id.isEmpty()) {
                Toast.makeText(getContext(), "Enter Room ID", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Joining Room: " + id, Toast.LENGTH_SHORT).show();

                // Start GameRoomActivity and pass Room ID
                Intent intent = new Intent(getActivity(), GameRoomActivity.class);
                intent.putExtra("ROOM_ID", id);
                startActivity(intent);
            }
        });

        return view;
    }
}
