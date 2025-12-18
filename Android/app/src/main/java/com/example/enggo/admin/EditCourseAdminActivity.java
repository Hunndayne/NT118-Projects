package com.example.enggo.admin;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.admin.CourseAdmin;
import com.example.enggo.admin.UpdateCourseRequest;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditCourseAdminActivity extends BaseAdminActivity {

    private EditText etName, etCode;
    private Long courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_course_admin);

        setupAdminHeader();
        setupAdminFooter();

        etName = findViewById(R.id.etCourseTitle);
        etCode = findViewById(R.id.etClassCode);
        Button btnSave = findViewById(R.id.buttonSaveCourse);
        Button btnCancel = findViewById(R.id.buttonCancelCourse);

        courseId = getIntent().getLongExtra("COURSE_ID", -1);

        if (courseId == -1) {
            Toast.makeText(this, "Invalid course", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadCourseDetail();

        btnSave.setOnClickListener(v -> updateCourse());

        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadCourseDetail() {
        String token = getTokenFromDb();
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getCourseById(token, courseId)
                .enqueue(new Callback<CourseAdmin>() {
                    @Override
                    public void onResponse(Call<CourseAdmin> call,
                                           Response<CourseAdmin> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            etName.setText(response.body().getName());
                            etCode.setText(response.body().getClassCode());
                        }
                    }

                    @Override
                    public void onFailure(Call<CourseAdmin> call, Throwable t) {
                        Toast.makeText(
                                EditCourseAdminActivity.this,
                                "Load course failed",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void updateCourse() {
        String name = etName.getText().toString().trim();
        String code = etCode.getText().toString().trim();

        if (name.isEmpty() || code.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateCourseRequest request =
                new UpdateCourseRequest(name, code);

        String token = getTokenFromDb();
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api .updateCourse(token, courseId, request)
                .enqueue(new Callback<CourseAdmin>() {
                    @Override
                    public void onResponse(Call<CourseAdmin> call,
                                           Response<CourseAdmin> response) {
                        if (response.isSuccessful()) {
                            setResult(RESULT_OK);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<CourseAdmin> call, Throwable t) {
                        Toast.makeText(
                                EditCourseAdminActivity.this,
                                "Update failed",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
