package com.example.enggo.admin;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileAdminActivity extends BaseAdminActivity {

    private TextView tvFullName, tvUsername, tvEmail, tvPhone, tvCity, tvCountry, tvDescription, tvLastLogin;
    private ImageView imAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_admin);

        setupAdminHeader();
        setupAdminFooter();

        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());

        // Ánh xạ views
        tvFullName = findViewById(R.id.tvFullName);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvCity = findViewById(R.id.tvCity);
        tvCountry = findViewById(R.id.tvCountry);
        tvDescription = findViewById(R.id.tvDescription);
        tvLastLogin = findViewById(R.id.tvLastLogin);
        imAvatar = findViewById(R.id.imAvatar);

        loadProfileData();
    }

    private void loadProfileData() {
        String token = getTokenFromDb();
        if (token == null) return;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getCurrentUser(token).enqueue(new Callback<UserAdmin>() {
            @Override
            public void onResponse(Call<UserAdmin> call, Response<UserAdmin> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserAdmin user = response.body();

                    tvFullName.setText(user.getFullName() != null ? user.getFullName() : "N/A");
                    tvUsername.setText("@" + user.getUsername() + " (" + (user.getRole() != null ? user.getRole() : "ADMIN") + ")");

                    setTextOrPlaceholder(tvEmail, user.getEmailAddress());
                    setTextOrPlaceholder(tvPhone, user.getPhoneNumber());
                    setTextOrPlaceholder(tvCity, user.getCity());
                    setTextOrPlaceholder(tvCountry, user.getCountry());
                    setTextOrPlaceholder(tvDescription, user.getDescription());

                    if (user.getLastLoginAt() != null) {
                        tvLastLogin.setText(user.getLastLoginAt().replace("T", " ").substring(0, Math.min(16, user.getLastLoginAt().length())));
                    } else {
                        tvLastLogin.setText("Never");
                    }

                    loadAvatarInto(imAvatar);
                } else {
                    Toast.makeText(ProfileAdminActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserAdmin> call, Throwable t) {
                Toast.makeText(ProfileAdminActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setTextOrPlaceholder(TextView view, String text) {
        if (view != null) {
            view.setText((text != null && !text.trim().isEmpty()) ? text : "--");
        }
    }
}