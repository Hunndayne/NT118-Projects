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

public class EditLessonTeacherActivity extends BaseTeacherActivity {
    private EditText etLessonName;
    private EditText etVideoLink;
    private EditText etPracticeLink;
    private Button btnCancel;
    private Button btnSave;
    private Button btnUpload;
    private Long courseId;
    private Long lessonId;
    private ActivityResultLauncher<String> filePickerLauncher;
    private String selectedFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_lessons_teacher);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        setupFilePicker();
        loadLessonData();
        setupListeners();
    }

    private void initViews() {
        etLessonName = findViewById(R.id.etLessonTitle);
        etVideoLink = findViewById(R.id.etLessonContent);
        etPracticeLink = findViewById(R.id.etAttachLinkContent);
        btnCancel = findViewById(R.id.buttonCancelLesson);
        btnSave = findViewById(R.id.buttonSaveLesson);
        btnUpload = findViewById(R.id.buttonAttachment);
    }

    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::handleFilePicked
        );
    }

    private void loadLessonData() {
        // Load lesson data from intent
        if (getIntent() != null) {
            String lessonName = getIntent().getStringExtra("lesson_name");
            String description = getIntent().getStringExtra("lesson_description");
            courseId = getIntent().getLongExtra(ManageLessonsTeacherActivity.EXTRA_COURSE_ID, -1);
            lessonId = getIntent().getLongExtra("lesson_id", -1);
            
            if (lessonName != null && etLessonName != null) {
                etLessonName.setText(lessonName);
            }
            if (description != null && etVideoLink != null) {
                etVideoLink.setText(description);
            }
        }
    }

    private void setupListeners() {
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (btnUpload != null) {
            btnUpload.setOnClickListener(v -> filePickerLauncher.launch("*/*"));
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                updateLesson();
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

    private void updateLesson() {
        if (courseId == null || courseId == -1 || lessonId == null || lessonId == -1) {
            Toast.makeText(this, "Missing lesson info", Toast.LENGTH_SHORT).show();
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
        LessonUpdateRequest request = new LessonUpdateRequest(title, description, null);

        apiService.updateLesson(token, courseId, lessonId, request)
                .enqueue(new Callback<LessonResponse>() {
                    @Override
                    public void onResponse(Call<LessonResponse> call, Response<LessonResponse> response) {
                        if (response.isSuccessful()) {
                            attachResources(token, link);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(
                                    EditLessonTeacherActivity.this,
                                    "Update lesson failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LessonResponse> call, Throwable t) {
                        Toast.makeText(
                                EditLessonTeacherActivity.this,
                                "Cannot connect to server",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void attachResources(String token, String link) {
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
