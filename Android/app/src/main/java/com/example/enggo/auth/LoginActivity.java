package com.example.enggo.auth;

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
import com.example.enggo.teacher.HomeTeacherActivity;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.api.LoginRequest;
import com.example.enggo.api.LoginResponse;
import com.example.enggo.database.Database;
import com.example.enggo.user.HomeUserActivity;

import java.util.Date;
import java.util.Locale;

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

        // API
        btnLogin.setOnClickListener(v -> loginUser());

        // DEBUG MODE: Bỏ qua login, vào thẳng Admin, Teacher hoặc User
        // btnLogin.setOnClickListener(v -> {
        //     // Chọn 1 trong 3 dòng dưới để debug:
        //     // goToAdminDashboard();   // Debug Admin UI
        //     // goToTeacherDashboard(); // Debug Teacher UI
        //     // goToUserHome();         // Debug User UI
        // });

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

        boolean isAdmin = isRoleAdmin(loginResponse.role, loginResponse.admin);

        // Save new token + role
        Database.Item newItem = new Database.Item();
        newItem.token = loginResponse.token;
        newItem.dateToken = new Date().getTime();
        newItem.isAdmin = isAdmin ? 1 : 0;
        newItem.role = getStoredRole(loginResponse.role, loginResponse.admin);
        dao.insert(newItem);

        // Route by role from backend
        routeByRole(loginResponse.role, loginResponse.admin);
    }

    private String getStoredRole(String role, boolean adminFlag) {
        if (isRoleAdmin(role, adminFlag)) {
            return "SUPER_ADMIN";
        }
        if (isRoleTeacher(role)) {
            return "TEACHER";
        }
        return "STUDENT";
    }

    private void routeByRole(String role, boolean adminFlag) {
        if (isRoleAdmin(role, adminFlag)) {
            goToAdminDashboard();
        } else if (isRoleTeacher(role)) {
            goToTeacherDashboard();
        } else {
            goToUserHome();
        }
    }

    private boolean isRoleAdmin(String role, boolean adminFlag) {
        if (adminFlag) {
            return true;
        }
        if (role == null) {
            return false;
        }
        String normalized = role.trim().toUpperCase(Locale.US);
        return "SUPER_ADMIN".equals(normalized) || "ADMIN".equals(normalized);
    }

    private boolean isRoleTeacher(String role) {
        if (role == null) {
            return false;
        }
        String normalized = role.trim().toUpperCase(Locale.US);
        return "TEACHER".equals(normalized);
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

    private void goToTeacherDashboard() {
        Intent intent = new Intent(this, HomeTeacherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
