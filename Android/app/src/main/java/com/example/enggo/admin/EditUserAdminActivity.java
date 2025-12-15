package com.example.enggo.admin;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.api.UserUpdateRequest;
import com.example.enggo.database.Database;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditUserAdminActivity extends BaseAdminActivity {

    private EditText etUsername, etFirstName, etLastName,
            etEmail, etPhone, etPassword;

    private ApiService apiService;
    private Database.Dao dao;
    private long userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_user_admin);

        setupAdminHeader();
        setupAdminFooter();

        etUsername = findViewById(R.id.etUsernameEditUsr_Admin);
        etFirstName = findViewById(R.id.etFirstNameEditUsr_Admin);
        etLastName = findViewById(R.id.etLastNameEditUsr_Admin);
        etEmail = findViewById(R.id.etEmailEditUsr_Admin);
        etPhone = findViewById(R.id.etPhoneNumberEditUsr_Admin);
        etPassword = findViewById(R.id.etPasswordEditUsr_Admin);

        Button btnEdit = findViewById(R.id.buttonEditUsr_Admin);
        Button btnCancel = findViewById(R.id.buttonCancelEditUsr_Admin);

        apiService = ApiClient.getClient().create(ApiService.class);
        dao = new Database.Dao(this);

        userId = getIntent().getLongExtra("USER_ID", -1);
        if (userId == -1) {
            finish();
            return;
        }

        loadUser();

        btnEdit.setOnClickListener(v -> updateUser());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadUser() {
        String token = dao.getAll().get(0).token;

        apiService.getUserById(token, userId)
                .enqueue(new Callback<UserAdmin>() {
                    @Override
                    public void onResponse(Call<UserAdmin> call,
                                           Response<UserAdmin> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            UserAdmin u = response.body();
                            etUsername.setText(u.getUsername());
                            etUsername.setEnabled(false);

                            etFirstName.setText(u.getFirstName());
                            etLastName.setText(u.getLastName());
                            etEmail.setText(u.getEmailAddress());
                            etPhone.setText(u.getPhoneNumber());
                        }
                    }

                    @Override
                    public void onFailure(Call<UserAdmin> call, Throwable t) {
                        Toast.makeText(
                                EditUserAdminActivity.this,
                                "Load user failed",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void updateUser() {

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(firstName)
                || TextUtils.isEmpty(lastName)
                || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(phone)) {

            Toast.makeText(this,
                    "Please fill all required fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        UserUpdateRequest req = new UserUpdateRequest();
        req.firstName = firstName;
        req.lastName = lastName;
        req.emailAddress = email;
        req.phoneNumber = phone;
        if (!password.isEmpty()) {
            req.password = password;
        }

        String token = dao.getAll().get(0).token;

        apiService.updateUser(token, userId, req)
                .enqueue(new Callback<UserAdmin>() {
                    @Override
                    public void onResponse(Call<UserAdmin> call,
                                           Response<UserAdmin> response) {

                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    EditUserAdminActivity.this,
                                    "User updated",
                                    Toast.LENGTH_SHORT
                            ).show();
                            finish();
                        } else {
                            Toast.makeText(
                                    EditUserAdminActivity.this,
                                    "Update failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserAdmin> call, Throwable t) {
                        Toast.makeText(
                                EditUserAdminActivity.this,
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
