package com.example.enggo.admin;
import com.example.enggo.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ManageLessonsAdminActivity extends BaseAdminActivity {

    // Lớp nội bộ để giữ dữ liệu cho mỗi bài học
    public static class LessonItem {
        String name;
        String date;
        String poster;
        String description;

        public LessonItem(String name, String date, String poster, String description) {
            this.name = name;
            this.date = date;
            this.poster = poster;
            this.description = description;
        }
    }

    private RecyclerView lessonsRecyclerView;
    private LessonAdminAdapter lessonAdminAdapter;
    // Thay đổi danh sách dữ liệu để sử dụng LessonItem
    private List<LessonItem> lessonItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_lessons_admin);

        setupAdminHeader();
        setupAdminFooter();

        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());

        lessonsRecyclerView = findViewById(R.id.lessonsRecyclerView);

        // Initialize empty list - data will be loaded from API
        lessonItems = new ArrayList<>();
        
        // Initialize Adapter with empty list
        lessonAdminAdapter = new LessonAdminAdapter(this, lessonItems);
        lessonsRecyclerView.setAdapter(lessonAdminAdapter);
        
        // TODO: Load lessons from API
        // loadLessons();

        Button btnAddLesson = findViewById(R.id.btnAddLesson);
        btnAddLesson.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddNewLessonAdminActivity.class);
            startActivity(intent);
        });
    }
}