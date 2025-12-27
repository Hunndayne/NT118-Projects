package com.example.enggo.teacher;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmissionListActivity extends BaseTeacherActivity {
    public static final String EXTRA_COURSE_ID = "course_id";
    public static final String EXTRA_ASSIGNMENT_ID = "assignment_id";
    public static final String EXTRA_ASSIGNMENT_TITLE = "assignment_title";
    private static final int REQ_GRADE = 3001;

    private TextView tvBack;
    private TextView tvAssignmentTitle;
    private TextView tvParticipantsCount;
    private TextView tvSubmittedCount;
    private TextView tvNeedsGradingCount;
    private RecyclerView recyclerSubmissions;
    private SubmissionStatusAdapter adapter;
    private List<SubmissionStatusResponse> submissions;
    private Long courseId;
    private Long assignmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submission_list);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        setupListeners();
        loadData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_GRADE && resultCode == RESULT_OK) {
            loadData();
        }
    }

    private void initViews() {
        tvBack = findViewById(R.id.tvBack);
        tvAssignmentTitle = findViewById(R.id.tvAssignmentTitle);
        tvParticipantsCount = findViewById(R.id.tvParticipantsCount);
        tvSubmittedCount = findViewById(R.id.tvSubmittedCount);
        tvNeedsGradingCount = findViewById(R.id.tvNeedsGradingCount);
        recyclerSubmissions = findViewById(R.id.recyclerSubmissions);
        recyclerSubmissions.setLayoutManager(new LinearLayoutManager(this));
        submissions = new ArrayList<>();
        adapter = new SubmissionStatusAdapter(submissions, this::openSubmissionDetail);
        recyclerSubmissions.setAdapter(adapter);

        Intent intent = getIntent();
        courseId = intent.getLongExtra(EXTRA_COURSE_ID, -1);
        assignmentId = intent.getLongExtra(EXTRA_ASSIGNMENT_ID, -1);
        String assignmentTitle = intent.getStringExtra(EXTRA_ASSIGNMENT_TITLE);
        if (assignmentTitle != null && tvAssignmentTitle != null) {
            tvAssignmentTitle.setText(assignmentTitle);
        }
    }

    private void setupListeners() {
        if (tvBack != null) {
            tvBack.setOnClickListener(v -> finish());
        }
    }

    private void loadData() {
        if (courseId == null || courseId == -1 || assignmentId == null || assignmentId == -1) {
            Toast.makeText(this, "Missing assignment info", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = getTokenFromDb();
        Log.d("SubmissionList", "Loading submissions for courseId: " + courseId + ", assignmentId: " + assignmentId);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getSubmissionStatus(token, courseId, assignmentId)
                .enqueue(new Callback<List<SubmissionStatusResponse>>() {
                    @Override
                    public void onResponse(Call<List<SubmissionStatusResponse>> call,
                                           Response<List<SubmissionStatusResponse>> response) {
                        Log.d("SubmissionList", "Response code: " + response.code());
                        if (response.isSuccessful() && response.body() != null) {
                            submissions.clear();
                            submissions.addAll(response.body());
                            Log.d("SubmissionList", "Loaded " + submissions.size() + " submissions");
                            adapter.notifyDataSetChanged();
                            updateStats();
                        } else {
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                errorBody = e.getMessage();
                            }
                            Log.e("SubmissionList", "Load failed: " + response.code() + " - " + errorBody);
                            Toast.makeText(
                                    SubmissionListActivity.this,
                                    "Load submissions failed: " + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<SubmissionStatusResponse>> call, Throwable t) {
                        Log.e("SubmissionList", "Network error: " + t.getMessage(), t);
                        Toast.makeText(
                                SubmissionListActivity.this,
                                "Cannot connect to server: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void updateStats() {
        int total = submissions.size();
        int submitted = 0;
        int missing = 0;
        for (SubmissionStatusResponse submission : submissions) {
            if (submission.submitted) {
                submitted++;
            } else if (isPastDeadline(submission.deadline)) {
                missing++;
            }
        }
        tvParticipantsCount.setText(String.valueOf(total));
        tvSubmittedCount.setText(String.valueOf(submitted));
        tvNeedsGradingCount.setText(String.valueOf(missing));
    }

    private void openSubmissionDetail(SubmissionStatusResponse submission) {
        boolean isSubmitted = submission.submitted;
        String statusLabel;
        if (!isSubmitted) {
            if (isPastDeadline(submission.deadline)) {
                statusLabel = "Missing";
            } else {
                statusLabel = "No submission";
            }
        } else {
            if (submission.score != null) {
                statusLabel = formatScore(submission.score) + "/100";
            } else {
                statusLabel = "Submitted";
            }
        }
        Intent intent = new Intent(this, GradingSubmissionActivity.class);
        intent.putExtra("student_name", submission.getDisplayName());
        intent.putExtra("score", statusLabel);
        intent.putExtra("is_submitted", isSubmitted);
        intent.putExtra("submitted_at", submission.submittedAt);
        intent.putExtra("file_url", submission.fileUrl);
        intent.putExtra("deadline", submission.deadline);
        intent.putExtra("assignment_id", assignmentId);
        if (submission.submissionId != null) {
            intent.putExtra("submission_id", submission.submissionId);
        }
        if (submission.score != null) {
            intent.putExtra("score_value", submission.score);
        }
        startActivityForResult(intent, REQ_GRADE);
    }

    private String formatScore(Double score) {
        if (score == null) {
            return "";
        }
        if (score == Math.floor(score)) {
            return String.valueOf(score.intValue());
        }
        return String.valueOf(score);
    }

    private boolean isPastDeadline(String deadline) {
        if (deadline == null || deadline.trim().isEmpty()) {
            return false;
        }
        java.text.SimpleDateFormat[] inputs = new java.text.SimpleDateFormat[]{
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", java.util.Locale.getDefault()),
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", java.util.Locale.getDefault()),
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mmX", java.util.Locale.getDefault()),
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm", java.util.Locale.getDefault()),
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()),
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.getDefault()),
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()),
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        };
        for (java.text.SimpleDateFormat input : inputs) {
            try {
                java.util.Date parsed = input.parse(deadline.trim());
                if (parsed != null) {
                    return parsed.before(new java.util.Date());
                }
            } catch (java.text.ParseException ignored) {
                // try next
            }
        }
        return false;
    }
}
