package com.example.enggo.common;
import com.example.enggo.R;
import com.example.enggo.user.BaseUserActivity;

import android.os.Bundle;
import android.widget.TextView;

public class ChangeAvatarActivity extends BaseUserActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avatar); // file profile.xml trong res/layout
        setupHeader();
        setupFooter();
        // Xử lý nút Back
        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());
    }
}