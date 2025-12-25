package com.playzelo.ludo.activity;
import android.widget.Toast;

import okhttp3.ResponseBody;
import retrofit2.Call;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.playzelo.ludo.R;
import com.playzelo.ludo.apiservice.ApiClient;
import com.playzelo.ludo.apiservice.ApiService;
import com.playzelo.ludo.databinding.ActivityLoginBinding;
import com.playzelo.ludo.models.User;
import com.playzelo.ludo.models.UserResponse;
import com.playzelo.ludo.utils.SessionManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_PLAY_GAMES = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpPlayGames();
        binding.btnLogin.setOnClickListener(v -> login());
        binding.switchText.setOnClickListener(view ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class))
        );
        binding.btnPlayGames.setOnClickListener(v->startPlayGamesLogin());
    }

    private void startPlayGamesLogin(){
        if (googleSignInClient == null){
            showToast("Google signin is not initialised");
            return;
        }
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_PLAY_GAMES);
    }
    private void setUpPlayGames() {
        String serverClientId = getString(R.string.server_client_id);

        Log.d("PLAY_GAMES", "========================================");
        Log.d("PLAY_GAMES", "DIAGNOSTIC INFO");
        Log.d("PLAY_GAMES", "========================================");
        Log.d("PLAY_GAMES", "Package Name: " + getPackageName());
        Log.d("PLAY_GAMES", "Server Client ID: " + serverClientId);
        Log.d("PLAY_GAMES", "Client ID length: " + serverClientId.length());
        Log.d("PLAY_GAMES", "Ends with .googleusercontent.com: " + serverClientId.endsWith(".apps.googleusercontent.com"));

        // Check if google-services.json is loaded
        try {
            int resId = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
            if (resId != 0) {
                String webClientFromJson = getString(resId);
                Log.d("PLAY_GAMES", "Web client from google-services.json: " + webClientFromJson);
            } else {
                Log.e("PLAY_GAMES", "❌ google-services.json NOT found or not processed!");
            }
        } catch (Exception e) {
            Log.e("PLAY_GAMES", "Error checking google-services.json", e);
        }
        Log.d("PLAY_GAMES", "========================================");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestEmail()
                .requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }    private void login() {

        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setVisibility(View.GONE);

        if (email.isEmpty() || password.isEmpty()) {
            showToast("All fields are required");
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setVisibility(View.VISIBLE);
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        apiService.login(body).enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {

                // ===== ADD COMPREHENSIVE LOGGING =====
                Log.d("LOGIN_DEBUG", "========== API RESPONSE ==========");
                Log.d("LOGIN_DEBUG", "Response Code: " + response.code());
                Log.d("LOGIN_DEBUG", "Response Message: " + response.message());
                Log.d("LOGIN_DEBUG", "Is Successful: " + response.isSuccessful());

                // Log request details
                Log.d("LOGIN_DEBUG", "Request URL: " + call.request().url());

                if (response.isSuccessful() && response.body() != null) {

                    UserResponse loginResponse = response.body();

                    Log.d("LOGIN_DEBUG", "Body is not null");
                    Log.d("LOGIN_DEBUG", "isSuccess(): " + loginResponse.isSuccess());
                    Log.d("LOGIN_DEBUG", "getMessage(): " + loginResponse.getMessage());
                    Log.d("LOGIN_DEBUG", "getToken(): " + loginResponse.getToken());
                    Log.d("LOGIN_DEBUG", "getUser(): " + loginResponse.getUser());

                    if (!loginResponse.isSuccess()) {
                        showToast(loginResponse.getMessage());
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnLogin.setVisibility(View.VISIBLE);
                        return;
                    }

                    // Extract data from API
                    String token = loginResponse.getToken();
                    User user = loginResponse.getUser();

                    // Save in SharedPreferences
                    SessionManager sessionManager = new SessionManager(LoginActivity.this);
                    sessionManager.saveLoginSession(
                            token,
                            user.getUsername(),
                            user.getEmail()
                    );

                    showToast("Login Successful");
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnLogin.setVisibility(View.VISIBLE);

                    // Send data to next screen
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("username", user.getUsername());
                    intent.putExtra("email", user.getEmail());
                    intent.putExtra("token", token);

                    startActivity(intent);
                    finish();

                } else {
                    // ===== DETAILED ERROR LOGGING =====
                    Log.e("LOGIN_DEBUG", "Response NOT successful or body is null");
                    Log.e("LOGIN_DEBUG", "Response code: " + response.code());

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("LOGIN_DEBUG", "Error Body: " + errorBody);
                            showToast("Error: " + errorBody);
                        } else {
                            showToast("Invalid Credentials");
                        }
                    } catch (Exception e) {
                        Log.e("LOGIN_DEBUG", "Error reading error body", e);
                        showToast("Invalid Credentials");
                    }

                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnLogin.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable throwable) {
                showToast("API Failure: " + throwable.getLocalizedMessage());
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setVisibility(View.VISIBLE);
            }
        });
    }
    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PLAY_GAMES) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                String googleId = account.getId();
                String name = account.getDisplayName();
                String email = account.getEmail();
                String photoUrl = account.getPhotoUrl() != null
                        ? account.getPhotoUrl().toString()
                        : "";

                String idToken = account.getIdToken();

                // 🔥 send token + user data
                sendTokenToBackend(
                        idToken,
                        googleId,
                        name,
                        email,
                        photoUrl
                );

            } catch (ApiException e) {
                Toast.makeText(this,
                        "Play Games sign-in failed: " + e.getStatusCode(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendTokenToBackend(
            String idToken,
            String googleId,
            String name,
            String email,
            String photoUrl
    ) {

        SessionManager sessionManager = new SessionManager(this);

        String json = "{"
                + "\"idToken\":\"" + idToken + "\","
                + "\"googleId\":\"" + googleId + "\","
                + "\"name\":\"" + name + "\","
                + "\"email\":\"" + email + "\","
                + "\"photo\":\"" + photoUrl + "\""
                + "}";

        RequestBody body = RequestBody.create(
                json,
                MediaType.parse("application/json")
        );

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.playGamesLogin(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseStr = response.body().string();
                        JSONObject obj = new JSONObject(responseStr);

                        String jwt = obj.getString("token");

                        // 🔐 Save session
                        sessionManager.saveToken(jwt);

                        // 🚀 SEND DATA VIA INTENT
                        Intent intent = new Intent(
                                LoginActivity.this,
                                MainActivity.class
                        );

                        intent.putExtra("token", jwt);
                        intent.putExtra("googleId", googleId);
                        intent.putExtra("name", name);
                        intent.putExtra("email", email);
                        intent.putExtra("photo", photoUrl);

                        startActivity(intent);
                        finish();

                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Server rejected login",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LoginActivity.this,
                        "Network error",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}