package com.example.enggo.admin;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageReportsAdminActivity extends BaseAdminActivity {

    private TextView tvTotalStudents;
    private TextView tvTotalCourses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_admin);

        setupAdminHeader();
        setupAdminFooter();

        tvTotalStudents = findViewById(R.id.tvTotalStudentsCount_admin);
        tvTotalCourses = findViewById(R.id.tvTotalCoursesCount_admin);

        LinearLayout totalCoursesCard = findViewById(R.id.totalCourses_admin);
        totalCoursesCard.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageCoursesAdminActivity.class));
        });

        LinearLayout totalStudentsCard = findViewById(R.id.totalStudents_admin);
        totalStudentsCard.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageAccountAdminActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTotalStudents();
        loadTotalCourses();
    }

    /* ================= TOTAL STUDENTS ================= */

    private void loadTotalStudents() {
        String token = getTokenFromDb();
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getAllStudents(token).enqueue(new Callback<List<UserAdmin>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<UserAdmin>> call,
                    @NonNull Response<List<UserAdmin>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    tvTotalStudents.setText(String.valueOf(response.body().size()));
                } else {
                    tvTotalStudents.setText("0");
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<UserAdmin>> call,
                    @NonNull Throwable t
            ) {
                tvTotalStudents.setText("0");
            }
        });
    }

    /* ================= TOTAL COURSES ================= */

    private void loadTotalCourses() {
        String token = getTokenFromDb();
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getAllCourses(token).enqueue(new Callback<List<CourseAdmin>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<CourseAdmin>> call,
                    @NonNull Response<List<CourseAdmin>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    tvTotalCourses.setText(String.valueOf(response.body().size()));
                } else {
                    tvTotalCourses.setText("0");
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<CourseAdmin>> call,
                    @NonNull Throwable t
            ) {
                tvTotalCourses.setText("0");
            }
        });
    }
}
