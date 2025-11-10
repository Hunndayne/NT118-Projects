package com.example.enggo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button; // <-- Thêm Button
import android.widget.TextView;

// Thêm 3 import này
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ManageCoursesAdminActivity extends BaseAdminActivity {


    public static class CourseItem {
        String name;
        String classCode;
        int lessonCount;

        public CourseItem(String name, String classCode, int lessonCount) {
            this.name = name;
            this.classCode = classCode;
            this.lessonCount = lessonCount;
        }
    }


    private RecyclerView coursesRecyclerView;
    private CourseAdminAdapter courseAdminAdapter;

    private List<CourseItem> courseItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.manage_courses_admin);

        setupAdminHeader();
        setupAdminFooter();

        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> {
            finish();
        });


        coursesRecyclerView = findViewById(R.id.coursesRecyclerView);


        courseItems = new ArrayList<>();
        courseItems.add(new CourseItem("Introduction of IELTS", "IE01", 2));
        courseItems.add(new CourseItem("Basic ENGLISH Grammar", "ENG01", 5));
        courseItems.add(new CourseItem("TOEIC READING LISTENING", "TOE03", 10));


        courseAdminAdapter = new CourseAdminAdapter(this, courseItems);

        coursesRecyclerView.setAdapter(courseAdminAdapter);

        Button btnAddCourse = findViewById(R.id.btnAddCourse);
        btnAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateCourseAdminActivity.class);
            startActivity(intent);
        });
    }
}
