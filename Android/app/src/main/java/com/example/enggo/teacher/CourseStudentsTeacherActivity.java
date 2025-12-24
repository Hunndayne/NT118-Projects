package com.example.enggo.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.enggo.R;
import com.example.enggo.admin.CourseParticipant;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseStudentsTeacherActivity extends BaseTeacherActivity {
    private TextView tvBack;
    private TextView tvCourseName;
    private TextView tvTotalStudents;
    private TextView tvActiveStudents;
    private EditText etSearchStudent;
    private ImageButton btnFilter;
    private Long courseId;
    private RecyclerView recyclerStudents;
    private List<CourseParticipant> students;
    private List<CourseParticipant> allStudents;
    private StudentListTeacherAdapter adapter;
    private String currentQuery;
    private int sortOrder = 0;
    private int statusFilter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_students_teacher);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        loadCourseData();
        setupListeners();
    }

    private void initViews() {
        tvBack = findViewById(R.id.tvBack);
        tvCourseName = findViewById(R.id.tvCourseName);
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvActiveStudents = findViewById(R.id.tvActiveStudents);
        etSearchStudent = findViewById(R.id.etSearchStudent);
        btnFilter = findViewById(R.id.btnFilter);
        recyclerStudents = findViewById(R.id.recyclerStudents);
        recyclerStudents.setLayoutManager(new LinearLayoutManager(this));
        students = new ArrayList<>();
        allStudents = new ArrayList<>();
        adapter = new StudentListTeacherAdapter(students);
        recyclerStudents.setAdapter(adapter);
    }

    private void loadCourseData() {
        // Get course data from intent
        Intent intent = getIntent();
        if (intent != null) {
            String courseName = intent.getStringExtra("course_name");
            courseId = intent.getLongExtra("course_id", -1);
            
            if (courseName != null && tvCourseName != null) {
                tvCourseName.setText(courseName + " - Students");
            }
        }

        loadStudentCounts();
    }

    private void setupListeners() {
        if (tvBack != null) {
            tvBack.setOnClickListener(v -> finish());
        }

        if (etSearchStudent != null) {
            etSearchStudent.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // no-op
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentQuery = s == null ? null : s.toString();
                    applyFilter();
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // no-op
                }
            });
        }

        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterOptions());
        }
    }

    private void loadStudentCounts() {
        if (courseId == null || courseId == -1) {
            return;
        }
        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getCourseParticipants(token, courseId).enqueue(new Callback<List<CourseParticipant>>() {
            @Override
            public void onResponse(Call<List<CourseParticipant>> call, Response<List<CourseParticipant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CourseParticipant> participantList = response.body();
                    int studentCount = 0;
                    int activeCount = 0;
                    allStudents.clear();
                    for (CourseParticipant participant : participantList) {
                        if (participant.getRole() == null || "STUDENT".equalsIgnoreCase(participant.getRole())) {
                            studentCount++;
                            if (participant.isActive()) {
                                activeCount++;
                            }
                            allStudents.add(participant);
                        }
                    }
                    tvTotalStudents.setText(String.valueOf(studentCount));
                    tvActiveStudents.setText(String.valueOf(activeCount));
                    applyFilter();
                } else {
                    showCountFallback();
                }
            }

            @Override
            public void onFailure(Call<List<CourseParticipant>> call, Throwable t) {
                showCountFallback();
            }
        });
    }

    private void showCountFallback() {
        if (tvTotalStudents != null) {
            tvTotalStudents.setText("0");
        }
        if (tvActiveStudents != null) {
            tvActiveStudents.setText("0");
        }
        students.clear();
        allStudents.clear();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Load students failed", Toast.LENGTH_SHORT).show();
    }

    private void applyFilter() {
        String query = currentQuery == null ? "" : currentQuery.trim().toLowerCase();
        students.clear();
        for (CourseParticipant participant : allStudents) {
            if (statusFilter == 1 && !participant.isActive()) {
                continue;
            }
            if (statusFilter == 2 && participant.isActive()) {
                continue;
            }
            String name = participant.getDisplayName().toLowerCase();
            if (query.isEmpty() || name.contains(query)) {
                students.add(participant);
            }
        }
        if (sortOrder != 0) {
            Collections.sort(students, Comparator.comparing(p -> p.getDisplayName().toLowerCase()));
            if (sortOrder == 2) {
                Collections.reverse(students);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showFilterOptions() {
        String[] options = new String[]{"Sort A-Z", "Sort Z-A", "Filter Active", "Filter Deactive"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Filter")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            sortOrder = 1;
                            statusFilter = 0;
                            break;
                        case 1:
                            sortOrder = 2;
                            statusFilter = 0;
                            break;
                        case 2:
                            statusFilter = 1;
                            sortOrder = 0;
                            break;
                        case 3:
                            statusFilter = 2;
                            sortOrder = 0;
                            break;
                        default:
                            break;
                    }
                    applyFilter();
                })
                .show();
    }
}
