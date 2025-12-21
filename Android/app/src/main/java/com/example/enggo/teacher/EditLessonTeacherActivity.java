package com.example.enggo.teacher;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import com.example.enggo.R;

public class EditLessonTeacherActivity extends BaseTeacherActivity {
    private EditText etLessonName;
    private EditText etVideoLink;
    private EditText etPracticeLink;
    private Button btnCancel;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_lessons_teacher);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        loadLessonData();
        setupListeners();
    }

    private void initViews() {
        etLessonName = findViewById(R.id.etLessonTitle);
        etVideoLink = findViewById(R.id.etLessonContent);
        etPracticeLink = findViewById(R.id.etAttachLinkContent);
        btnCancel = findViewById(R.id.buttonCancelLesson);
        btnSave = findViewById(R.id.buttonSaveLesson);
    }

    private void loadLessonData() {
        // Load lesson data from intent
        if (getIntent() != null) {
            String lessonName = getIntent().getStringExtra("lesson_name");
            String videoLink = getIntent().getStringExtra("video_link");
            String practiceLink = getIntent().getStringExtra("practice_link");
            
            if (lessonName != null && etLessonName != null) {
                etLessonName.setText(lessonName);
            }
            if (videoLink != null && etVideoLink != null) {
                etVideoLink.setText(videoLink);
            }
            if (practiceLink != null && etPracticeLink != null) {
                etPracticeLink.setText(practiceLink);
            }
        }
    }

    private void setupListeners() {
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                // TODO: Validate and update lesson
                finish();
            });
        }
    }
}
