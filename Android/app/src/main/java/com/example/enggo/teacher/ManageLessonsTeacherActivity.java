package com.example.enggo.teacher;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageLessonsTeacherActivity extends BaseTeacherActivity {

    public static final String EXTRA_COURSE_ID = "course_id";
    public static final String EXTRA_COURSE_NAME = "course_name";
    private static final int REQ_ADD_LESSON = 1001;
    private static final int REQ_EDIT_LESSON = 1002;

    private Long courseId;
    private List<LessonResponse> lessons;
    private LessonTeacherAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_lessons_teacher);

        setupTeacherHeader();
        setupTeacherFooter();

        courseId = getIntent().getLongExtra(EXTRA_COURSE_ID, -1);
        if (courseId == -1) {
            Toast.makeText(this, "Missing course id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvBack = findViewById(R.id.tvBack);
        TextView tvTitle = findViewById(R.id.tvTeacher_LessonsManager);
        if (tvTitle != null) {
            String courseName = getIntent().getStringExtra(EXTRA_COURSE_NAME);
            if (courseName != null && !courseName.trim().isEmpty()) {
                tvTitle.setText(courseName + " - Lessons");
            }
        }

        tvBack.setOnClickListener(v -> finish());

        Button btnAddLesson = findViewById(R.id.btnAddLesson);
        btnAddLesson.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddLessonTeacherActivity.class);
            intent.putExtra(EXTRA_COURSE_ID, courseId);
            startActivityForResult(intent, REQ_ADD_LESSON);
        });

        RecyclerView recyclerView = findViewById(R.id.lessonsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        lessons = new ArrayList<>();
        adapter = new LessonTeacherAdapter(lessons, new LessonTeacherAdapter.OnLessonActionListener() {
            @Override
            public void onEdit(LessonResponse lesson) {
                Intent intent = new Intent(ManageLessonsTeacherActivity.this, EditLessonTeacherActivity.class);
                intent.putExtra(EXTRA_COURSE_ID, courseId);
                intent.putExtra("lesson_id", lesson.id);
                intent.putExtra("lesson_name", lesson.title);
                intent.putExtra("lesson_description", lesson.description);
                intent.putExtra("lesson_order", lesson.orderIndex);
                startActivityForResult(intent, REQ_EDIT_LESSON);
            }

            @Override
            public void onDelete(LessonResponse lesson) {
                confirmDelete(lesson);
            }
        });
        recyclerView.setAdapter(adapter);

        loadLessons();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQ_ADD_LESSON || requestCode == REQ_EDIT_LESSON) && resultCode == RESULT_OK) {
            loadLessons();
        }
    }

    private void loadLessons() {
        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getLessons(token, courseId).enqueue(new Callback<List<LessonResponse>>() {
            @Override
            public void onResponse(Call<List<LessonResponse>> call, Response<List<LessonResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    lessons.clear();
                    lessons.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(
                            ManageLessonsTeacherActivity.this,
                            "Load lessons failed",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<List<LessonResponse>> call, Throwable t) {
                Toast.makeText(
                        ManageLessonsTeacherActivity.this,
                        "Cannot connect to server",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void confirmDelete(LessonResponse lesson) {
        new AlertDialog.Builder(this)
                .setTitle("Delete lesson")
                .setMessage("Are you sure you want to delete this lesson?")
                .setPositiveButton("Delete", (dialog, which) -> deleteLesson(lesson))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteLesson(LessonResponse lesson) {
        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.deleteLesson(token, courseId, lesson.id)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            lessons.remove(lesson);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(
                                    ManageLessonsTeacherActivity.this,
                                    "Delete lesson failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(
                                ManageLessonsTeacherActivity.this,
                                "Cannot connect to server",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
