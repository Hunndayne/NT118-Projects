package com.example.enggo;

import android.os.Bundle;
import android.widget.TextView;

public class ChangeAvatarActivity extends BaseActivity {
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
