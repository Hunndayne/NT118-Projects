package com.example.enggo.teacher;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.admin.UserAdmin;
import com.example.enggo.user.ProfileUserActivity;
import com.example.enggo.auth.ChangePasswordActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MenuTeacherActivity extends BaseTeacherActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_teacher);

        setupTeacherHeader();
        setupTeacherFooter();

        // Setup back button
        TextView tvBack = findViewById(R.id.tvBackTeacher);
        tvBack.setOnClickListener(v -> finish());

        // Setup avatar and info onclick
        ImageView imTeacherAvatar = findViewById(R.id.imTeacherAvatar);
        LinearLayout teacherInfoLayout = findViewById(R.id.teacherInfoLayout);
        
        imTeacherAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditUserTeacherActivity.class);
            startActivity(intent);
        });
        
        teacherInfoLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditUserTeacherActivity.class);
            startActivity(intent);
        });

        // Setup menu list
        setupMenuOptions();
        
        // Load teacher name
        TextView tvTeacherName = findViewById(R.id.tvTeacherName);
        loadTeacherName(tvTeacherName);
    }

    private void loadTeacherName(TextView tvTeacherName) {
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
                        tvTeacherName.setText(name);
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<UserAdmin> call, Throwable t) {
                // Keep default label on failure.
            }
        });
    }

    private void setupMenuOptions() {
        ListView teacherMenuListView = findViewById(R.id.teacherMenuListView);
        String[] accountOptions = getResources().getStringArray(R.array.user_account_options);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.list_item_account,
                R.id.tv_item_account,
                accountOptions
        );
        teacherMenuListView.setAdapter(adapter);

        teacherMenuListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            switch (position) {
                case 0: // Edit Profile
                    startActivity(new Intent(this, EditUserTeacherActivity.class));
                    break;
                case 1: // Change Password
                    startActivity(new Intent(this, ChangePasswordActivity.class));
                    break;
            }
        });
    }
}
