package com.example.enggo.teacher;

import com.example.enggo.R;
import com.example.enggo.admin.CourseAdmin;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.common.CalendarSetup;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.view.CalendarView;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleTeacherActivity extends BaseTeacherActivity {
    private TextView tvBack;
    private CalendarSetup calendarSetup;
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
        CalendarView calendarView = findViewById(R.id.calendarView);
        TextView monthYearText = findViewById(R.id.monthYearText);
        ImageButton previousMonthButton = findViewById(R.id.previousMonthButton);
        ImageButton nextMonthButton = findViewById(R.id.nextMonthButton);

        // Setup the calendar with empty events (will be loaded from API)
        calendarSetup = new CalendarSetup(this, calendarView, monthYearText, previousMonthButton, nextMonthButton, events);
        calendarSetup.setup();
        
        // Load schedule events from API
        loadScheduleEvents();
        
        previousMonthButton.setOnClickListener(v -> {
            CalendarMonth currentMonth = calendarView.findFirstVisibleMonth();
            if (currentMonth != null) {
                calendarView.scrollToMonth(currentMonth.getYearMonth().minusMonths(1));
            }
        });

        nextMonthButton.setOnClickListener(v -> {
            CalendarMonth currentMonth = calendarView.findFirstVisibleMonth();
            if (currentMonth != null) {
                calendarView.scrollToMonth(currentMonth.getYearMonth().plusMonths(1));
            }
        });

    }

    private void initViews() {
        tvBack = findViewById(R.id.tvBack);
    }

    private void setupListeners() {
        if (tvBack != null) {
            tvBack.setOnClickListener(v -> finish());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadScheduleEvents();
    }

    private void loadScheduleEvents() {
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        int generation = ++loadGeneration;
        events.clear();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        final int[] pending = {2};
        loadAssignmentsEvents(apiService, token, generation, () -> finishPending(pending));
        loadCourseScheduleEvents(apiService, token, generation, () -> finishPending(pending));
    }

    private void finishPending(int[] pending) {
        pending[0] -= 1;
        if (pending[0] <= 0 && calendarSetup != null) {
            calendarSetup.updateEvents(events);
        }
    }

    private void loadAssignmentsEvents(ApiService apiService, String token, int generation, Runnable done) {
        apiService.getClasses(token).enqueue(new Callback<List<ClassResponse>>() {
            @Override
            public void onResponse(Call<List<ClassResponse>> call,
                                   Response<List<ClassResponse>> response) {
                if (generation != loadGeneration) {
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    done.run();
                    return;
                }
                List<ClassResponse> classes = response.body();
                if (classes.isEmpty()) {
                    done.run();
                    return;
                }
                final int[] pending = {classes.size()};
                for (ClassResponse clazz : classes) {
                    // Load assignments for each class
                    apiService.getAssignments(token, clazz.id).enqueue(new Callback<List<AssignmentResponse>>() {
                        @Override
                        public void onResponse(Call<List<AssignmentResponse>> call,
                                               Response<List<AssignmentResponse>> response) {
                            if (generation != loadGeneration) {
                                return;
                            }
                            if (response.isSuccessful() && response.body() != null) {
                                for (AssignmentResponse assignment : response.body()) {
                                    LocalDate date = parseDate(assignment.deadline);
                                    if (date != null) {
                                        addEvent(date, formatAssignmentLabel(assignment, clazz));
                                    }
                                }
                            }
                            finishPending();
                        }

                        @Override
                        public void onFailure(Call<List<AssignmentResponse>> call, Throwable t) {
                            if (generation != loadGeneration) {
                                return;
                            }
                            finishPending();
                        }

                        private void finishPending() {
                            pending[0] -= 1;
                            if (pending[0] <= 0) {
                                done.run();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<ClassResponse>> call, Throwable t) {
                if (generation != loadGeneration) {
                    return;
                }
                done.run();
            }
        });
    }

    private void loadCourseScheduleEvents(ApiService apiService, String token, int generation, Runnable done) {
        apiService.getAllCourses(token).enqueue(new Callback<List<CourseAdmin>>() {
            @Override
            public void onResponse(Call<List<CourseAdmin>> call, Response<List<CourseAdmin>> response) {
                if (generation != loadGeneration) {
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    done.run();
                    return;
                }
                YearMonth current = YearMonth.now();
                LocalDate rangeStart = current.minusMonths(10).atDay(1);
                LocalDate rangeEnd = current.plusMonths(10).atEndOfMonth();

                for (CourseAdmin course : response.body()) {
                    addCourseSchedule(course, rangeStart, rangeEnd);
                }
                done.run();
            }

            @Override
            public void onFailure(Call<List<CourseAdmin>> call, Throwable t) {
                if (generation != loadGeneration) {
                    return;
                }
                done.run();
            }
        });
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            // Try ISO format first (yyyy-MM-dd)
            if (dateStr.contains("T")) {
                dateStr = dateStr.split("T")[0];
            }
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            try {
                // Try dd/MM/yyyy format
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private DayOfWeek parseDayOfWeek(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return DayOfWeek.valueOf(value.trim().toUpperCase());
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

    private void addEvent(LocalDate date, String eventLabel) {
        if (!events.containsKey(date)) {
            events.put(date, new ArrayList<>());
        }
        events.get(date).add(eventLabel);
    }

    private String formatAssignmentLabel(AssignmentResponse assignment, ClassResponse clazz) {
        String className = clazz.name != null ? clazz.name : ("Class " + clazz.id);
        String title = assignment.title != null ? assignment.title : "Assignment";
        return "[" + className + "] " + title;
    }

    private void addCourseSchedule(CourseAdmin course, LocalDate rangeStart, LocalDate rangeEnd) {
        DayOfWeek dayOfWeek = parseDayOfWeek(course.getDayOfWeek());
        LocalDate startDate = parseDate(course.getStartDate());
        LocalDate endDate = parseDate(course.getEndDate());
        if (dayOfWeek == null || startDate == null || endDate == null) {
            return;
        }
        LocalDate effectiveStart = startDate.isAfter(rangeStart) ? startDate : rangeStart;
        LocalDate effectiveEnd = endDate.isBefore(rangeEnd) ? endDate : rangeEnd;
        if (effectiveEnd.isBefore(effectiveStart)) {
            return;
        }
        String label = formatCourseScheduleLabel(course);
        for (LocalDate date = effectiveStart; !date.isAfter(effectiveEnd); date = date.plusDays(1)) {
            if (date.getDayOfWeek() == dayOfWeek) {
                addEvent(date, label);
            }
        }
    }

    private String formatCourseScheduleLabel(CourseAdmin course) {
        String name = course.getName() != null ? course.getName() : "Course";
        LocalTime start = parseTime(course.getStartTime());
        LocalTime end = parseTime(course.getEndTime());
        if (start != null && end != null) {
            return "[" + name + "] Teaching " + start + "-" + end;
        }
        return "[" + name + "] Teaching";
    }
}
