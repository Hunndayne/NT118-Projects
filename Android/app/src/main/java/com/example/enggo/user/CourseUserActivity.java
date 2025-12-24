package com.example.enggo.user;
import com.example.enggo.R;
import com.example.enggo.admin.CourseAdmin;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.common.CalendarSetup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.view.CalendarView;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseUserActivity extends BaseUserActivity {
    private RecyclerView recyclerCourses;
    private CourseStudentAdapter adapter;
    private List<CourseAdmin> courses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_student);
        setupHeader();
        setupFooter();
        //Ví dụ sử dụng trong OnCreate MainActivity
        CalendarView calendarView = findViewById(R.id.calendarView);
        TextView monthYearText = findViewById(R.id.monthYearText);
        ImageButton previousMonthButton = findViewById(R.id.previousMonthButton);
        ImageButton nextMonthButton = findViewById(R.id.nextMonthButton);

        // Create some sample events
        Map<LocalDate, List<String>> events = new HashMap<>();
        events.put(LocalDate.now(), new ArrayList<String>() {{
            add("Meeting");
            add("Lunch");
        }});
        events.put(LocalDate.now().plusDays(2), new ArrayList<String>() {{
            add("Dentist");
        }});

        // Setup the calendar
        CalendarSetup calendarSetup = new CalendarSetup(this, calendarView, monthYearText, previousMonthButton, nextMonthButton, events);
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

        recyclerCourses = findViewById(R.id.recyclerCourses);
        recyclerCourses.setLayoutManager(new LinearLayoutManager(this));
        courses = new ArrayList<>();
        adapter = new CourseStudentAdapter(courses, course -> {
            Intent intent = new Intent(CourseUserActivity.this, ClassUserActivity.class);
            intent.putExtra("course_id", course.getId());
            intent.putExtra("course_name", course.getName());
            intent.putExtra("course_code", course.getClassCode());
            startActivity(intent);
        });
        recyclerCourses.setAdapter(adapter);

        loadCourses();

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCourses();
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
                    courses.clear();
                    courses.addAll(response.body());
                    adapter.notifyDataSetChanged();
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
}
