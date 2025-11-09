package com.example.enggo;

import android.os.Bundle;
import android.widget.Button;

public class EditUserAdminActivity extends BaseAdminActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_user_admin);
        setupAdminHeader();
        setupAdminFooter();

        Button btnCancel = findViewById(R.id.buttonCancelEditUsr_Admin);
        btnCancel.setOnClickListener(v -> {
            finish();
        });
    }
}
