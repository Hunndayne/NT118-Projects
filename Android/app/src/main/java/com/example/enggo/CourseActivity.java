package com.example.enggo;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kizitonwose.calendar.core.CalendarMonth;
import  com.kizitonwose.calendar.view.CalendarView;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseActivity extends BaseActivity {

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
    }
}
