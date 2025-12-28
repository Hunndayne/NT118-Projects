package com.example.enggo.teacher;

import com.example.enggo.R;
import com.example.enggo.admin.CourseAdmin;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.common.CalendarSetup;
import com.kizitonwose.calendar.view.CalendarView;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleTeacherActivity extends BaseTeacherActivity {

    // UI Components
    private TextView tvBack;
    private TextView tvTodayCount;

    // Calendar Components
    private CalendarView calendarView;
    private TextView monthYearText;
    private ImageButton previousMonthButton;
    private ImageButton nextMonthButton;

    // Logic
    private CalendarSetup calendarSetup;
    // Map này chứa dữ liệu để CalendarSetup hiển thị (Dấu chấm & Dialog Content)
    private final Map<LocalDate, List<String>> events = new HashMap<>();

    private int loadGeneration = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_teacher);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        setupListeners();

        // 1. Khởi tạo CalendarSetup
        // CalendarSetup sẽ tự động xử lý DayBinder và Click Event (hiện Dialog)
        calendarSetup = new CalendarSetup(this, calendarView, monthYearText, previousMonthButton, nextMonthButton, events);
        calendarSetup.setup();

        // 2. Load dữ liệu từ API
        loadScheduleEvents();
    }

    private void initViews() {
        tvBack = findViewById(R.id.tvBack);
        tvTodayCount = findViewById(R.id.tvTodayCount);

        calendarView = findViewById(R.id.calendarView);
        monthYearText = findViewById(R.id.monthYearText);
        previousMonthButton = findViewById(R.id.previousMonthButton);
        nextMonthButton = findViewById(R.id.nextMonthButton);
    }

    private void setupListeners() {
        if (tvBack != null) {
            tvBack.setOnClickListener(v -> finish());
        }

        // Các listener cho button previous/next đã được CalendarSetup xử lý,
        // nhưng nếu muốn gán lại thủ công cũng không sao, tuy nhiên CalendarSetup đã nhận các button này trong constructor rồi.
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload khi quay lại màn hình
        loadScheduleEvents();
    }

    private void loadScheduleEvents() {
        String token = getTokenFromDb();
        if (token == null) return;

        loadGeneration++;
        int generation = loadGeneration;

        // Clear dữ liệu cũ
        events.clear();
        // Reset text Today
        tvTodayCount.setText("Loading...");

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAllCourses(token).enqueue(new Callback<List<CourseAdmin>>() {
            @Override
            public void onResponse(Call<List<CourseAdmin>> call, Response<List<CourseAdmin>> response) {
                if (generation != loadGeneration) return;

                if (response.isSuccessful() && response.body() != null) {
                    YearMonth current = YearMonth.now();
                    // Load trong khoảng +- 6 tháng
                    LocalDate rangeStart = current.minusMonths(6).atDay(1);
                    LocalDate rangeEnd = current.plusMonths(6).atEndOfMonth();

                    for (CourseAdmin course : response.body()) {
                        processCourseToEvents(course, rangeStart, rangeEnd);
                    }

                    // 1. Cập nhật Calendar (để hiện dấu chấm và dữ liệu cho Dialog)
                    calendarSetup.updateEvents(events);

                    // 2. Cập nhật Card Today
                    updateTodayInfo();
                } else {
                    tvTodayCount.setText("0 classes scheduled");
                }
            }

            @Override
            public void onFailure(Call<List<CourseAdmin>> call, Throwable t) {
                if (generation != loadGeneration) return;
                tvTodayCount.setText("0 classes scheduled");
                Toast.makeText(ScheduleTeacherActivity.this, "Failed to load schedule", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processCourseToEvents(CourseAdmin course, LocalDate rangeStart, LocalDate rangeEnd) {
        DayOfWeek dayOfWeek = parseDayOfWeek(course.getDayOfWeek());
        LocalDate startDate = parseDate(course.getStartDate());
        LocalDate endDate = parseDate(course.getEndDate());
        LocalTime startTime = parseTime(course.getStartTime());
        LocalTime endTime = parseTime(course.getEndTime());

        if (dayOfWeek == null || startDate == null || endDate == null || startTime == null) {
            return;
        }

        LocalDate effectiveStart = startDate.isAfter(rangeStart) ? startDate : rangeStart;
        LocalDate effectiveEnd = endDate.isBefore(rangeEnd) ? endDate : rangeEnd;

        if (effectiveEnd.isBefore(effectiveStart)) return;

        String courseName = course.getName() != null ? course.getName() : "Course";

        // Format String hiển thị trong Dialog
        // Ví dụ: "09:30 - 11:00: [ENG101] Basic English"
        String timeStr = startTime.toString();
        if (endTime != null) {
            timeStr += " - " + endTime.toString();
        }
        String dialogContent = timeStr + ": " + courseName;

        for (LocalDate date = effectiveStart; !date.isAfter(effectiveEnd); date = date.plusDays(1)) {
            if (date.getDayOfWeek() == dayOfWeek) {
                if (!events.containsKey(date)) {
                    events.put(date, new ArrayList<>());
                }
                events.get(date).add(dialogContent);
            }
        }
    }

    private void updateTodayInfo() {
        LocalDate today = LocalDate.now();
        List<String> todayClasses = events.get(today);

        if (todayClasses == null || todayClasses.isEmpty()) {
            tvTodayCount.setText("No classes scheduled");
        } else {
            int count = todayClasses.size();
            String text = count + (count == 1 ? " class scheduled" : " classes scheduled");
            tvTodayCount.setText(text);
        }
    }

    /* ================= UTILS ================= */

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            if (dateStr.contains("T")) dateStr = dateStr.split("T")[0];
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    private DayOfWeek parseDayOfWeek(String value) {
        try { return DayOfWeek.valueOf(value.trim().toUpperCase()); }
        catch (Exception e) { return null; }
    }

    private LocalTime parseTime(String value) {
        if (value == null) return null;
        try {
            String clean = value.trim().length() > 5 ? value.trim().substring(0, 5) : value.trim();
            return LocalTime.parse(clean, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) { return null; }
    }
}