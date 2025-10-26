package com.example.enggo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ManageAccountAdminActivity extends BaseAdminActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_account_admin);

        setupAdminHeader();
        setupAdminFooter();

        TextView tvBack = findViewById(R.id.tvBack);
        Button btnAddUser = findViewById(R.id.btnAddUser);

        btnAddUser.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddUserAdminActivity.class);
            startActivity(intent);
        });

        tvBack.setOnClickListener(v -> {
            finish();
        });
    }
}
