package com.playzelo.ludo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.playzelo.ludo.R;
import com.playzelo.ludo.apiservice.ApiClient;
import com.playzelo.ludo.apiservice.ApiService;
import com.playzelo.ludo.apiservice.LudoApiHelper;
import com.playzelo.ludo.databinding.ActivityTournamentsBinding;
import com.playzelo.ludo.models.CreateRoomRequest;
import com.playzelo.ludo.models.JoinRoomBody;
import com.playzelo.ludo.models.RoomDto;
import com.playzelo.ludo.models.RoomListResponse;
import com.playzelo.ludo.models.RoomResponse;
import com.playzelo.ludo.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TournamentsActivity extends AppCompatActivity {
    ActivityTournamentsBinding binding;
    ApiService apiService;
    LudoApiHelper ludoApiHelper;
    private String username, email, token, id,photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTournamentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        onClickListener();

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            email = intent.getStringExtra("email");
            token = intent.getStringExtra("token");
            id = intent.getStringExtra("userId");
            photo = intent.getStringExtra("photo");
        }
        apiService = ApiClient.getClient().create(ApiService.class);
        Log.d("UserDataTA", "Token: " + token);
        ludoApiHelper = new LudoApiHelper();

    }


    //    private void onClickListener(){
//        binding.btnTwoPlayer.setOnClickListener(view -> {
//            Intent intent = new Intent(TournamentsActivity.this, MatchmakingActivity.class);
//            intent.putExtra("playerCount", 2);
//            intent.putExtra("type", "2p");
//            intent.putExtra("roomId", ""); // no room yet
//            intent.putExtra("username", username);
//            intent.putExtra("entryFee", 0);
//            intent.putExtra("winPrize", 0);
//            intent.putExtra("userId", id);
//            intent.putExtra("token", token);
//            startActivity(intent);
//            showToast("Two player selected");
//        });
//        binding.btnFourPlayer.setOnClickListener(view -> {
//            Intent intent = new Intent(TournamentsActivity.this, MatchmakingActivity.class);
//            intent.putExtra("playerCount", 4);
//            intent.putExtra("type", "4p");
//            intent.putExtra("roomId", "");
//           intent.putExtra("username", username);
//           intent.putExtra("userId", id);
//           intent.putExtra("token", token);
//            intent.putExtra("entryFee", 0);
//            intent.putExtra("winPrize", 0);
//            startActivity(intent);
//            showToast("Four player selected");
//
//        });
//    }

    private void onClickListener() {
        binding.btnTwoPlayer.setOnClickListener(v -> handleTypeClick(2));
        binding.btnFourPlayer.setOnClickListener(v -> handleTypeClick(4));
    }

    private void handleTypeClick(int type) {
        binding.btnTwoPlayer.setEnabled(false);
        binding.btnFourPlayer.setEnabled(false);

        apiService.getRooms().enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<RoomListResponse> call, Response<RoomListResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    binding.btnTwoPlayer.setEnabled(true);
                    binding.btnFourPlayer.setEnabled(true);
                    showToast("Failed to fetch room: " + response.code());
                    return;
                }
                List<RoomDto> rooms = response.body().getRooms();
                RoomDto targetRoom = null;
                if (rooms != null) {
                    for (RoomDto r : rooms) {
                        int maxPlayers = r.getType();
                        int currentPlayers = (r.getPlayers() != null) ? r.getPlayers().size() : 0;

                        if (r.getType() == type &&
                                "waiting".equalsIgnoreCase(r.getStatus())
                                && currentPlayers < maxPlayers
                        ) {
                            targetRoom = r;
                            break;
                        }
                    }
                }
                if (targetRoom != null) {
                    Log.d("MatchMaking", "Found Existing room: " + targetRoom.getRoomId());
                    joinExistingRoom(targetRoom);
                } else {
                    Log.d("MatchMaking", "No room found,creating a new room");
                    createNewRoom(type);
                }
            }

            @Override
            public void onFailure(Call<RoomListResponse> call, Throwable t) {
                binding.btnTwoPlayer.setEnabled(true);
                binding.btnFourPlayer.setEnabled(true);
                showToast("Network error: " + t.getMessage());
            }
        });
    }

    private void createNewRoom(int type) {
        SessionManager session = new SessionManager(this);

        if (id == null || id.isEmpty()) {
            id = session.getUserId();
        }

        if (username == null || username.isEmpty()) {
            username = session.getUsername();
        }

        if (username == null || username.isEmpty() || id == null || id.isEmpty()) {
            Log.e("ROOM_DEBUG",
                    "❌ Invalid data → type=" + type +
                            ", userId=" + id +
                            ", username=" + username);

            showToast("User session invalid. Please login again.");
            return;
        }

        Log.d("ROOM_DEBUG",
                "✅ Sending → type=" + type +
                        ", userId=" + id +
                        ", username=" + username);
        CreateRoomRequest body = new CreateRoomRequest(type,id,username);
        apiService.createRoom(body).enqueue(
                new Callback<>() {
                    @Override
                    public void onResponse(Call<RoomResponse> call, Response<RoomResponse> response) {
                        binding.btnTwoPlayer.setEnabled(true);
                        binding.btnFourPlayer.setEnabled(true);

                        if (!response.isSuccessful() || response.body() == null) {

                            try {
                                String error = response.errorBody() != null
                                        ? response.errorBody().string()
                                        : "unknown";

                                Log.e("CREATED_ROOM",
                                        "code=" + response.code() +
                                                " error=" + error);

                                showToast("Create Failed: " + response.code());

                            } catch (Exception e) {
                                Log.e("CREATED_ROOM", "read error", e);
                            }

                            return;
                        }
                        RoomDto room = response.body().getRoom();
//                        ludoApiHelper.logRoomPlayers("CREATED_ROOM", room, id);
                        goToMatchMakingActivity(room);
                    }

                    @Override
                    public void onFailure(Call<RoomResponse> call, Throwable t) {
                        binding.btnTwoPlayer.setEnabled(true);
                        binding.btnFourPlayer.setEnabled(true);
                        showToast("Create network error: " + t.getMessage());
                    }
                }
        );
    }

    private void joinExistingRoom(RoomDto targetRoom) {
        JoinRoomBody body = new JoinRoomBody(id,username);
        apiService.joinRoom(targetRoom.getRoomId(),body)
                .enqueue(new Callback<RoomResponse>() {
                    @Override
                    public void onResponse(Call<RoomResponse> call, Response<RoomResponse> response) {
                        binding.btnTwoPlayer.setEnabled(true);
                        binding.btnFourPlayer.setEnabled(true);

                        if (!response.isSuccessful() || response.body() == null) {

                            try {
                                String errorMsg = response.errorBody() != null
                                        ? response.errorBody().string()
                                        : "unknown error";

                                Log.e("JOINED_ROOM",
                                        "code=" + response.code() +
                                                " error=" + errorMsg);

                                showToast("Join failed: " + errorMsg);

                            } catch (Exception e) {
                                Log.e("JOINED_ROOM", "read error", e);
                                showToast("Join failed: " + response.code());
                            }

                            return;
                        }
                        RoomDto room = response.body().getRoom();
                        goToMatchMakingActivity(room);
                    }

                    @Override
                    public void onFailure(Call<RoomResponse> call, Throwable t) {
                        binding.btnTwoPlayer.setEnabled(true);
                        binding.btnFourPlayer.setEnabled(true);
                        showToast("Join network error: " + t.getMessage());
                    }
                });
    }

    private void goToMatchMakingActivity(RoomDto room) {
        Intent intent = new Intent(TournamentsActivity.this, MatchmakingActivity.class);
        intent.putExtra("room", room);
        intent.putExtra("userId", id);
        intent.putExtra("username", username);
        intent.putExtra("token", token);
        intent.putExtra("photo", photo);
        startActivity(intent);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            findViewById(R.id.mainContentLayout).setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
