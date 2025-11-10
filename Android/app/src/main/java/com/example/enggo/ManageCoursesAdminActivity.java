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

    // Lớp nội bộ để giữ dữ liệu cho mỗi mục trong danh sách
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

    // Khai báo RecyclerView và Adapter
    private RecyclerView coursesRecyclerView;
    private CourseAdminAdapter courseAdminAdapter;
    // Thay đổi danh sách dữ liệu để sử dụng CourseItem
    private List<CourseItem> courseItems;

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

        // 4. Tạo dữ liệu mới (sử dụng CourseItem)
        courseItems = new ArrayList<>();
        courseItems.add(new CourseItem("Introduction of IELTS", "IE01", 2));
        courseItems.add(new CourseItem("Basic ENGLISH Grammar", "ENG01", 5));
        courseItems.add(new CourseItem("TOEIC READING LISTENING", "TOE03", 10));

        // 5. Khởi tạo Adapter MỚI với danh sách mới
        // LƯU Ý: Bạn cần cập nhật CourseAdminAdapter để chấp nhận List<CourseItem>
        courseAdminAdapter = new CourseAdminAdapter(this, courseItems);

        // 6. Gắn Adapter vào RecyclerView
        coursesRecyclerView.setAdapter(courseAdminAdapter);

        // 7. Gắn click cho nút Add Course (giữ nguyên)
        Button btnAddCourse = findViewById(R.id.btnAddCourse);
        btnAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateCourseAdminActivity.class);
            startActivity(intent);
        });
    }
}
