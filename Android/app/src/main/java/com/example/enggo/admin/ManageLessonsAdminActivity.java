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

        // Tạo dữ liệu mới (sử dụng LessonItem)
        lessonItems = new ArrayList<>();
        lessonItems.add(new LessonItem("Basic Grammar", "20/05/2024", "Admin 1", "An introduction to the basic grammar rules."));
        lessonItems.add(new LessonItem("Advanced Grammar", "21/05/2024", "Admin 2", "A deeper dive into complex grammar topics."));
        lessonItems.add(new LessonItem("Vocabulary Lesson 1", "22/05/2024", "Admin 1", "Learn the first 50 essential vocabulary words."));

        // Khởi tạo Adapter với danh sách mới
        // LƯU Ý: Bạn cần cập nhật LessonAdminAdapter để chấp nhận List<LessonItem>
        lessonAdminAdapter = new LessonAdminAdapter(this, lessonItems);

        lessonsRecyclerView.setAdapter(lessonAdminAdapter);

        Button btnAddLesson = findViewById(R.id.btnAddLesson);
        btnAddLesson.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddNewLessonAdminActivity.class);
            startActivity(intent);
        });
    }
}