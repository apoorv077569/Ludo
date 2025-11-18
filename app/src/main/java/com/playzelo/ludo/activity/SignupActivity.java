package com.playzelo.ludo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.playzelo.ludo.apiservice.ApiClient;
import com.playzelo.ludo.apiservice.ApiService;
import com.playzelo.ludo.databinding.ActivitySignupBinding;
import com.playzelo.ludo.models.User;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        signUp();
        redirectToLogin();
    }

    private void signUp() {
        binding.btnSignUp.setOnClickListener(
                view -> {
//                    Intent intent = new Intent(SignupActivity.this,MainActivity.class);
//                    startActivity(intent);
                    String username = binding.usernameInput.getText().toString().trim();
                    String email = binding.emailInput.getText().toString().trim();
                    String password = binding.passwordInput.getText().toString().trim();

                    if(username.isEmpty() || email.isEmpty() || password.isEmpty()){
                        showToast("All fields are required");
                        return;
                    }
                    ApiService apiService = ApiClient.getClient().create(ApiService.class);
                    Map<String,String> body = new HashMap<>();
                    body.put("username",username);
                    body.put("email",email);
                    body.put("password",password);

                    apiService.signup(body).enqueue(new Callback<>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if (response.isSuccessful() && response.body()!=null){
                                showToast("Signup Successfull");
                                startActivity(new Intent(SignupActivity.this,LoginActivity.class));
                            }
                            showToast("Error in Signup");
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable throwable) {
                            showToast("API Failure");
                        }
                    });
                }
        );
    }

    private void redirectToLogin() {
        binding.switchText.setOnClickListener(view -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        });
    }
    private void showToast(String message){
        Toast.makeText(SignupActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}