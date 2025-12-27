package com.example.enggo.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.admin.UserAdmin;
import com.example.enggo.auth.ChangePasswordActivity;

public class MenuAdminActivity extends BaseAdminActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_admin);

        setupAdminHeader();
        setupAdminFooter();

        ListView adminMenuListView = findViewById(R.id.adminMenuListView);
        String[] accountOptions = getResources().getStringArray(R.array.user_account_options);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.list_item_account,
                R.id.tv_item_account,
                accountOptions
        );
        adminMenuListView.setAdapter(adapter);

        adminMenuListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            switch (position) {
                case 0: // Edit Profile
                    Intent intent = new Intent(MenuAdminActivity.this, EditProfileAdminActivity.class);
                    startActivity(intent);
                    break;
                case 1: // Change Password
                    Intent intent1 = new Intent(MenuAdminActivity.this, ChangePasswordActivity.class);
                    startActivity(intent1);
                    break;
            }
        });

        ImageView imAdminAvatar = findViewById(R.id.imAdminAvatar);
        imAdminAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(MenuAdminActivity.this, EditProfileAdminActivity.class);
            startActivity(intent);
        });

        LinearLayout adminInfoLayout = findViewById(R.id.adminInfoLayout);
        adminInfoLayout.setOnClickListener(v -> {
            Intent intent = new Intent(MenuAdminActivity.this, EditProfileAdminActivity.class);
            startActivity(intent);
        });

        TextView tvAdminName = findViewById(R.id.tvAdminName);
        loadAdminName(tvAdminName);
    }

    private void loadAdminName(TextView tvAdminName) {
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getCurrentUser(token).enqueue(new retrofit2.Callback<UserAdmin>() {
            @Override
            public void onResponse(retrofit2.Call<UserAdmin> call,
                                   retrofit2.Response<UserAdmin> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String name = response.body().getFullName();
                    if (name == null || name.trim().isEmpty()) {
                        name = response.body().getUsername();
                    }
                    if (name != null && !name.trim().isEmpty()) {
                        tvAdminName.setText(name);
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<UserAdmin> call, Throwable t) {
                // Keep default label on failure.
            }
        });
    }
}
