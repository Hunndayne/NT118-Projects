package com.example.enggo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        setupHeader();
        setupFooter();
        CardView cardCourse1 = findViewById(R.id.cardCourse1);
        CardView cardCourse2 = findViewById(R.id.cardCourse2);

        cardCourse1.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClassActivity.class);
            startActivity(intent);
        });

        LinearLayout layoutCourseList = findViewById(R.id.layoutCourseList);
        ImageView imgArrowMyCourses = findViewById(R.id.imgArrowMyCourses);
        imgArrowMyCourses.setOnClickListener(v -> toggleSection(layoutCourseList, imgArrowMyCourses));

        LinearLayout layoutNotificationList = findViewById(R.id.layoutNotificationList);
        ImageView imgArrowNotification = findViewById(R.id.imgArrowNotification);
        imgArrowNotification.setOnClickListener(v -> toggleSection(layoutNotificationList, imgArrowNotification));
    }

    private void toggleSection(View content, ImageView arrow) {
        boolean isVisible = content.getVisibility() == View.VISIBLE;
        content.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        arrow.animate().rotation(isVisible ? 0f : 180f).setDuration(300).start();
    }
}
