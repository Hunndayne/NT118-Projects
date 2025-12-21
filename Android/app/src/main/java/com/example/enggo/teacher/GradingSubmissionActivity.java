package com.example.enggo.teacher;

import com.example.enggo.R;

import android.os.Bundle;
import android.widget.TextView;

public class GradingSubmissionActivity extends BaseTeacherActivity {
    private TextView tvBack;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homework_grading);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        setupListeners();
    }

    private void initViews() {
        tvBack = findViewById(R.id.tvBack);
        tvTitle = findViewById(R.id.tvTitle);
        
        // Load submission data from intent
        if (getIntent() != null) {
            String studentName = getIntent().getStringExtra("student_name");
            String score = getIntent().getStringExtra("score");
            boolean isSubmitted = getIntent().getBooleanExtra("is_submitted", true);
            
            if (studentName != null && tvTitle != null) {
                tvTitle.setText("Grading: " + studentName);
            }
        } else if (tvTitle != null) {
            tvTitle.setText("Homework Grading");
        }
    }

    private void setupListeners() {
        if (tvBack != null) {
            tvBack.setOnClickListener(v -> finish());
        }
    }
}
