package com.example.enggo.admin;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.api.ReportOverviewResponse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageReportsAdminActivity extends BaseAdminActivity {

    // Thống kê tổng quan
    private TextView tvTotalStudents;
    private TextView tvTotalCourses;
    private TextView tvTotalClasses;
    private TextView tvTotalModules;

    // Thống kê nộp bài
    private TextView tvOnTime;
    private TextView tvLate;
    private TextView tvMissing;
    private TextView tvSubmissionRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_admin);

        setupAdminHeader();
        setupAdminFooter();

        // Ánh xạ View
        tvTotalStudents = findViewById(R.id.tvTotalStudentsCount_admin);
        tvTotalCourses = findViewById(R.id.tvTotalCoursesCount_admin);
        tvTotalClasses = findViewById(R.id.tvTotalClassesCount_admin);
        tvTotalModules = findViewById(R.id.tvTotalModulesCount_admin);

        tvOnTime = findViewById(R.id.tvOnTimeCount_admin);
        tvLate = findViewById(R.id.tvLateCount_admin);
        tvMissing = findViewById(R.id.tvMissingCount_admin);
        tvSubmissionRate = findViewById(R.id.tvSubmissionRate_admin);

        // Sự kiện click chuyển trang
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
        loadDashboardData();
    }

    private void loadDashboardData() {
        String token = getTokenFromDb();
        ApiService api = ApiClient.getClient().create(ApiService.class);

        // Gọi 1 API duy nhất lấy toàn bộ dữ liệu báo cáo
        api.getReportOverview(token).enqueue(new Callback<ReportOverviewResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<ReportOverviewResponse> call,
                    @NonNull Response<ReportOverviewResponse> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    ReportOverviewResponse data = response.body();
                    updateUI(data);
                } else {
                    setEmptyData();
                    Toast.makeText(ManageReportsAdminActivity.this, "Failed to load report data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<ReportOverviewResponse> call,
                    @NonNull Throwable t
            ) {
                setEmptyData();
                Toast.makeText(ManageReportsAdminActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(ReportOverviewResponse data) {
        // Cập nhật các thẻ tổng quan
        tvTotalStudents.setText(String.valueOf(data.getTotalStudents()));
        tvTotalCourses.setText(String.valueOf(data.getTotalCourses()));
        tvTotalClasses.setText(String.valueOf(data.getTotalClasses()));
        tvTotalModules.setText(String.valueOf(data.getTotalModules()));

        // Cập nhật thống kê nộp bài
        if (data.getSubmissionStatistics() != null) {
            ReportOverviewResponse.SubmissionStatistics stats = data.getSubmissionStatistics();
            tvOnTime.setText(String.valueOf(stats.getOnTime()));
            tvLate.setText(String.valueOf(stats.getLate()));
            tvMissing.setText(String.valueOf(stats.getMissing()));
            // Format tỉ lệ phần trăm, ví dụ: 85.5%
            tvSubmissionRate.setText(String.format("%.1f%%", stats.getSubmissionRate()));
        } else {
            // Trường hợp object stats null
            tvOnTime.setText("0");
            tvLate.setText("0");
            tvMissing.setText("0");
            tvSubmissionRate.setText("0%");
        }
    }

    private void setEmptyData() {
        tvTotalStudents.setText("0");
        tvTotalCourses.setText("0");
        tvTotalClasses.setText("0");
        tvTotalModules.setText("0");
        tvOnTime.setText("0");
        tvLate.setText("0");
        tvMissing.setText("0");
        tvSubmissionRate.setText("0%");
    }
}