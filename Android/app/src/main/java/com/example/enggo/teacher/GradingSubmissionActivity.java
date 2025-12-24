package com.example.enggo.teacher;

import com.example.enggo.R;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GradingSubmissionActivity extends BaseTeacherActivity {
    private TextView tvBack;
    private TextView tvTitle;
    private TextView tvSubmissionStatusTag;
    private TextView tvStudentName;
    private TextView tvSubmittedAt;
    private LinearLayout layoutSubmissionFile;
    private TextView tvSubmissionFileName;
    private TextView tvNoSubmission;
    private EditText etGrade;
    private EditText etFeedback;
    private Button btnSaveOnly;
    private Long assignmentId;
    private Long submissionId;

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
        tvSubmissionStatusTag = findViewById(R.id.tvSubmissionStatusTag);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvSubmittedAt = findViewById(R.id.tvSubmittedAt);
        layoutSubmissionFile = findViewById(R.id.layoutSubmissionFile);
        tvSubmissionFileName = findViewById(R.id.tvSubmissionFileName);
        tvNoSubmission = findViewById(R.id.tvNoSubmission);
        etGrade = findViewById(R.id.etGrade);
        etFeedback = findViewById(R.id.etFeedback);
        btnSaveOnly = findViewById(R.id.btnSaveOnly);

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
        String statusLabel = getIntent().getStringExtra("score");
        assignmentId = getIntent().getLongExtra("assignment_id", -1);
        submissionId = getIntent().getLongExtra("submission_id", -1);
        double scoreValue = getIntent().getDoubleExtra("score_value", Double.NaN);

        if (studentName != null && tvTitle != null) {
            tvTitle.setText("Grading: " + studentName);
        }
        if (tvStudentName != null) {
            tvStudentName.setText(studentName == null ? "-" : studentName);
        }
        if (tvSubmissionStatusTag != null) {
            updateStatusTag(tvSubmissionStatusTag, statusLabel);
        }
        if (etGrade != null && !Double.isNaN(scoreValue)) {
            if (scoreValue == Math.floor(scoreValue)) {
                etGrade.setText(String.valueOf((int) scoreValue));
            } else {
                etGrade.setText(String.valueOf(scoreValue));
            }
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
        if (btnSaveOnly != null) {
            btnSaveOnly.setOnClickListener(v -> submitGrade());
        }
    }

    private void updateStatusTag(TextView view, String statusLabel) {
        if (statusLabel == null || statusLabel.trim().isEmpty()) {
            statusLabel = "No submission";
        }
        String normalized = statusLabel.trim().toLowerCase();
        view.setText(statusLabel);
        if (normalized.contains("missing")) {
            view.setBackgroundColor(0xFFFFEBEE);
            view.setTextColor(0xFFD32F2F);
        } else if (normalized.contains("no submission")) {
            view.setBackgroundColor(0xFFF3E5F5);
            view.setTextColor(0xFF6A1B9A);
        } else {
            view.setBackgroundColor(0xFFE8F5E9);
            view.setTextColor(0xFF2E7D32);
        }
    }

    private void submitGrade() {
        if (assignmentId == null || assignmentId == -1 || submissionId == null || submissionId == -1) {
            Toast.makeText(this, "No submission to grade", Toast.LENGTH_SHORT).show();
            return;
        }
        String rawScore = etGrade != null ? etGrade.getText().toString().trim() : "";
        Double score = null;
        if (!rawScore.isEmpty()) {
            try {
                score = Double.parseDouble(rawScore);
            } catch (NumberFormatException ignored) {
                Toast.makeText(this, "Invalid score", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String feedback = etFeedback != null ? etFeedback.getText().toString().trim() : null;
        GradeSubmissionRequest request = new GradeSubmissionRequest(score, feedback, "GRADED");

        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.gradeSubmission(token, assignmentId, submissionId, request)
                .enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                        if (response.isSuccessful()) {
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(
                                    GradingSubmissionActivity.this,
                                    "Save grade failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                        Toast.makeText(
                                GradingSubmissionActivity.this,
                                "Cannot connect to server",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
