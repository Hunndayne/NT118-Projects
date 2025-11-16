package com.example.enggo.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.example.enggo.admin.HomeAdminActivity;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.api.CheckLoginResponse;
import com.example.enggo.database.Database;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Database.Dao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dao = new Database.Dao(this);
        checkToken();
    }

    private void checkToken() {
        List<Database.Item> items = dao.getAll();
        if (items.isEmpty()) {
            goToLogin();
            finish(); // Đóng MainActivity để xóa khỏi back stack
            return;
        }

        Database.Item lastToken = items.get(0);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<CheckLoginResponse> call = apiService.checkLogin(lastToken.token);

        call.enqueue(new Callback<CheckLoginResponse>() {
            @Override
            public void onResponse(Call<CheckLoginResponse> call, Response<CheckLoginResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().active) {
                    if (lastToken.isAdmin == 1) {
                        goToAdminDashboard();
                    } else {
                        goToUserHome();
                    }
                } else {
                    goToLogin();
                }
                finish(); // Đóng MainActivity để xóa khỏi back stack
            }

            @Override
            public void onFailure(Call<CheckLoginResponse> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
                goToLogin();
                finish(); // Đóng MainActivity để xóa khỏi back stack
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        // Các cờ này sẽ xóa stack tác vụ và bắt đầu một tác vụ mới cho LoginActivity.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void goToUserHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void goToAdminDashboard() {
        Intent intent = new Intent(this, HomeAdminActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
