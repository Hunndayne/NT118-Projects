package com.example.enggo.admin;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.admin.CreateCourseRequest;
import com.example.enggo.admin.CourseAdmin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateCourseAdminActivity extends BaseAdminActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_course_admin);

        setupAdminHeader();
        setupAdminFooter();

        EditText etName = findViewById(R.id.etCourseTitle_admin);
        EditText etCode = findViewById(R.id.etClassCode_admin);

        Button btnCancel = findViewById(R.id.buttonCancelCourseCreate_admin);
        Button btnCreate = findViewById(R.id.buttonCreateCourse_admin);

        btnCancel.setOnClickListener(v -> finish());

        btnCreate.setOnClickListener(v -> {

            String name = etName.getText().toString().trim();
            String code = etCode.getText().toString().trim();

            if (name.isEmpty() || code.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String token = getTokenFromDb();

            CreateCourseRequest request =
                    new CreateCourseRequest(name, code);

            ApiService apiService =
                    ApiClient.getClient().create(ApiService.class);

            apiService.createCourse(token, request)
                    .enqueue(new Callback<CourseAdmin>() {
                        @Override
                        public void onResponse(Call<CourseAdmin> call,
                                               Response<CourseAdmin> response) {

                            if (response.isSuccessful()) {
                                Toast.makeText(
                                        CreateCourseAdminActivity.this,
                                        "Course created successfully",
                                        Toast.LENGTH_SHORT
                                ).show();

                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(
                                        CreateCourseAdminActivity.this,
                                        "Create failed: " + response.code(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<CourseAdmin> call, Throwable t) {
                            Toast.makeText(
                                    CreateCourseAdminActivity.this,
                                    t.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });
    }
}
