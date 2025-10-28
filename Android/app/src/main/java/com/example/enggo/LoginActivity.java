package com.example.enggo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        androidx.appcompat.widget.AppCompatButton btnLogin = findViewById(R.id.btnLogin);
        EditText etUsername = findViewById(R.id.etUsername);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            if (username.equals("admin")) {
                Intent intent = new Intent(LoginActivity.this, HomeAdminActivity.class);
                startActivity(intent);
            } else if (username.equals("user")) {
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        TextView tvForgetPassword = findViewById(R.id.fg);
        tvForgetPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }
}
