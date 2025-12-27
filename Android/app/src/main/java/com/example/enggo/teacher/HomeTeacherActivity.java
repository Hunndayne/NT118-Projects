package com.example.enggo.teacher;

import com.example.enggo.R;
import com.example.enggo.admin.CourseAdmin;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeTeacherActivity extends BaseTeacherActivity {

    private RecyclerView recyclerView;
    private CourseTeacherAdapter adapter;
    private List<CourseTeacher> courses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_teacher);

        setupTeacherHeader();
        setupTeacherFooter();

        // Setup onclick handlers
        setupScheduleItems();
        setupGradingCards();
        setupCourseList();
        loadTeacherCourses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTeacherCourses();
    }

    private void setupScheduleItems() {
        // Schedule items will be loaded from API in future implementation
        // For now, the static schedule card is hidden in the layout (android:visibility="gone")
    }

    private void setupGradingCards() {
        MaterialCardView cardGradingNew = findViewById(R.id.cardGradingNew_teacher);
        MaterialCardView cardGradingPending = findViewById(R.id.cardGradingPending_teacher);
        
        cardGradingNew.setOnClickListener(v -> {
            Intent intent = new Intent(this, SubmissionListActivity.class);
            startActivity(intent);
        });
        
        cardGradingPending.setOnClickListener(v -> {
            Intent intent = new Intent(this, SubmissionListActivity.class);
            startActivity(intent);
        });
    }

    private void setupCourseList() {
        recyclerView = findViewById(R.id.recyclerTeacherCourses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        courses = new ArrayList<>();
        adapter = new CourseTeacherAdapter(this, courses, course -> {
            Intent intent = new Intent(this, ClassCourseActivity.class);
            intent.putExtra("course_id", course.getId());
            intent.putExtra("course_name", course.getName());
            intent.putExtra("course_description", course.getClassCode());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadTeacherCourses() {
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAllCourses(token).enqueue(new Callback<List<CourseAdmin>>() {
            @Override
            public void onResponse(Call<List<CourseAdmin>> call, Response<List<CourseAdmin>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    courses.clear();
                    for (CourseAdmin courseAdmin : response.body()) {
                        CourseTeacher courseTeacher = new CourseTeacher(
                                courseAdmin.getId(),
                                courseAdmin.getName(),
                                courseAdmin.getClassCode(),
                                courseAdmin.getLessonCount()
                        );
                        courses.add(courseTeacher);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(
                            HomeTeacherActivity.this,
                            "Failed to load courses",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<List<CourseAdmin>> call, Throwable t) {
                Toast.makeText(
                        HomeTeacherActivity.this,
                        "Cannot connect to server",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
