package com.example.enggo.user;

import com.example.enggo.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.enggo.admin.HomeAdminActivity;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.api.LoginRequest;
import com.example.enggo.api.LoginResponse;
import com.example.enggo.database.Database;

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
        etPassword = findViewById(R.id.etPassword);

        androidx.appcompat.widget.AppCompatButton btnLogin = findViewById(R.id.btnLogin);
        TextView tvForgetPassword = findViewById(R.id.fg);

        dao = new Database.Dao(this);

        // ✅ BẤM LOGIN LÀ GỌI API THẬT (KHÔNG HARDCODE)
        btnLogin.setOnClickListener(v -> loginUser());

        tvForgetPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Username và password không được trống", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        LoginRequest loginRequest = new LoginRequest(username, password);

        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginSuccess(response.body());
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Login thất bại. Kiểm tra lại username/password.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "Login API failed", t);
                Toast.makeText(LoginActivity.this,
                        "Không kết nối được server. Kiểm tra IP/port và mạng.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLoginSuccess(LoginResponse loginResponse) {
        // Clear old tokens
        dao.deleteAll();

        // Save new token + role
        Database.Item newItem = new Database.Item();
        newItem.token = loginResponse.token;
        newItem.dateToken = new Date().getTime();
        newItem.isAdmin = loginResponse.admin ? 1 : 0;
        dao.insert(newItem);

        // Route by role from backend
        if (loginResponse.admin) {
            goToAdminDashboard();
        } else {
            goToUserHome();
        }
    }

    private void goToUserHome() {
        Intent intent = new Intent(this, HomeUserActivity.class);
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
