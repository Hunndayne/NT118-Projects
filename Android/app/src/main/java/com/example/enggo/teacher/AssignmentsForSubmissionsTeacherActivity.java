package com.example.enggo.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignmentsForSubmissionsTeacherActivity extends BaseTeacherActivity {

    public static final String EXTRA_COURSE_ID = "course_id";
    public static final String EXTRA_COURSE_NAME = "course_name";

    private Long courseId;
    private List<AssignmentResponse> assignments;
    private AssignmentSubmissionsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assignments_for_submissions_teacher);

        setupTeacherHeader();
        setupTeacherFooter();

        courseId = getIntent().getLongExtra(EXTRA_COURSE_ID, -1);
        if (courseId == -1) {
            Toast.makeText(this, "Missing course id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvBack = findViewById(R.id.tvBack);
        TextView tvTitle = findViewById(R.id.tvAssignmentsTitle);
        if (tvTitle != null) {
            String courseName = getIntent().getStringExtra(EXTRA_COURSE_NAME);
            if (courseName != null && !courseName.trim().isEmpty()) {
                tvTitle.setText(courseName + " - Assignments");
            }
        }

        tvBack.setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recyclerAssignments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        assignments = new ArrayList<>();
        adapter = new AssignmentSubmissionsAdapter(assignments, assignment -> {
            Intent intent = new Intent(this, SubmissionListActivity.class);
            intent.putExtra(SubmissionListActivity.EXTRA_COURSE_ID, courseId);
            intent.putExtra(SubmissionListActivity.EXTRA_ASSIGNMENT_ID, assignment.id);
            intent.putExtra(SubmissionListActivity.EXTRA_ASSIGNMENT_TITLE, assignment.title);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        loadAssignments();
    }

    private void loadAssignments() {
        String token = getTokenFromDb();
        Log.d("AssignmentsForSubmissions", "Loading assignments for courseId: " + courseId);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAssignments(token, courseId).enqueue(new Callback<List<AssignmentResponse>>() {
            @Override
            public void onResponse(Call<List<AssignmentResponse>> call, Response<List<AssignmentResponse>> response) {
                Log.d("AssignmentsForSubmissions", "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    assignments.clear();
                    assignments.addAll(response.body());
                    Log.d("AssignmentsForSubmissions", "Loaded " + assignments.size() + " assignments");
                    adapter.notifyDataSetChanged();
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorBody = e.getMessage();
                    }
                    Log.e("AssignmentsForSubmissions", "Load failed: " + response.code() + " - " + errorBody);
                    Toast.makeText(
                            AssignmentsForSubmissionsTeacherActivity.this,
                            "Load assignments failed: " + response.code(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<List<AssignmentResponse>> call, Throwable t) {
                Log.e("AssignmentsForSubmissions", "Network error: " + t.getMessage(), t);
                Toast.makeText(
                        AssignmentsForSubmissionsTeacherActivity.this,
                        "Cannot connect to server: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
