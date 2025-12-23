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

public class EditAssignmentTeacherActivity extends BaseTeacherActivity {
    private EditText etTitle;
    private EditText etContent;
    private EditText etAttachLink;
    private EditText etStartTime;
    private EditText etDueTime;
    private Button btnCancel;
    private Button btnSave;
    private Button btnAttachment;
    private Long courseId;
    private Long assignmentId;
    private ActivityResultLauncher<String> filePickerLauncher;
    private String selectedFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_assignment_teacher);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        setupFilePicker();
        loadAssignmentData();
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etEditAssignmentTitle_admin);
        etContent = findViewById(R.id.etEditAssignmentContent_admin);
        etAttachLink = findViewById(R.id.etAttachLinkContentEditAssignment_admin);
        etStartTime = findViewById(R.id.etStartTimeEditAssignment_admin);
        etDueTime = findViewById(R.id.etDueTimeEditAssignment_admin);
        btnCancel = findViewById(R.id.buttonCancelEditAssignment_admin);
        btnSave = findViewById(R.id.buttonSaveEditAssignment_admin);
        btnAttachment = findViewById(R.id.buttonAttachment_EditAssignment_admin);
    }

    private void loadAssignmentData() {
        // Load assignment data from intent
        if (getIntent() != null) {
            String title = getIntent().getStringExtra("assignment_title");
            String content = getIntent().getStringExtra("assignment_content");
            String attachLink = getIntent().getStringExtra("attach_link");
            String startTime = getIntent().getStringExtra("start_time");
            String dueTime = getIntent().getStringExtra("due_time");
            courseId = getIntent().getLongExtra(ManageAssignmentsTeacherActivity.EXTRA_COURSE_ID, -1);
            assignmentId = getIntent().getLongExtra("assignment_id", -1);
            
            if (title != null && etTitle != null) {
                etTitle.setText(title);
            }
            if (content != null && etContent != null) {
                etContent.setText(content);
            }
            if (attachLink != null && etAttachLink != null) {
                etAttachLink.setText(attachLink);
            }
            if (startTime != null && etStartTime != null) {
                etStartTime.setText(startTime);
            }
            if (dueTime != null && etDueTime != null) {
                etDueTime.setText(dueTime);
            }
        }
    }

    private void setupListeners() {
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (btnAttachment != null) {
            btnAttachment.setOnClickListener(v -> filePickerLauncher.launch("*/*"));
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                updateAssignment();
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
        if (btnAttachment != null) {
            btnAttachment.setText(uri.getLastPathSegment());
        }
    }

    private void updateAssignment() {
        if (courseId == null || courseId == -1 || assignmentId == null || assignmentId == -1) {
            Toast.makeText(this, "Missing assignment info", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTitle.getText().toString().trim();
        String description = etContent.getText().toString().trim();
        String attachLink = etAttachLink.getText().toString().trim();
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
        AssignmentUpdateRequest request = new AssignmentUpdateRequest(
                title,
                description.isEmpty() ? null : description,
                attachmentUrl,
                dueTime.isEmpty() ? null : dueTime
        );

        apiService.updateAssignment(token, courseId, assignmentId, request)
                .enqueue(new Callback<AssignmentResponse>() {
                    @Override
                    public void onResponse(Call<AssignmentResponse> call, Response<AssignmentResponse> response) {
                        if (response.isSuccessful()) {
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(
                                    EditAssignmentTeacherActivity.this,
                                    "Update assignment failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AssignmentResponse> call, Throwable t) {
                        Toast.makeText(
                                EditAssignmentTeacherActivity.this,
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
