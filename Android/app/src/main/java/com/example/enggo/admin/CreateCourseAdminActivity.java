package com.example.enggo.admin;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.admin.CreateCourseRequest;
import com.example.enggo.admin.CourseAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

public class CreateCourseAdminActivity extends BaseAdminActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_course_admin);

        setupAdminHeader();
        setupAdminFooter();

        EditText etName = findViewById(R.id.etCourseTitle_admin);
        EditText etCode = findViewById(R.id.etClassCode_admin);
        Spinner spDayOfWeek = findViewById(R.id.spDayOfWeek);
        EditText etStartTime = findViewById(R.id.etStartTime_admin);
        EditText etEndTime = findViewById(R.id.etEndTime_admin);
        EditText etStartDate = findViewById(R.id.etStartDate_admin);
        EditText etEndDate = findViewById(R.id.etEndDate_admin);

        Button btnCancel = findViewById(R.id.buttonCancelCourseCreate_admin);

        Button btnParticipants = findViewById(R.id.buttonParticipantsList_admin);
        btnParticipants.setOnClickListener(v -> {
            Toast.makeText(this, "Create the course first, then add participants", Toast.LENGTH_SHORT).show();
        });

        Button btnCreate = findViewById(R.id.buttonCreateCourse_admin);

        btnCancel.setOnClickListener(v -> finish());

        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        btnCreate.setOnClickListener(v -> {

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
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!validateSchedule(startDate, endDate, startTime, endTime)) {
                return;
            }

            String token = getTokenFromDb();

            CreateCourseRequest request =
                    new CreateCourseRequest(name, code, dayOfWeek, startTime, endTime, startDate, endDate);

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
        try {
            return LocalTime.parse(value.trim(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return null;
        }
    }
}
