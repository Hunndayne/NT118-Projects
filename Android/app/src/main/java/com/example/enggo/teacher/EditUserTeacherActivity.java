package com.example.enggo.teacher;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.api.UserUpdateRequest;
import com.example.enggo.admin.UserAdmin;
import com.example.enggo.common.ChangeAvatarActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditUserTeacherActivity extends BaseTeacherActivity {
    private TextView tvBack;
    private TextView tvUserName;
    private ImageView imAvatar;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etEmailVisibility;
    private EditText etCity;
    private EditText etCountry;
    private EditText etTimezone;
    private EditText etDescription;
    private EditText etInterests;
    private EditText etPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_teacher);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        setupListeners();
        loadProfileInfo();
    }

    private void initViews() {
        tvBack = findViewById(R.id.tvBack);
        tvUserName = findViewById(R.id.tvUserName);
        imAvatar = findViewById(R.id.imAvatar);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etEmailVisibility = findViewById(R.id.etEmailVisibility);
        etCity = findViewById(R.id.etCity);
        etCountry = findViewById(R.id.etCountry);
        etTimezone = findViewById(R.id.etTimezone);
        etDescription = findViewById(R.id.etDescription);
        etInterests = findViewById(R.id.etInterests);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
    }

    private void setupListeners() {
        if (tvBack != null) {
            tvBack.setOnClickListener(v -> finish());
        }
        
        LinearLayout userInfoLayout = findViewById(R.id.userInfoLayout);
        if (userInfoLayout != null) {
            userInfoLayout.setOnClickListener(v -> {
                Intent intent = new Intent(EditUserTeacherActivity.this, ChangeAvatarActivity.class);
                startActivity(intent);
            });
        }
        
        findViewById(R.id.btnUpdateProfile).setOnClickListener(v -> updateProfile());
    }

    private void loadProfileInfo() {
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getCurrentUser(token).enqueue(new Callback<UserAdmin>() {
            @Override
            public void onResponse(Call<UserAdmin> call, Response<UserAdmin> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(EditUserTeacherActivity.this, "Load profile failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                UserAdmin user = response.body();
                setText(tvUserName, buildDisplayName(user));
                setText(etFirstName, user.getFirstName());
                setText(etLastName, user.getLastName());
                setText(etEmail, user.getEmailAddress());
                setText(etEmailVisibility, user.getEmailVisibility());
                setText(etCity, user.getCity());
                setText(etCountry, user.getCountry());
                setText(etTimezone, user.getTimezone());
                setText(etDescription, user.getDescription());
                setText(etInterests, user.getInterest());
                setText(etPhoneNumber, user.getPhoneNumber());
                loadAvatar(user.getAvatarUrl());
            }

            @Override
            public void onFailure(Call<UserAdmin> call, Throwable t) {
                Toast.makeText(EditUserTeacherActivity.this, "Cannot connect to server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        UserUpdateRequest request = new UserUpdateRequest();
        request.firstName = trimToNull(etFirstName);
        request.lastName = trimToNull(etLastName);
        request.emailAddress = trimToNull(etEmail);
        request.emailVisibility = trimToNull(etEmailVisibility);
        request.city = trimToNull(etCity);
        request.country = trimToNull(etCountry);
        request.timezone = trimToNull(etTimezone);
        request.description = trimToNull(etDescription);
        request.interest = trimToNull(etInterests);
        request.phoneNumber = trimToNull(etPhoneNumber);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.updateCurrentUser(token, request).enqueue(new Callback<UserAdmin>() {
            @Override
            public void onResponse(Call<UserAdmin> call, Response<UserAdmin> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EditUserTeacherActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    UserAdmin user = response.body();
                    setText(tvUserName, buildDisplayName(user));
                } else {
                    Toast.makeText(EditUserTeacherActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserAdmin> call, Throwable t) {
                Toast.makeText(EditUserTeacherActivity.this, "Cannot connect to server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setText(TextView view, String value) {
        if (view == null) {
            return;
        }
        view.setText(value == null ? "" : value);
    }

    private String buildDisplayName(UserAdmin user) {
        if (user == null) {
            return "";
        }
        String fullName = user.getFullName();
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName.trim();
        }
        String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String last = user.getLastName() == null ? "" : user.getLastName().trim();
        String combined = (first + " " + last).trim();
        if (!combined.isEmpty()) {
            return combined;
        }
        return user.getUsername() == null ? "" : user.getUsername();
    }

    private String trimToNull(EditText input) {
        if (input == null) {
            return null;
        }
        String value = input.getText() == null ? "" : input.getText().toString().trim();
        return value.isEmpty() ? null : value;
    }

    private void loadAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty() || imAvatar == null) {
            return;
        }
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL imageUrl = new URL(avatarUrl);
                connection = (HttpURLConnection) imageUrl.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();
                try (InputStream inputStream = connection.getInputStream()) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    runOnUiThread(() -> {
                        imAvatar.setImageTintList(null);
                        imAvatar.clearColorFilter();
                        imAvatar.setImageBitmap(bitmap);
                    });
                }
            } catch (Exception ignored) {
                // no-op
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
