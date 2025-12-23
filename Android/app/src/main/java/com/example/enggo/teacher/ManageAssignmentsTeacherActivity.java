package com.example.enggo.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

public class ManageAssignmentsTeacherActivity extends BaseTeacherActivity {

    public static final String EXTRA_COURSE_ID = "course_id";
    public static final String EXTRA_COURSE_NAME = "course_name";
    private static final int REQ_ADD_ASSIGNMENT = 2001;
    private static final int REQ_EDIT_ASSIGNMENT = 2002;

    private Long courseId;
    private List<AssignmentResponse> assignments;
    private AssignmentTeacherAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_assignments_teacher);

        setupTeacherHeader();
        setupTeacherFooter();

        courseId = getIntent().getLongExtra(EXTRA_COURSE_ID, -1);
        if (courseId == -1) {
            Toast.makeText(this, "Missing course id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvBack = findViewById(R.id.tvBack);
        TextView tvTitle = findViewById(R.id.tvAdmin_AssignmentsManager);
        if (tvTitle != null) {
            String courseName = getIntent().getStringExtra(EXTRA_COURSE_NAME);
            if (courseName != null && !courseName.trim().isEmpty()) {
                tvTitle.setText(courseName + " - Assignments");
            }
        }

        tvBack.setOnClickListener(v -> finish());

        Button btnAddAssignment = findViewById(R.id.btnAddAssignment);
        btnAddAssignment.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAssignmentTeacherActivity.class);
            intent.putExtra(EXTRA_COURSE_ID, courseId);
            startActivityForResult(intent, REQ_ADD_ASSIGNMENT);
        });

        RecyclerView recyclerView = findViewById(R.id.assignmentsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        assignments = new ArrayList<>();
        adapter = new AssignmentTeacherAdapter(assignments, new AssignmentTeacherAdapter.OnAssignmentActionListener() {
            @Override
            public void onEdit(AssignmentResponse assignment) {
                Intent intent = new Intent(ManageAssignmentsTeacherActivity.this, EditAssignmentTeacherActivity.class);
                intent.putExtra(EXTRA_COURSE_ID, courseId);
                intent.putExtra("assignment_id", assignment.id);
                intent.putExtra("assignment_title", assignment.title);
                intent.putExtra("assignment_content", assignment.description);
                intent.putExtra("attach_link", assignment.attachmentUrl);
                intent.putExtra("start_time", assignment.createdAt);
                intent.putExtra("due_time", assignment.deadline);
                startActivityForResult(intent, REQ_EDIT_ASSIGNMENT);
            }

            @Override
            public void onDelete(AssignmentResponse assignment) {
                confirmDelete(assignment);
            }
        });
        recyclerView.setAdapter(adapter);

        loadAssignments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAssignments();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQ_ADD_ASSIGNMENT || requestCode == REQ_EDIT_ASSIGNMENT) && resultCode == RESULT_OK) {
            loadAssignments();
        }
    }

    private void loadAssignments() {
        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAssignments(token, courseId).enqueue(new Callback<List<AssignmentResponse>>() {
            @Override
            public void onResponse(Call<List<AssignmentResponse>> call, Response<List<AssignmentResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    assignments.clear();
                    assignments.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(
                            ManageAssignmentsTeacherActivity.this,
                            "Load assignments failed",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<List<AssignmentResponse>> call, Throwable t) {
                Toast.makeText(
                        ManageAssignmentsTeacherActivity.this,
                        "Cannot connect to server",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void confirmDelete(AssignmentResponse assignment) {
        new AlertDialog.Builder(this)
                .setTitle("Delete assignment")
                .setMessage("Are you sure you want to delete this assignment?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAssignment(assignment))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAssignment(AssignmentResponse assignment) {
        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.deleteAssignment(token, courseId, assignment.id)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            assignments.remove(assignment);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(
                                    ManageAssignmentsTeacherActivity.this,
                                    "Delete assignment failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(
                                ManageAssignmentsTeacherActivity.this,
                                "Cannot connect to server",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
