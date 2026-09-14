package com.playzelo.ludo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
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
        binding.btnPlayGames.setOnClickListener(v -> startPlayGamesLogin());
    }

    private void startPlayGamesLogin() {
        if (googleSignInClient == null) {
            showToast("Google signin is not initialised");
            return;
        }
        binding.progressPlayGames.setVisibility(View.GONE);
        binding.progressPlayGames.setVisibility(View.VISIBLE);
        binding.progressPlayGames.setVisibility(View.VISIBLE);
        binding.btnPlayGames.setEnabled(false);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_PLAY_GAMES);
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
    }

    private void login() {

        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        binding.btnLogin.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

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
                    intent.putExtra("userId", user.getId());
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

                  // 🔍 PRINT AUD FROM ID TOKEN (ANDROID)
                  try {
                      String[] parts = idToken.split("\\.");
                      if (parts.length >= 2) {
                          String payloadJson = new String(
                                  android.util.Base64.decode(
                                          parts[1],
                                          android.util.Base64.URL_SAFE
                                  )
                          );

                          JSONObject payload = new JSONObject(payloadJson);

                          Log.e("GOOGLE_AUD", "aud = " + payload.optString("aud"));
                          Log.e("GOOGLE_AUD", "azp = " + payload.optString("azp"));
                          Log.e("GOOGLE_AUD", "iss = " + payload.optString("iss"));
                          Log.e("GOOGLE_AUD", "email = " + payload.optString("email"));
                      }
                  } catch (Exception e) {
                      Log.e("GOOGLE_AUD", "Failed to decode ID token", e);
                  }


                  // ✅ DEBUG + GUARD (CORRECT PLACE)
                  Log.e("GOOGLE_DEBUG", "ID TOKEN = " + idToken);
                  Log.e("GOOGLE_DEBUG", "TOKEN LENGTH = " + (idToken != null ? idToken.length() : 0));

                  if (idToken == null) {
                      Toast.makeText(
                              this,
                              "Google ID Token is null. Add your email as TEST USER in OAuth screen.",
                              Toast.LENGTH_LONG
                      ).show();

                      binding.progressPlayGames.setVisibility(View.GONE);
                      binding.btnPlayGames.setEnabled(true);
                      return; // ❗ STOP HERE
                  }

                  // 🔥 EXISTING LOGIC (UNCHANGED)
                  sendTokenToBackend(
                          idToken,
                          googleId,
                          name,
                          email,
                          photoUrl
                  );

              } catch (ApiException e) {
                  binding.progressPlayGames.setVisibility(View.GONE);
                  binding.btnPlayGames.setVisibility(View.VISIBLE);

                  Toast.makeText(
                          this,
                          "Play Games sign-in failed: " + e.getStatusCode(),
                          Toast.LENGTH_LONG
                  ).show();
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

        binding.progressPlayGames.setVisibility(View.VISIBLE);
        binding.btnPlayGames.setVisibility(View.GONE);

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

        apiService.playGamesLogin(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseStr = response.body().string();
                        JSONObject obj = new JSONObject(responseStr);

                        String jwt = obj.getString("token");

                        JSONObject userObj = obj.getJSONObject("user");
                        String mongoUserId = userObj.getString("id");

                        // 🔐 Save session
                        sessionManager.saveLoginSession(
                                jwt,
                                name,
                                email
                        );
                        // 👉 ADD before any return
                        binding.progressPlayGames.setVisibility(View.GONE);
                        binding.btnPlayGames.setVisibility(View.VISIBLE);


                        // 🚀 SEND DATA VIA INTENT
                        Intent intent = new Intent(
                                LoginActivity.this,
                                MainActivity.class
                        );

                        intent.putExtra("token", jwt);
                        intent.putExtra("userId", mongoUserId);
                        intent.putExtra("username", name);
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
                    try {
                        String error = response.errorBody().string();
                        Log.e("PLAY_GAMES", "Server Error: " + error);
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    binding.progressPlayGames.setVisibility(View.GONE);
                    binding.btnPlayGames.setVisibility(View.VISIBLE);
                    binding.btnPlayGames.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                binding.progressPlayGames.setVisibility(View.GONE);
                binding.btnPlayGames.setVisibility(View.VISIBLE);
                binding.btnPlayGames.setEnabled(true);

                Toast.makeText(LoginActivity.this,
                        "Network error",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}