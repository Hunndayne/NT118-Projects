package com.example.enggo.admin;
import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class ManageReportsAdminActivity extends BaseAdminActivity {
    private TextView tvTotalStudents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_admin);
        setupAdminHeader();
        setupAdminFooter();

        tvTotalStudents = findViewById(R.id.tvTotalStudentsCount_admin);
        loadTotalStudents();

        LinearLayout tvTotalCourses = findViewById(R.id.totalCourses_admin);
        tvTotalCourses.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageCoursesAdminActivity.class);
            startActivity(intent);
        });

        LinearLayout tvStudent = findViewById(R.id.totalStudents_admin);
        tvStudent.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageAccountAdminActivity.class);
            startActivity(intent);
        });
    }

    private void loadTotalStudents() {

        String token = getTokenFromDb(); // token admin từ SQLite

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getAllStudents(token).enqueue(new retrofit2.Callback<List<UserAdmin>>() {
            @Override
            public void onResponse(Call<List<UserAdmin>> call,
                                   Response<List<UserAdmin>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    int totalStudents = response.body().size();
                    tvTotalStudents.setText(String.valueOf(totalStudents));
                } else {
                    tvTotalStudents.setText("0");
                }
            }

            @Override
            public void onFailure(Call<List<UserAdmin>> call, Throwable t) {
                tvTotalStudents.setText("0");
            }
        });
    }

}