package com.example.enggo.teacher;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import com.example.enggo.R;

public class AddAssignmentTeacherActivity extends BaseTeacherActivity {
    private EditText etTitle;
    private EditText etContent;
    private EditText etAttachLink;
    private EditText etStartTime;
    private EditText etDueTime;
    private Button btnAddAttachment;
    private Button btnCancel;
    private Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_assignment_teacher);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        setupListeners();
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
            btnAddAttachment.setOnClickListener(v -> {
                // TODO: Implement file attachment
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> {
                // TODO: Validate and submit assignment
                finish();
            });
        }

        if (etStartTime != null) {
            etStartTime.setOnClickListener(v -> {
                // TODO: Show date/time picker
            });
        }

        if (etDueTime != null) {
            etDueTime.setOnClickListener(v -> {
                // TODO: Show date/time picker
            });
        }
    }
}
