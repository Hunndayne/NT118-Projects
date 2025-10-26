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

    // Khai báo RecyclerView và Adapter
    private RecyclerView coursesRecyclerView;
    private CourseAdminAdapter courseAdminAdapter;
    private List<String> courseNames; // Danh sách dữ liệu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Dùng layout MỚI cọu vừa sửa ở Bước 1
        setContentView(R.layout.manage_courses_admin);

        setupAdminHeader();
        setupAdminFooter();

        // 2. Xử lý nút Back (giữ nguyên)
        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> {
            finish();
        });

        // 3. Ánh xạ RecyclerView MỚI
        coursesRecyclerView = findViewById(R.id.coursesRecyclerView);

        // 4. Tạo dữ liệu (Tạm thời)
        courseNames = new ArrayList<>();
        courseNames.add("Introduction of Vietnamese");
        courseNames.add("Basic Vietnamese Grammar");
        courseNames.add("TOEIC READING LISTENING");

        // 5. Khởi tạo Adapter MỚI
        courseAdminAdapter = new CourseAdminAdapter(this, courseNames);

        // 6. Gắn Adapter vào RecyclerView
        coursesRecyclerView.setAdapter(courseAdminAdapter);

        // 7. Gắn click cho nút Add Course (giữ nguyên)
        Button btnAddCourse = findViewById(R.id.btnAddCourse);
        btnAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditCourseAdminActivity.class);
            startActivity(intent);
        });

        Button btnAddCourses = findViewById(R.id.btnAddCourse);
        btnAddCourses.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateCourseAdminActivity.class);
            startActivity(intent);
        });
    }
}