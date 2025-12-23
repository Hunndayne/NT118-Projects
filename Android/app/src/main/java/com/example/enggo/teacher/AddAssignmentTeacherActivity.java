package com.example.enggo.teacher;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAssignmentTeacherActivity extends BaseTeacherActivity {
    private EditText etTitle;
    private EditText etContent;
    private EditText etAttachLink;
    private EditText etStartTime;
    private EditText etDueTime;
    private Button btnAddAttachment;
    private Button btnCancel;
    private Button btnCreate;
    private Long courseId;
    private ActivityResultLauncher<String> filePickerLauncher;
    private String selectedFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_assignment_teacher);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        setupFilePicker();
        setupListeners();

        courseId = getIntent().getLongExtra(ManageAssignmentsTeacherActivity.EXTRA_COURSE_ID, -1);
    }

    private void initViews() {
        etTitle = findViewById(R.id.etAddAssignmentTitle_admin);
        etContent = findViewById(R.id.etAddAssignmentContent_admin);
        etAttachLink = findViewById(R.id.etAttachLinkContentAddAssignment_admin);
        etStartTime = findViewById(R.id.etStartTimeAddAssignment_admin);
        etDueTime = findViewById(R.id.etDueTimeAddAssignment_admin);
        btnAddAttachment = findViewById(R.id.buttonAddAttachment_Assignment_admin);
        btnCancel = findViewById(R.id.buttonCancelAddAssignment_admin);
        btnCreate = findViewById(R.id.buttonCreateAssignment_admin);
    }

    private void setupListeners() {
        if (btnAddAttachment != null) {
            btnAddAttachment.setOnClickListener(v -> filePickerLauncher.launch("*/*"));
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> {
                createAssignment();
            });
        }

        if (etStartTime != null) {
            etStartTime.setOnClickListener(v -> {
                showDateTimePicker(etStartTime);
            });
        }

        if (etDueTime != null) {
            etDueTime.setOnClickListener(v -> {
                showDateTimePicker(etDueTime);
            });
        }
    }

    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::handleFilePicked
        );
    }

    private void handleFilePicked(Uri uri) {
        if (uri == null) {
            return;
        }
        selectedFileUri = uri.toString();
        if (btnAddAttachment != null) {
            btnAddAttachment.setText(uri.getLastPathSegment());
        }
    }

    private void createAssignment() {
        if (courseId == null || courseId == -1) {
            Toast.makeText(this, "Missing course id", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTitle.getText().toString().trim();
        String description = etContent.getText().toString().trim();
        String attachLink = etAttachLink.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String dueTime = etDueTime.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Assignment title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String attachmentUrl = null;
        if (!attachLink.isEmpty()) {
            attachmentUrl = attachLink;
        } else if (selectedFileUri != null && !selectedFileUri.trim().isEmpty()) {
            attachmentUrl = selectedFileUri;
        }

        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        AssignmentCreateRequest request = new AssignmentCreateRequest(
                title,
                description.isEmpty() ? null : description,
                attachmentUrl,
                dueTime.isEmpty() ? null : dueTime,
                startTime.isEmpty() ? null : startTime
        );

        apiService.createAssignment(token, courseId, request)
                .enqueue(new Callback<AssignmentResponse>() {
                    @Override
                    public void onResponse(Call<AssignmentResponse> call, Response<AssignmentResponse> response) {
                        if (response.isSuccessful()) {
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(
                                    AddAssignmentTeacherActivity.this,
                                    "Create assignment failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AssignmentResponse> call, Throwable t) {
                        Toast.makeText(
                                AddAssignmentTeacherActivity.this,
                                "Cannot connect to server",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void showDateTimePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timePicker = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                String formatted = String.format(
                                        Locale.getDefault(),
                                        "%04d-%02d-%02d %02d:%02d",
                                        year,
                                        month + 1,
                                        dayOfMonth,
                                        hourOfDay,
                                        minute
                                );
                                target.setText(formatted);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );
                    timePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }
}
