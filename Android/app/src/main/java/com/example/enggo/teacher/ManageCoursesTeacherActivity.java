package com.example.enggo.teacher;

import com.example.enggo.R;
import com.example.enggo.admin.CourseAdmin;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageCoursesTeacherActivity extends BaseTeacherActivity 
        implements CourseTeacherAdapter.OnCourseClickListener {

    private RecyclerView coursesRecyclerView;
    private CourseTeacherAdapter adapter;
    private List<CourseTeacher> courseItems;

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
        
        courseItems = new ArrayList<>();
        adapter = new CourseTeacherAdapter(this, courseItems, this);
        coursesRecyclerView.setAdapter(adapter);

        // Load courses
        loadCourses();
    }

    private void loadCourses() {
        // TODO: Implement API call to load teacher's courses
        // For now, using mock data
        // courseItems.clear();
        
        // // Mock data - replace with actual API call
        // courseItems.add(new CourseTeacher(1, "TOEIC Reading & Listening", "TA153GD", 12));
        // courseItems.add(new CourseTeacher(2, "TOEIC Speaking & Writing", "TB115", 8));
        // courseItems.add(new CourseTeacher(3, "IELTS Basic Course 2024", "IELTS01", 15));
        // courseItems.add(new CourseTeacher(4, "Business English", "BIZ101", 10));
        
        // adapter.notifyDataSetChanged();
        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        
        apiService.getAllCourses(token)
                .enqueue(new Callback<List<CourseAdmin>>() {
                    @Override
                    public void onResponse(Call<List<CourseAdmin>> call, Response<List<CourseAdmin>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            courseItems.clear();
                            // Convert CourseAdmin to CourseTeacher
                            for (CourseAdmin courseAdmin : response.body()) {
                                CourseTeacher courseTeacher = new CourseTeacher(
                                        courseAdmin.getId(),
                                        courseAdmin.getName(),
                                        courseAdmin.getClassCode(),
                                        courseAdmin.getLessonCount()
                                );
                                courseItems.add(courseTeacher);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(
                                    ManageCoursesTeacherActivity.this,
                                    "Failed to load courses",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CourseAdmin>> call, Throwable t) {
                        Toast.makeText(
                                ManageCoursesTeacherActivity.this,
                                "Cannot connect to server: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    @Override
    public void onCourseClick(CourseTeacher course) {
        // Navigate to ClassCourseActivity (course management page)
        Intent intent = new Intent(this, ClassCourseActivity.class);
        intent.putExtra("course_id", course.getId());
        intent.putExtra("course_name", course.getName());
        intent.putExtra("course_code", course.getClassCode());
        startActivity(intent);
    }
}
