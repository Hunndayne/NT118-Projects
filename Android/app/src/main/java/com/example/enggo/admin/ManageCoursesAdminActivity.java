package com.example.enggo.admin;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;

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

public class ManageCoursesAdminActivity extends BaseAdminActivity {

    private static final int REQUEST_CREATE_COURSE = 1001;

    private RecyclerView coursesRecyclerView;
    private CourseAdminAdapter courseAdminAdapter;
    private List<CourseAdmin> courseItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_courses_admin);

        setupAdminHeader();
        setupAdminFooter();

        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());

        coursesRecyclerView = findViewById(R.id.coursesRecyclerView);

        // 🔥 INIT LIST + ADAPTER
        courseItems = new ArrayList<>();
        courseAdminAdapter = new CourseAdminAdapter(this, courseItems);

        // 🔥 BẮT BUỘC CÓ LayoutManager
        coursesRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );
        coursesRecyclerView.setAdapter(courseAdminAdapter);

        // 🔥 LOAD DATA LẦN ĐẦU
        loadCoursesFromApi();

        Button btnAddCourse = findViewById(R.id.btnAddCourse);
        btnAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ManageCoursesAdminActivity.this,
                    CreateCourseAdminActivity.class
            );
            // ❗ CHỈ DÙNG startActivityForResult
            startActivityForResult(intent, REQUEST_CREATE_COURSE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CREATE_COURSE && resultCode == RESULT_OK) {
            // 🔥 RELOAD SAU KHI TẠO COURSE
            loadCoursesFromApi();
        }
    }

    // 🔥 BONUS: reload khi quay lại màn hình
    @Override
    protected void onResume() {
        super.onResume();
        loadCoursesFromApi();
    }

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
                                    "Empty course list",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CourseAdmin>> call, Throwable t) {
                        Toast.makeText(
                                ManageCoursesAdminActivity.this,
                                "Load courses failed",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
