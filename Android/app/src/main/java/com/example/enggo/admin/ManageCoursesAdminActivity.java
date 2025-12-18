package com.example.enggo.admin;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import androidx.appcompat.app.AlertDialog;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageCoursesAdminActivity
        extends BaseAdminActivity
        implements CourseAdminAdapter.OnCourseActionListener {

    private static final int REQ_CREATE_COURSE = 1001;
    private static final int REQ_EDIT_COURSE = 1002;

    private RecyclerView coursesRecyclerView;
    private CourseAdminAdapter courseAdminAdapter;
    private List<CourseAdmin> courseItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_courses_admin);

        setupAdminHeader();
        setupAdminFooter();

        // Back
        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());

        // RecyclerView
        coursesRecyclerView = findViewById(R.id.coursesRecyclerView);
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        courseItems = new ArrayList<>();
        courseAdminAdapter =
                new CourseAdminAdapter(this, courseItems, this);

        coursesRecyclerView.setAdapter(courseAdminAdapter);

        // Add course
        Button btnAddCourse = findViewById(R.id.btnAddCourse);
        btnAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ManageCoursesAdminActivity.this,
                    CreateCourseAdminActivity.class
            );
            startActivityForResult(intent, REQ_CREATE_COURSE);
        });

        // Load data lần đầu
        loadCoursesFromApi();
    }

    // ===============================
    // LOAD COURSES
    // ===============================
    private void loadCoursesFromApi() {
        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getAllCourses(token)
                .enqueue(new Callback<List<CourseAdmin>>() {
                    @Override
                    public void onResponse(Call<List<CourseAdmin>> call,
                                           Response<List<CourseAdmin>> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            courseItems.clear();
                            courseItems.addAll(response.body());
                            courseAdminAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(
                                    ManageCoursesAdminActivity.this,
                                    "Load courses failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CourseAdmin>> call, Throwable t) {
                        Toast.makeText(
                                ManageCoursesAdminActivity.this,
                                "Cannot connect to server",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // ===============================
    // CALLBACK FROM ADAPTER
    // ===============================
    @Override
    public void onEditClick(CourseAdmin course) {
        Intent intent = new Intent(
                this,
                EditCourseAdminActivity.class
        );
        intent.putExtra("COURSE_ID", course.getId());
        startActivityForResult(intent, REQ_EDIT_COURSE);
    }

    @Override
    public void onDeleteClick(CourseAdmin course) {

        new AlertDialog.Builder(this)
                .setTitle("Delete course")
                .setMessage("Are you sure you want to delete this course?")
                .setPositiveButton("Delete", (dialog, which) -> {

                    String token = getTokenFromDb();
                    ApiService apiService =
                            ApiClient.getClient().create(ApiService.class);

                    apiService.deleteCourse(token, course.getId())
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(
                                        Call<Void> call,
                                        Response<Void> response) {

                                    if (response.isSuccessful()) {
                                        Toast.makeText(
                                                ManageCoursesAdminActivity.this,
                                                "Course deleted",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                        loadCoursesFromApi(); // reload list
                                    } else {
                                        Toast.makeText(
                                                ManageCoursesAdminActivity.this,
                                                "Delete failed",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }

                                @Override
                                public void onFailure(
                                        Call<Void> call,
                                        Throwable t) {

                                    Toast.makeText(
                                            ManageCoursesAdminActivity.this,
                                            "Server error",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    // ===============================
    // RECEIVE RESULT FROM CREATE / EDIT
    // ===============================
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQ_CREATE_COURSE
                || requestCode == REQ_EDIT_COURSE)
                && resultCode == RESULT_OK) {

            loadCoursesFromApi();
        }
    }
}
