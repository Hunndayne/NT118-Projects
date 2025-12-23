package com.example.enggo.teacher;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddLessonTeacherActivity extends BaseTeacherActivity {
    private EditText etLessonName;
    private EditText etVideoLink;
    private EditText etPracticeLink;
    private Button btnCancel;
    private Button btnCreate;
    private Button btnUpload;
    private Long courseId;
    private ActivityResultLauncher<String> filePickerLauncher;
    private String selectedFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_lessons_teacher);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        setupFilePicker();
        setupListeners();

        courseId = getIntent().getLongExtra(ManageLessonsTeacherActivity.EXTRA_COURSE_ID, -1);
    }

    private void initViews() {
        etLessonName = findViewById(R.id.etLessonTitle_Admin);
        etVideoLink = findViewById(R.id.etAddLessonContent_Admin);
        etPracticeLink = findViewById(R.id.etUpAttachLinkContent_Admim);
        btnCancel = findViewById(R.id.buttonCancelUpLesson_Admin);
        btnCreate = findViewById(R.id.buttonUpLesson_Admin);
        btnUpload = findViewById(R.id.buttonUploadAttachmentAdmin);
    }

    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::handleFilePicked
        );
    }

    private void setupListeners() {
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (btnUpload != null) {
            btnUpload.setOnClickListener(v -> filePickerLauncher.launch("*/*"));
        }

        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> {
                createLesson();
            });
        }
    }

    private void handleFilePicked(Uri uri) {
        if (uri == null) {
            return;
        }
        selectedFileUri = uri.toString();
        if (btnUpload != null) {
            btnUpload.setText(uri.getLastPathSegment());
        }
    }

    private void createLesson() {
        if (courseId == null || courseId == -1) {
            Toast.makeText(this, "Missing course id", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etLessonName.getText().toString().trim();
        String content = etVideoLink.getText().toString().trim();
        String link = etPracticeLink.getText().toString().trim();
        String description = content;

        if (title.isEmpty()) {
            Toast.makeText(this, "Lesson title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        LessonCreateRequest request = new LessonCreateRequest(title, description, null);

        apiService.createLesson(token, courseId, request)
                .enqueue(new Callback<LessonResponse>() {
                    @Override
                    public void onResponse(Call<LessonResponse> call, Response<LessonResponse> response) {
                        if (response.isSuccessful()) {
                            LessonResponse lesson = response.body();
                            if (lesson != null) {
                                attachResources(token, lesson.id, link);
                            }
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(
                                    AddLessonTeacherActivity.this,
                                    "Create lesson failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LessonResponse> call, Throwable t) {
                        Toast.makeText(
                                AddLessonTeacherActivity.this,
                                "Cannot connect to server",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void attachResources(String token, Long lessonId, String link) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        if (selectedFileUri != null && !selectedFileUri.trim().isEmpty()) {
            LessonResourceRequest fileRequest = new LessonResourceRequest(
                    "FILE",
                    "Attachment",
                    null,
                    null,
                    selectedFileUri
            );
            apiService.addLessonResource(token, courseId, lessonId, fileRequest).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // no-op
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // no-op
                }
            });
        }

        if (link != null && !link.trim().isEmpty()) {
            LessonResourceRequest linkRequest = new LessonResourceRequest(
                    "LINK",
                    "Attachment Link",
                    null,
                    link,
                    null
            );
            apiService.addLessonResource(token, courseId, lessonId, linkRequest).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // no-op
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // no-op
                }
            });
        }
    }
}
