package com.example.enggo.teacher;

import com.example.enggo.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

public class ClassCourseActivity extends BaseTeacherActivity {
    private TextView tvBack;
    private TextView tvCourseName;
    private TextView tvCourseDescription;
    private CardView cardLessons;
    private CardView cardAssignments;
    private CardView cardStudents;
    private CardView cardSubmissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_detail_teacher);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        loadCourseData();
        setupListeners();
    }

    private void initViews() {
        tvBack = findViewById(R.id.tvBack);
        tvCourseName = findViewById(R.id.tvCourseName);
        tvCourseDescription = findViewById(R.id.tvCourseDescription);
        cardLessons = findViewById(R.id.cardLessons);
        cardAssignments = findViewById(R.id.cardAssignments);
        cardStudents = findViewById(R.id.cardStudents);
        cardSubmissions = findViewById(R.id.cardSubmissions);
    }

    private void loadCourseData() {
        // Get course data from intent
        Intent intent = getIntent();
        if (intent != null) {
            String courseName = intent.getStringExtra("course_name");
            String courseDescription = intent.getStringExtra("course_description");
            
            if (courseName != null) {
                tvCourseName.setText(courseName);
            }
            if (courseDescription != null) {
                tvCourseDescription.setText(courseDescription);
            }
        }
    }

    private void setupListeners() {
        if (tvBack != null) {
            tvBack.setOnClickListener(v -> finish());
        }

        if (cardLessons != null) {
            cardLessons.setOnClickListener(v -> {
                Intent intent = new Intent(this, ManageLessonsTeacherActivity.class);
                intent.putExtra(ManageLessonsTeacherActivity.EXTRA_COURSE_ID,
                        getIntent().getLongExtra("course_id", -1));
                intent.putExtra(ManageLessonsTeacherActivity.EXTRA_COURSE_NAME,
                        tvCourseName != null ? tvCourseName.getText().toString() : null);
                startActivity(intent);
            });
        }

        if (cardAssignments != null) {
            cardAssignments.setOnClickListener(v -> {
                Intent intent = new Intent(this, ManageAssignmentsTeacherActivity.class);
                intent.putExtra(ManageAssignmentsTeacherActivity.EXTRA_COURSE_ID,
                        getIntent().getLongExtra("course_id", -1));
                intent.putExtra(ManageAssignmentsTeacherActivity.EXTRA_COURSE_NAME,
                        tvCourseName != null ? tvCourseName.getText().toString() : null);
                startActivity(intent);
            });
        }

        if (cardStudents != null) {
            cardStudents.setOnClickListener(v -> {
                Intent intent = new Intent(this, CourseStudentsTeacherActivity.class);
                // Pass course data
                intent.putExtra("course_name", tvCourseName != null ? tvCourseName.getText().toString() : "Course");
                intent.putExtra("course_id", getIntent().getLongExtra("course_id", -1));
                startActivity(intent);
            });
        }

        if (cardSubmissions != null) {
            cardSubmissions.setOnClickListener(v -> {
                Intent intent = new Intent(this, AssignmentsForSubmissionsTeacherActivity.class);
                intent.putExtra(AssignmentsForSubmissionsTeacherActivity.EXTRA_COURSE_ID,
                        getIntent().getLongExtra("course_id", -1));
                intent.putExtra(AssignmentsForSubmissionsTeacherActivity.EXTRA_COURSE_NAME,
                        tvCourseName != null ? tvCourseName.getText().toString() : null);
                startActivity(intent);
            });
        }
    }
}
