package com.example.enggo.user;
import com.example.enggo.R;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProfileUserActivity extends BaseUserActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile); // file profile.xml trong res/layout

        // Xử lý nút Back
        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());
        setupHeader();
        setupFooter();
        LinearLayout userInfoLayout = findViewById(R.id.userInfoLayout);
    }
}