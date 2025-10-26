package com.example.enggo;

import android.content.Intent;
import android.os.Bundle;

import android.widget.LinearLayout;
import android.widget.TextView;

public class EditInformationActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_information); // file profile.xml trong res/layout

        // Xử lý nút Back
        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());
        setupHeader();
        setupFooter();
        LinearLayout userInfoLayout = findViewById(R.id.userInfoLayout);
        userInfoLayout.setOnClickListener(v -> {
            Intent intent = new Intent(EditInformationActivity.this, ChangeAvatarActivity.class);
            startActivity(intent);
        });
    }
}
