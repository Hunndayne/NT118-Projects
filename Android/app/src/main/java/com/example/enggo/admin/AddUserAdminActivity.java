package com.example.enggo.admin;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.api.CreateUserRequest;
import com.example.enggo.database.Database;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddUserAdminActivity extends BaseAdminActivity {

    private EditText etUsername, etFirstName, etLastName, etEmail, etPhone, etPassword;
    private RadioGroup rgRole;
    private ApiService apiService;
    private Database.Dao dao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_user_admin);

        setupAdminHeader();
        setupAdminFooter();

        etUsername = findViewById(R.id.etUsernameCreateUsr_Admin);
        etFirstName = findViewById(R.id.etFirstNameCreateUsr_Admin);
        etLastName = findViewById(R.id.etLastNameCreateUsr_Admin);
        etEmail = findViewById(R.id.etEmailCreateUsr_Admin);
        etPhone = findViewById(R.id.etPhoneNumberCreateUsr_Admin);
        etPassword = findViewById(R.id.etPasswordCreateUsr_Admin);
        rgRole = findViewById(R.id.rgRoleCreateUsr_Admin);

        Button btnCreate = findViewById(R.id.buttonCreateUsr_Admin);
        Button btnCancel = findViewById(R.id.buttonCancelCreateUsr_Admin);

        apiService = ApiClient.getClient().create(ApiService.class);
        dao = new Database.Dao(this);

        btnCreate.setOnClickListener(v -> createUser());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void createUser() {

        String username = etUsername.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = getSelectedRole();

        if (TextUtils.isEmpty(username)
                || TextUtils.isEmpty(firstName)
                || TextUtils.isEmpty(lastName)
                || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(phone)) {

            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return;
        }

        java.util.List<Database.Item> tokens = dao.getAll();
        if (tokens.isEmpty() || TextUtils.isEmpty(tokens.get(0).token)) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = tokens.get(0).token;

        CreateUserRequest req = new CreateUserRequest();
        req.username = username;
        req.firstName = firstName;
        req.lastName = lastName;
        req.emailAddress = email;
        req.phoneNumber = phone;
        req.password = password;
        req.role = role;
        req.admin = "SUPER_ADMIN".equals(role);

        apiService.createUser(token, req)
                .enqueue(new Callback<UserAdmin>() {

                    @Override
                    public void onResponse(Call<UserAdmin> call,
                                           Response<UserAdmin> response) {

                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    AddUserAdminActivity.this,
                                    "User created successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(
                                    AddUserAdminActivity.this,
                                    "Create user failed (" + response.code() + ")",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserAdmin> call, Throwable t) {
                        Toast.makeText(
                                AddUserAdminActivity.this,
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private String getSelectedRole() {
        int checkedId = rgRole.getCheckedRadioButtonId();
        if (checkedId == R.id.rbRoleAdminCreateUsr_Admin) {
            return "SUPER_ADMIN";
        }
        if (checkedId == R.id.rbRoleTeacherCreateUsr_Admin) {
            return "TEACHER";
        }
        return "STUDENT";
    }
}
