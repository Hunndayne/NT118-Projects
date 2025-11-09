package com.example.enggo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText etUsername;
    private EditText etPassword;
    private Database.Dao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        etUsername = findViewById(R.id.etUsername);
        // Assuming your password field has the ID 'etPassword' in your layout
        etPassword = findViewById(R.id.etPassword);
        androidx.appcompat.widget.AppCompatButton btnLogin = findViewById(R.id.btnLogin);
        TextView tvForgetPassword = findViewById(R.id.fg);

        dao = new Database.Dao(this);

        btnLogin.setOnClickListener(v -> {
            loginUser();
        });

        tvForgetPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Username and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        LoginRequest loginRequest = new LoginRequest(username, password);
        Call<LoginResponse> call = apiService.login(loginRequest);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginSuccess(response.body());
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "Login API call failed: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "An error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLoginSuccess(LoginResponse loginResponse) {
        // Clear old tokens
        dao.deleteAll();

        // Create and save the new token
        Database.Item newItem = new Database.Item();
        newItem.token = loginResponse.token;
        newItem.dateToken = new Date().getTime(); // Current time in milliseconds
        newItem.isAdmin = loginResponse.admin ? 1 : 0;
        dao.insert(newItem);

        // Navigate to the appropriate screen
        if (loginResponse.admin) {
            goToAdminDashboard();
        } else {
            goToUserHome();
        }
    }

    private void goToUserHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToAdminDashboard() {
        Intent intent = new Intent(this, HomeAdminActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
