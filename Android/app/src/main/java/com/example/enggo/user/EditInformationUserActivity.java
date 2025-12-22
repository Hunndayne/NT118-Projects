package com.example.enggo.user;
import com.example.enggo.R;
import com.example.enggo.common.ChangeAvatarActivity;

import android.content.Intent;
import android.os.Bundle;

import android.widget.LinearLayout;
import android.widget.TextView;

public class EditInformationUserActivity extends BaseUserActivity {
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
            Intent intent = new Intent(EditInformationUserActivity.this, ChangeAvatarActivity.class);
            startActivity(intent);
        });
    }
}