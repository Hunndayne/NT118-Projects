package com.example.enggo.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.example.enggo.R;

public class CourseStudentsTeacherActivity extends BaseTeacherActivity {
    private TextView tvBack;
    private TextView tvCourseName;
    private TextView tvTotalStudents;
    private TextView tvActiveStudents;

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
        // Note: Student cards are read-only, no interaction needed
    }

    private void loadCourseData() {
        // Get course data from intent
        Intent intent = getIntent();
        if (intent != null) {
            String courseName = intent.getStringExtra("course_name");
            
            if (courseName != null && tvCourseName != null) {
                tvCourseName.setText(courseName + " - Students");
            }
        }
        
        // TODO: Load actual student data from API
        // For now, using placeholder data
    }

    private void setupListeners() {
        if (tvBack != null) {
            tvBack.setOnClickListener(v -> finish());
        }

        // Note: Teacher has read-only access to student list
        // No edit, delete, or ban permissions for students
    }
}
