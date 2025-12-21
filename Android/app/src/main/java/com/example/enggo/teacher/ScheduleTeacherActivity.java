package com.example.enggo.teacher;

import com.example.enggo.R;

import android.os.Bundle;
import android.widget.TextView;

public class ScheduleTeacherActivity extends BaseTeacherActivity {
    private TextView tvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_teacher);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        setupListeners();
    }

    private void initViews() {
        tvBack = findViewById(R.id.tvBack);
    }

    private void setupListeners() {
        if (tvBack != null) {
            tvBack.setOnClickListener(v -> finish());
        }
    }
}
