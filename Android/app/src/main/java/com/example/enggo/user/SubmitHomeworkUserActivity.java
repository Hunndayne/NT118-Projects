package com.example.enggo.user;
import com.example.enggo.R;

import android.os.Bundle;
import android.widget.TextView;

public class SubmitHomeworkUserActivity extends BaseUserActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submit_homework);

        // Xử lý nút Back
        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());
        setupHeader();
        setupFooter();
    }
}