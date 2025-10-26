package com.example.enggo;

import android.content.Intent; // <-- Thêm vào
import android.os.Bundle;
import android.widget.Button; // <-- Thêm vào
import android.widget.TextView; // <-- Thêm vào

// Thêm 2 import này
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ManageLessonsAdminActivity extends BaseAdminActivity {

    // Khai báo RecyclerView và Adapter
    private RecyclerView lessonsRecyclerView;
    private LessonAdminAdapter lessonAdminAdapter;
    private List<String> lessonNames; // Danh sách dữ liệu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_lessons_admin);

        setupAdminHeader();
        setupAdminFooter();

        // Xử lý nút Back (như cọu hỏi)
        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());

        // 1. Ánh xạ RecyclerView
        lessonsRecyclerView = findViewById(R.id.lessonsRecyclerView);

        // 2. Tạo dữ liệu (Tạm thời, sau này cọu sẽ lấy từ database)
        lessonNames = new ArrayList<>();
        lessonNames.add("Basic Vietnamese Grammar");
        lessonNames.add("Advanced Grammar");
        lessonNames.add("Vocabulary Lesson 1");
        lessonNames.add("Listening Practice");

        // 3. Khởi tạo Adapter (đưa dữ liệu vào)
        lessonAdminAdapter = new LessonAdminAdapter(this, lessonNames);

        // 4. Gắn Adapter vào RecyclerView
        lessonsRecyclerView.setAdapter(lessonAdminAdapter);

    }
}