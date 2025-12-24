package com.example.enggo.user;
import com.example.enggo.R;
import com.example.enggo.admin.CourseAdmin;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.common.CalendarSetup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.view.CalendarView;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseUserActivity extends BaseUserActivity {
    private RecyclerView recyclerAllCourses;
    private RecyclerView recyclerRecentCourses;
    private CourseStudentAdapter allAdapter;
    private CourseStudentAdapter recentAdapter;
    private final List<CourseAdmin> courses = new ArrayList<>();
    private final List<CourseAdmin> allCourses = new ArrayList<>();
    private final List<CourseAdmin> recentCourses = new ArrayList<>();
    private TextView searchInput;
    private CalendarView calendarView;
    private TextView monthYearText;
    private ImageButton previousMonthButton;
    private ImageButton nextMonthButton;
    private final Map<LocalDate, List<String>> deadlineEvents = new HashMap<>();
    private CalendarSetup calendarSetup;
    private int deadlineLoadGeneration = 0;
    private static final String PREFS_RECENT = "recent_courses";
    private static final String KEY_RECENT_IDS = "recent_course_ids";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_student);
        setupHeader();
        setupFooter();
        calendarView = findViewById(R.id.calendarView);
        monthYearText = findViewById(R.id.monthYearText);
        previousMonthButton = findViewById(R.id.previousMonthButton);
        nextMonthButton = findViewById(R.id.nextMonthButton);

        calendarSetup = new CalendarSetup(this, calendarView, monthYearText, previousMonthButton, nextMonthButton, deadlineEvents);
        calendarSetup.setup();
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

        recyclerAllCourses = findViewById(R.id.recyclerAllCourses);
        recyclerAllCourses.setLayoutManager(new LinearLayoutManager(this));
        allAdapter = new CourseStudentAdapter(courses, this::openCourse);
        recyclerAllCourses.setAdapter(allAdapter);

        recyclerRecentCourses = findViewById(R.id.recyclerRecentCourses);
        recyclerRecentCourses.setLayoutManager(new LinearLayoutManager(this));
        recentAdapter = new CourseStudentAdapter(recentCourses, this::openCourse);
        recyclerRecentCourses.setAdapter(recentAdapter);

        searchInput = findViewById(R.id.edtSearch);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyCourseFilter(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });

        loadCourses();
        loadDeadlineEvents();

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCourses();
        loadDeadlineEvents();
    }

    private void loadCourses() {
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAllCourses(token).enqueue(new Callback<List<CourseAdmin>>() {
            @Override
            public void onResponse(Call<List<CourseAdmin>> call, Response<List<CourseAdmin>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allCourses.clear();
                    allCourses.addAll(response.body());
                    applyCourseFilter(searchInput != null ? searchInput.getText().toString() : "");
                    rebuildRecentCourses();
                } else {
                    Toast.makeText(
                            CourseUserActivity.this,
                            "Load courses failed",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<List<CourseAdmin>> call, Throwable t) {
                Toast.makeText(
                        CourseUserActivity.this,
                        "Cannot connect to server",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void applyCourseFilter(String query) {
        String needle = query == null ? "" : query.trim().toLowerCase();
        courses.clear();
        if (needle.isEmpty()) {
            courses.addAll(allCourses);
        } else {
            for (CourseAdmin course : allCourses) {
                String name = course.getName() == null ? "" : course.getName().toLowerCase();
                String code = course.getClassCode() == null ? "" : course.getClassCode().toLowerCase();
                if (name.contains(needle) || code.contains(needle)) {
                    courses.add(course);
                }
            }
        }
        allAdapter.notifyDataSetChanged();
    }

    private void openCourse(CourseAdmin course) {
        if (course == null) {
            return;
        }
        updateRecentCourses(course.getId());
        Intent intent = new Intent(CourseUserActivity.this, ClassUserActivity.class);
        intent.putExtra("course_id", course.getId());
        intent.putExtra("course_name", course.getName());
        intent.putExtra("course_code", course.getClassCode());
        startActivity(intent);
    }

    private void rebuildRecentCourses() {
        List<Long> recentIds = getRecentCourseIds();
        Map<Long, CourseAdmin> lookup = new HashMap<>();
        for (CourseAdmin course : allCourses) {
            lookup.put(course.getId(), course);
        }
        recentCourses.clear();
        for (Long id : recentIds) {
            CourseAdmin course = lookup.get(id);
            if (course != null) {
                recentCourses.add(course);
            }
        }
        recentAdapter.notifyDataSetChanged();
    }

    private void updateRecentCourses(long courseId) {
        List<Long> recentIds = getRecentCourseIds();
        recentIds.remove(courseId);
        recentIds.add(0, courseId);
        if (recentIds.size() > 10) {
            recentIds = recentIds.subList(0, 10);
        }
        saveRecentCourseIds(recentIds);
        rebuildRecentCourses();
    }

    private List<Long> getRecentCourseIds() {
        SharedPreferences prefs = getSharedPreferences(PREFS_RECENT, MODE_PRIVATE);
        String raw = prefs.getString(KEY_RECENT_IDS, "");
        List<Long> ids = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return ids;
        }
        String[] parts = raw.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                ids.add(Long.parseLong(trimmed));
            } catch (NumberFormatException ignored) {
                // no-op
            }
        }
        return ids;
    }

    private void saveRecentCourseIds(List<Long> ids) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(ids.get(i));
        }
        SharedPreferences prefs = getSharedPreferences(PREFS_RECENT, MODE_PRIVATE);
        prefs.edit().putString(KEY_RECENT_IDS, builder.toString()).apply();
    }

    private void loadDeadlineEvents() {
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        int generation = ++deadlineLoadGeneration;
        deadlineEvents.clear();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getClasses(token).enqueue(new Callback<List<com.example.enggo.teacher.ClassResponse>>() {
            @Override
            public void onResponse(Call<List<com.example.enggo.teacher.ClassResponse>> call,
                                   Response<List<com.example.enggo.teacher.ClassResponse>> response) {
                if (generation != deadlineLoadGeneration) {
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    calendarSetup.updateEvents(deadlineEvents);
                    return;
                }
                List<com.example.enggo.teacher.ClassResponse> classes = response.body();
                if (classes.isEmpty()) {
                    calendarSetup.updateEvents(deadlineEvents);
                    return;
                }
                final int[] pending = {classes.size()};
                for (com.example.enggo.teacher.ClassResponse clazz : classes) {
                    apiService.getAssignments(token, clazz.id).enqueue(new Callback<List<com.example.enggo.teacher.AssignmentResponse>>() {
                        @Override
                        public void onResponse(Call<List<com.example.enggo.teacher.AssignmentResponse>> call,
                                               Response<List<com.example.enggo.teacher.AssignmentResponse>> response) {
                            if (generation != deadlineLoadGeneration) {
                                return;
                            }
                            if (response.isSuccessful() && response.body() != null) {
                                for (com.example.enggo.teacher.AssignmentResponse assignment : response.body()) {
                                    LocalDate date = parseDeadlineDate(assignment.deadline);
                                    if (date != null) {
                                        addDeadlineEvent(date, formatAssignmentLabel(assignment, clazz));
                                    }
                                }
                            }
                            finishPending();
                        }

                        @Override
                        public void onFailure(Call<List<com.example.enggo.teacher.AssignmentResponse>> call, Throwable t) {
                            if (generation != deadlineLoadGeneration) {
                                return;
                            }
                            finishPending();
                        }

                        private void finishPending() {
                            pending[0] -= 1;
                            if (pending[0] <= 0) {
                                calendarSetup.updateEvents(deadlineEvents);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<com.example.enggo.teacher.ClassResponse>> call, Throwable t) {
                if (generation != deadlineLoadGeneration) {
                    return;
                }
                calendarSetup.updateEvents(deadlineEvents);
            }
        });
    }

    private void addDeadlineEvent(LocalDate date, String label) {
        if (date == null || label == null || label.trim().isEmpty()) {
            return;
        }
        List<String> list = deadlineEvents.computeIfAbsent(date, k -> new ArrayList<>());
        String normalized = label.trim();
        if (!list.contains(normalized)) {
            list.add(normalized);
        }
    }

    private LocalDate parseDeadlineDate(String deadline) {
        if (deadline == null || deadline.trim().isEmpty()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(deadline.trim()).toLocalDate();
        } catch (Exception ignored) {
        }
        try {
            return LocalDate.parse(deadline.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private String formatAssignmentLabel(com.example.enggo.teacher.AssignmentResponse assignment,
                                         com.example.enggo.teacher.ClassResponse clazz) {
        String title = assignment.title == null || assignment.title.trim().isEmpty()
                ? "Assignment"
                : assignment.title.trim();
        String className = clazz.name == null ? "" : clazz.name.trim();
        String time = formatDeadlineTime(assignment.deadline);
        String label = className.isEmpty() ? title : title + " - " + className;
        return time == null ? label : label + " (" + time + ")";
    }

    private String formatDeadlineTime(String deadline) {
        if (deadline == null || deadline.trim().isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            return OffsetDateTime.parse(deadline.trim()).toLocalTime().format(timeFormatter);
        } catch (Exception ignored) {
            return null;
        }
    }
}
