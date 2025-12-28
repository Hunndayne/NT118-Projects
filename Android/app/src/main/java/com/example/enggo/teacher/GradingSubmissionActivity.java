package com.example.enggo.teacher;

import com.example.enggo.R;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.URLUtil;

import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GradingSubmissionActivity extends BaseTeacherActivity {
    public static final String EXTRA_SUBMISSION_INDEX = "submission_index";
    public static final String EXTRA_SUBMISSION_IDS = "submission_ids";
    public static final String EXTRA_STUDENT_NAMES = "student_names";
    public static final String EXTRA_SUBMITTED_FLAGS = "submitted_flags";
    public static final String EXTRA_SUBMITTED_ATS = "submitted_ats";
    public static final String EXTRA_FILE_URLS = "file_urls";
    public static final String EXTRA_DEADLINES = "deadlines";
    public static final String EXTRA_SCORES = "scores";

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
    private Button btnSaveAndNext;
    private Button btnSaveOnly;
    private Long assignmentId;
    private Long submissionId;
    private String submissionFileUrl;
    private long[] submissionIds;
    private String[] studentNames;
    private boolean[] submittedFlags;
    private String[] submittedAts;
    private String[] fileUrls;
    private String[] deadlines;
    private double[] scores;
    private int submissionIndex = -1;

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
        btnSaveAndNext = findViewById(R.id.btnSaveAndNext);
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
        assignmentId = getIntent().getLongExtra("assignment_id", -1);

        submissionIds = getIntent().getLongArrayExtra(EXTRA_SUBMISSION_IDS);
        studentNames = getIntent().getStringArrayExtra(EXTRA_STUDENT_NAMES);
        submittedFlags = getIntent().getBooleanArrayExtra(EXTRA_SUBMITTED_FLAGS);
        submittedAts = getIntent().getStringArrayExtra(EXTRA_SUBMITTED_ATS);
        fileUrls = getIntent().getStringArrayExtra(EXTRA_FILE_URLS);
        deadlines = getIntent().getStringArrayExtra(EXTRA_DEADLINES);
        scores = getIntent().getDoubleArrayExtra(EXTRA_SCORES);
        submissionIndex = getIntent().getIntExtra(EXTRA_SUBMISSION_INDEX, -1);

        if (isValidSubmissionIndex(submissionIndex)) {
            applySubmissionAtIndex(submissionIndex);
            return;
        }

        String studentName = getIntent().getStringExtra("student_name");
        boolean isSubmitted = getIntent().getBooleanExtra("is_submitted", true);
        String submittedAt = getIntent().getStringExtra("submitted_at");
        String fileUrl = getIntent().getStringExtra("file_url");
        String deadline = getIntent().getStringExtra("deadline");
        String statusLabel = getIntent().getStringExtra("score");
        submissionId = getIntent().getLongExtra("submission_id", -1);
        double scoreValue = getIntent().getDoubleExtra("score_value", Double.NaN);

        bindSubmissionData(studentName, isSubmitted, submittedAt, fileUrl, deadline, statusLabel, scoreValue);
        updateSaveAndNextState();
    }

    private void applySubmissionAtIndex(int index) {
        String studentName = safeArrayValue(studentNames, index);
        boolean isSubmitted = submittedFlags != null && index >= 0 && index < submittedFlags.length && submittedFlags[index];
        String submittedAt = safeArrayValue(submittedAts, index);
        String fileUrl = safeArrayValue(fileUrls, index);
        String deadline = safeArrayValue(deadlines, index);
        double scoreValue = scores != null && index >= 0 && index < scores.length ? scores[index] : Double.NaN;

        submissionId = submissionIds != null && index >= 0 && index < submissionIds.length ? submissionIds[index] : -1;
        String statusLabel = buildStatusLabel(isSubmitted, deadline, scoreValue);

        bindSubmissionData(studentName, isSubmitted, submittedAt, fileUrl, deadline, statusLabel, scoreValue);
        updateSaveAndNextState();
    }

    private void bindSubmissionData(String studentName,
                                    boolean isSubmitted,
                                    String submittedAt,
                                    String fileUrl,
                                    String deadline,
                                    String statusLabel,
                                    double scoreValue) {
        if (studentName != null && tvTitle != null) {
            tvTitle.setText("Grading: " + studentName);
        }
        if (tvStudentName != null) {
            tvStudentName.setText(studentName == null ? "-" : studentName);
        }
        if (tvSubmissionStatusTag != null) {
            updateStatusTag(tvSubmissionStatusTag, statusLabel);
        }
        if (etGrade != null) {
            if (!Double.isNaN(scoreValue)) {
                if (scoreValue == Math.floor(scoreValue)) {
                    etGrade.setText(String.valueOf((int) scoreValue));
                } else {
                    etGrade.setText(String.valueOf(scoreValue));
                }
            } else {
                etGrade.setText("");
            }
        }
        if (etFeedback != null) {
            etFeedback.setText("");
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
        submissionFileUrl = fileUrl;
        if (!isSubmitted || submissionFileUrl == null || submissionFileUrl.trim().isEmpty()) {
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
                tvSubmissionFileName.setText(extractFileName(submissionFileUrl));
            }
        }
    }

    private String buildStatusLabel(boolean isSubmitted, String deadline, double scoreValue) {
        if (!isSubmitted) {
            if (isPastDeadline(deadline)) {
                return "Missing";
            }
            return "No submission";
        }
        if (!Double.isNaN(scoreValue)) {
            return formatScore(scoreValue) + "/100";
        }
        return "Submitted";
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
        if (layoutSubmissionFile != null) {
            layoutSubmissionFile.setOnClickListener(v -> openSubmissionFile());
        }
        if (tvSubmissionFileName != null) {
            tvSubmissionFileName.setOnClickListener(v -> openSubmissionFile());
        }
        if (btnSaveAndNext != null) {
            btnSaveAndNext.setOnClickListener(v -> submitGrade(true));
        }
        if (btnSaveOnly != null) {
            btnSaveOnly.setOnClickListener(v -> submitGrade(false));
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

    private void submitGrade(boolean goNext) {
        if (assignmentId == null || assignmentId == -1 || submissionId == null || submissionId == -1) {
            if (goNext) {
                moveToNextSubmission();
            } else {
                Toast.makeText(this, "No submission to grade", Toast.LENGTH_SHORT).show();
            }
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
                            if (goNext) {
                                moveToNextSubmission();
                            } else {
                                finish();
                            }
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

    private void moveToNextSubmission() {
        if (!isValidSubmissionIndex(submissionIndex)) {
            finish();
            return;
        }
        int nextIndex = submissionIndex + 1;
        if (!isValidSubmissionIndex(nextIndex)) {
            finish();
            return;
        }
        submissionIndex = nextIndex;
        applySubmissionAtIndex(submissionIndex);
    }

    private void updateSaveAndNextState() {
        if (btnSaveAndNext == null) {
            return;
        }
        boolean hasNext = isValidSubmissionIndex(submissionIndex + 1);
        btnSaveAndNext.setEnabled(hasNext);
        btnSaveAndNext.setAlpha(hasNext ? 1f : 0.5f);
    }

    private boolean isValidSubmissionIndex(int index) {
        return submissionIds != null && index >= 0 && index < submissionIds.length;
    }

    private String safeArrayValue(String[] values, int index) {
        if (values == null || index < 0 || index >= values.length) {
            return null;
        }
        return values[index];
    }

    private String formatScore(double score) {
        if (score == Math.floor(score)) {
            return String.valueOf((int) score);
        }
        return String.valueOf(score);
    }

    private void openSubmissionFile() {
        if (submissionFileUrl == null || submissionFileUrl.trim().isEmpty()) {
            Toast.makeText(this, "No file to download", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = submissionFileUrl.trim();
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return;
        }
        String fileName = extractFileName(url);
        String safeName = fileName == null || fileName.trim().isEmpty()
                ? URLUtil.guessFileName(url, null, null)
                : fileName;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName);
        request.setTitle(safeName);
        request.setDescription("Downloading submission");
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            Toast.makeText(this, "Downloading " + safeName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show();
        }
    }
}
