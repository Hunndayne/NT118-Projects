package com.example.enggo;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SubmitHomeworkActivity extends BaseActivity{
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
