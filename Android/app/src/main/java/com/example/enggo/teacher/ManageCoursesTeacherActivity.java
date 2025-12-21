package com.example.enggo.teacher;

import com.example.enggo.R;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ManageCoursesTeacherActivity extends BaseTeacherActivity {

    private RecyclerView coursesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_courses_teacher);

        setupTeacherHeader();
        setupTeacherFooter();

        // Setup back button
        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());

        // Setup RecyclerView
        coursesRecyclerView = findViewById(R.id.coursesRecyclerView);
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // TODO: Load courses from API and setup adapter
        loadCourses();
    }

    private void loadCourses() {
        // TODO: Implement API call to load teacher's courses
        // TODO: Create adapter: new CoursesAdapter(coursesList, this)
        // TODO: Set adapter: coursesRecyclerView.setAdapter(adapter)
    }
}
