package com.example.enggo.teacher;

import com.example.enggo.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

public class SubmissionListActivity extends BaseTeacherActivity {
    private TextView tvBack;
    private CardView cardSubmission0;
    private CardView cardSubmission1;
    private CardView cardSubmission2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submission_list);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        setupListeners();
    }

    private void initViews() {
        tvBack = findViewById(R.id.tvBack);
        cardSubmission0 = findViewById(R.id.cardSubmission0);
        cardSubmission1 = findViewById(R.id.cardSubmission1);
        cardSubmission2 = findViewById(R.id.cardSubmission2);
    }

    private void setupListeners() {
        if (tvBack != null) {
            tvBack.setOnClickListener(v -> finish());
        }

        if (cardSubmission0 != null) {
            cardSubmission0.setOnClickListener(v -> openSubmissionDetail("Nguyen Van A", "Needs Grading", true));
        }

        if (cardSubmission1 != null) {
            cardSubmission1.setOnClickListener(v -> openSubmissionDetail("Tran Thi B", "85/100", true));
        }

        if (cardSubmission2 != null) {
            cardSubmission2.setOnClickListener(v -> openSubmissionDetail("Le Van C", "Missing", false));
        }
    }

    private void openSubmissionDetail(String studentName, String score, boolean isSubmitted) {
        Intent intent = new Intent(this, GradingSubmissionActivity.class);
        intent.putExtra("student_name", studentName);
        intent.putExtra("score", score);
        intent.putExtra("is_submitted", isSubmitted);
        startActivity(intent);
    }
}
