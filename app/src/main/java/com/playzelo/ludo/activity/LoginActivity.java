package com.playzelo.ludo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.playzelo.ludo.apiservice.ApiClient;
import com.playzelo.ludo.apiservice.ApiService;
import com.playzelo.ludo.databinding.ActivityLoginBinding;
import com.playzelo.ludo.models.User;
import com.playzelo.ludo.models.UserResponse;
import com.playzelo.ludo.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(v -> login());
        binding.switchText.setOnClickListener(view ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class))
        );
    }

    private void login() {

        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("All fields are required");
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        apiService.login(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    UserResponse loginResponse = response.body();

                    if (!loginResponse.isSuccess()) {
                        showToast(loginResponse.getMessage());
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

                    // Send data to next screen
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("username", user.getUsername());
                    intent.putExtra("email", user.getEmail());
                    intent.putExtra("token", token);

                    startActivity(intent);
                    finish();
                } else {
                    showToast("Invalid Credentials");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable throwable) {
                showToast("API Failure: " + throwable.getLocalizedMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
