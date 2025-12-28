package com.example.enggo.admin;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.admin.CourseAdmin;
import com.example.enggo.admin.UpdateCourseRequest;

import android.content.Intent;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditCourseAdminActivity extends BaseAdminActivity {

    private EditText etName, etCode;
    private Spinner spDayOfWeek;
    private EditText etStartTime;
    private EditText etEndTime;
    private EditText etStartDate;
    private EditText etEndDate;
    private Long courseId;
    private String originalCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_course_admin);

        setupAdminHeader();
        setupAdminFooter();

        etName = findViewById(R.id.etCourseTitle);
        etCode = findViewById(R.id.etClassCode);
        spDayOfWeek = findViewById(R.id.spDayOfWeek);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        Button btnSave = findViewById(R.id.buttonSaveCourse);
        Button btnCancel = findViewById(R.id.buttonCancelCourse);
        Button btnParticipants = findViewById(R.id.buttonParticipantsList);

        courseId = getIntent().getLongExtra("COURSE_ID", -1);

        if (courseId == -1) {
            Toast.makeText(this, "Invalid course", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        loadCourseDetail();

        btnSave.setOnClickListener(v -> updateCourse());

        btnCancel.setOnClickListener(v -> finish());
        btnParticipants.setOnClickListener(v -> {
            Intent intent = new Intent(EditCourseAdminActivity.this, CoursesParticipantAdmin.class);
            intent.putExtra(CoursesParticipantAdmin.EXTRA_COURSE_ID, courseId);
            startActivity(intent);
        });
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
                            CourseAdmin course = response.body();
                            etName.setText(course.getName());
                            etCode.setText(course.getClassCode());
                            originalCode = course.getClassCode();
                            setDayOfWeekSelection(course.getDayOfWeek());
                            setTimeText(etStartTime, course.getStartTime());
                            setTimeText(etEndTime, course.getEndTime());
                            setText(etStartDate, course.getStartDate());
                            setText(etEndDate, course.getEndDate());
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
        String dayOfWeek = spDayOfWeek.getSelectedItem() != null
                ? spDayOfWeek.getSelectedItem().toString().trim()
                : "";
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();

        if (name.isEmpty() || code.isEmpty()
                || dayOfWeek.isEmpty()
                || startTime.isEmpty()
                || endTime.isEmpty()
                || startDate.isEmpty()
                || endDate.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateSchedule(startDate, endDate, startTime, endTime)) {
            return;
        }

        UpdateCourseRequest request = new UpdateCourseRequest();
        request.name = name;
        if (originalCode == null || !code.equalsIgnoreCase(originalCode)) {
            request.code = code;
        }
        request.dayOfWeek = dayOfWeek;
        request.startTime = startTime;
        request.endTime = endTime;
        request.startDate = startDate;
        request.endDate = endDate;

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

    private void showDatePicker(EditText target) {
        LocalDate current = parseDate(target.getText().toString());
        LocalDate base = current != null ? current : LocalDate.now();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String value = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    target.setText(value);
                },
                base.getYear(),
                base.getMonthValue() - 1,
                base.getDayOfMonth()
        );
        dialog.show();
    }

    private void showTimePicker(EditText target) {
        LocalTime current = parseTime(target.getText().toString());
        LocalTime base = current != null ? current : LocalTime.of(8, 0);
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String value = String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
                    target.setText(value);
                },
                base.getHour(),
                base.getMinute(),
                true
        );
        dialog.show();
    }

    private void setDayOfWeekSelection(String dayOfWeek) {
        if (spDayOfWeek == null || dayOfWeek == null) {
            return;
        }
        String[] days = getResources().getStringArray(R.array.week_days);
        for (int i = 0; i < days.length; i++) {
            if (dayOfWeek.equalsIgnoreCase(days[i])) {
                spDayOfWeek.setSelection(i);
                return;
            }
        }
    }

    private void setTimeText(EditText target, String timeValue) {
        if (target == null || timeValue == null) {
            return;
        }
        String normalized = timeValue.trim();
        if (normalized.length() > 5) {
            normalized = normalized.substring(0, 5);
        }
        target.setText(normalized);
    }

    private void setText(EditText target, String value) {
        if (target == null) {
            return;
        }
        target.setText(value == null ? "" : value);
    }

    private boolean validateSchedule(String startDate, String endDate, String startTime, String endTime) {
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);
        LocalTime startT = parseTime(startTime);
        LocalTime endT = parseTime(endTime);
        if (start == null || end == null || startT == null || endT == null) {
            Toast.makeText(this, "Invalid date/time format", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (end.isBefore(start)) {
            Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!endT.isAfter(startT)) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > 5) {
            normalized = normalized.substring(0, 5);
        }
        try {
            return LocalTime.parse(normalized, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return null;
        }
    }
}
