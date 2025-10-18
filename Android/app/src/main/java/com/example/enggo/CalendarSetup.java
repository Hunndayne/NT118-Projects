package com.example.enggo;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarSetup {

    private Context context;
    private CalendarView calendarView;
    private TextView monthYearText;
    private Map<LocalDate, List<String>> events;
    private LocalDate today = LocalDate.now();

    public CalendarSetup(Context context, CalendarView calendarView, TextView monthYearText, Map<LocalDate, List<String>> events) {
        this.context = context;
        this.calendarView = calendarView;
        this.monthYearText = monthYearText;
        this.events = events;
    }

    private static class DayViewContainer extends ViewContainer {
        public TextView dayText;
        public View dotView;
        public CalendarDay day;

        public DayViewContainer(View view) {
            super(view);
            dayText = view.findViewById(R.id.dayText);
            dotView = view.findViewById(R.id.dotView);
        }
    }

    public void setup() {
        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay day) {
                container.day = day;
                container.dayText.setText(String.valueOf(day.getDate().getDayOfMonth()));

                if (day.getDate().equals(today)) {
                    container.dayText.setBackgroundResource(R.drawable.today_background);
                } else {
                    container.dayText.setBackground(null);
                }

                container.getView().setOnClickListener(v -> {
                    if (day.getPosition() == DayPosition.MonthDate) {
                        onDayClicked(day.getDate());
                    }
                });

                if (day.getPosition() == DayPosition.MonthDate) {
                    container.dayText.setVisibility(View.VISIBLE);
                    LocalDate date = day.getDate();
                    if (events.containsKey(date)) {
                        container.dayText.setTextColor(ContextCompat.getColor(context, R.color.lightblue));
                        container.dayText.setTypeface(null, Typeface.BOLD);
                        container.dotView.setVisibility(View.VISIBLE);
                    } else {
                        container.dayText.setTypeface(null, Typeface.NORMAL);
                        container.dotView.setVisibility(View.INVISIBLE);
                        // Set alternating colors for days without events
                        DayOfWeek dayOfWeek = date.getDayOfWeek();
                        if (dayOfWeek == DayOfWeek.MONDAY || dayOfWeek == DayOfWeek.WEDNESDAY || dayOfWeek == DayOfWeek.FRIDAY) {
                            container.dayText.setTextColor(ContextCompat.getColor(context, R.color.lightblue));
                        } else {
                            container.dayText.setTextColor(ContextCompat.getColor(context, R.color.black));
                        }
                    }
                } else {
                    container.dayText.setVisibility(View.INVISIBLE);
                    container.dotView.setVisibility(View.INVISIBLE);
                }
            }
        });

        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(10);
        YearMonth endMonth = currentMonth.plusMonths(10);
        DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
        calendarView.setup(startMonth, endMonth, firstDayOfWeek);
        calendarView.scrollToMonth(currentMonth);

        DateTimeFormatter titleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
        calendarView.setMonthScrollListener(calendarMonth -> {
            monthYearText.setText(titleFormatter.format(calendarMonth.getYearMonth()));
            return null;
        });
    }

    private void onDayClicked(LocalDate date) {
        List<String> dailyEvents = events.get(date);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy");
        String title = date.format(formatter);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(title);

        if (dailyEvents == null || dailyEvents.isEmpty()) {
            builder.setMessage("No events for this day.");
        } else {
            StringBuilder message = new StringBuilder();
            for (String event : dailyEvents) {
                message.append("• ").append(event).append("\n");
            }
            builder.setMessage(message.toString().trim());
        }

        builder.setPositiveButton("OK", null);
        builder.show();
    }
}
//Ví dụ sử dụng trong OnCreate MainActivity
//CalendarView calendarView = findViewById(R.id.calendarView);
//TextView monthYearText = findViewById(R.id.monthYearText);
//ImageButton previousMonthButton = findViewById(R.id.previousMonthButton);
//ImageButton nextMonthButton = findViewById(R.id.nextMonthButton);
//
//// Create some sample events
//Map<LocalDate, List<String>> events = new HashMap<>();
//        events.put(LocalDate.now(), new ArrayList<String>() {{ add("Meeting"); add("Lunch"); }});
//        events.put(LocalDate.now().plusDays(2), new ArrayList<String>() {{ add("Dentist"); }});
//
//// Setup the calendar
//CalendarSetup calendarSetup = new CalendarSetup(this, calendarView, monthYearText, events);
//        calendarSetup.setup();
//
//        previousMonthButton.setOnClickListener(v -> {
//CalendarMonth currentMonth = calendarView.findFirstVisibleMonth();
//            if (currentMonth != null) {
//        calendarView.scrollToMonth(currentMonth.getYearMonth().minusMonths(1));
//        }
//        });
//
//        nextMonthButton.setOnClickListener(v -> {
//CalendarMonth currentMonth = calendarView.findFirstVisibleMonth();
//            if (currentMonth != null) {
//        calendarView.scrollToMonth(currentMonth.getYearMonth().plusMonths(1));
//        }
//        });
