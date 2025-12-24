package com.example.enggo.teacher;

import com.example.enggo.R;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GradingSubmissionActivity extends BaseTeacherActivity {
    private TextView tvBack;
    private TextView tvTitle;
    private TextView tvStudentName;
    private TextView tvSubmittedAt;
    private LinearLayout layoutSubmissionFile;
    private TextView tvSubmissionFileName;
    private TextView tvNoSubmission;

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
        tvStudentName = findViewById(R.id.tvStudentName);
        tvSubmittedAt = findViewById(R.id.tvSubmittedAt);
        layoutSubmissionFile = findViewById(R.id.layoutSubmissionFile);
        tvSubmissionFileName = findViewById(R.id.tvSubmissionFileName);
        tvNoSubmission = findViewById(R.id.tvNoSubmission);

        bindSubmission();
    }

    private void bindSubmission() {
        if (getIntent() == null) {
            if (tvTitle != null) {
                tvTitle.setText("Homework Grading");
            }
            return;
        }
        String studentName = getIntent().getStringExtra("student_name");
        boolean isSubmitted = getIntent().getBooleanExtra("is_submitted", true);
        String submittedAt = getIntent().getStringExtra("submitted_at");
        String fileUrl = getIntent().getStringExtra("file_url");
        String deadline = getIntent().getStringExtra("deadline");

        if (studentName != null && tvTitle != null) {
            tvTitle.setText("Grading: " + studentName);
        }
        if (tvStudentName != null) {
            tvStudentName.setText(studentName == null ? "-" : studentName);
        }
        if (tvSubmittedAt != null) {
            if (isSubmitted) {
                tvSubmittedAt.setText("Submitted: " + formatDate(submittedAt));
            } else if (isPastDeadline(deadline)) {
                tvSubmittedAt.setText("Missing");
            } else {
                tvSubmittedAt.setText("No submission");
            }
        }
        if (!isSubmitted || fileUrl == null || fileUrl.trim().isEmpty()) {
            if (layoutSubmissionFile != null) {
                layoutSubmissionFile.setVisibility(View.GONE);
            }
            if (tvNoSubmission != null) {
                tvNoSubmission.setVisibility(isSubmitted ? View.GONE : View.VISIBLE);
            }
        } else {
            if (layoutSubmissionFile != null) {
                layoutSubmissionFile.setVisibility(View.VISIBLE);
            }
            if (tvNoSubmission != null) {
                tvNoSubmission.setVisibility(View.GONE);
            }
            if (tvSubmissionFileName != null) {
                tvSubmissionFileName.setText(extractFileName(fileUrl));
            }
        }
    }

    private String extractFileName(String fileUrl) {
        if (fileUrl == null) {
            return "-";
        }
        int slash = fileUrl.lastIndexOf('/');
        if (slash >= 0 && slash < fileUrl.length() - 1) {
            return fileUrl.substring(slash + 1);
        }
        return fileUrl;
    }

    private String formatDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        SimpleDateFormat[] inputs = new SimpleDateFormat[]{
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mmX", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        };
        for (SimpleDateFormat input : inputs) {
            try {
                Date parsed = input.parse(value.trim());
                if (parsed != null) {
                    return output.format(parsed);
                }
            } catch (ParseException ignored) {
                // try next
            }
        }
        return value;
    }

    private boolean isPastDeadline(String deadline) {
        if (deadline == null || deadline.trim().isEmpty()) {
            return false;
        }
        SimpleDateFormat[] inputs = new SimpleDateFormat[]{
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mmX", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        };
        for (SimpleDateFormat input : inputs) {
            try {
                Date parsed = input.parse(deadline.trim());
                if (parsed != null) {
                    return parsed.before(new Date());
                }
            } catch (ParseException ignored) {
                // try next
            }
        }
        return false;
    }

    private void setupListeners() {
        if (tvBack != null) {
            tvBack.setOnClickListener(v -> finish());
        }
    }
}
