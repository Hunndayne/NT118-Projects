package com.example.enggo.user;
import com.example.enggo.R;

import android.os.Bundle;
import android.widget.TextView;

public class ChangeAvatarUserActivity extends BaseUserActivity {
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