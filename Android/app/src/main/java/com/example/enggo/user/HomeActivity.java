package com.example.enggo.user;
import com.example.enggo.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;
import java.util.List;

public class HomeActivity extends BaseActivity {

    private static final long AUTO_SCROLL_DELAY_MS = 4000;

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ImageSliderAdapter sliderAdapter;
    private Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private Runnable autoScrollRunnable;

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

        viewPager = findViewById(R.id.viewPagerImageSlider);
        tabLayout = findViewById(R.id.tabLayoutIndicator);

        List<Integer> imageList = Arrays.asList(
                R.drawable.banner_1,
                R.drawable.banner_2,
                R.drawable.banner_3
        );

        sliderAdapter = new ImageSliderAdapter(imageList);
        viewPager.setAdapter(sliderAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
        }).attach();

        startAutoScroll(imageList.size());

        layoutCourseList.setVisibility(View.VISIBLE);
        imgArrowMyCourses.setRotation(180f);


        layoutNotificationList.setVisibility(View.VISIBLE);
        imgArrowNotification.setRotation(180f);

        viewPager.setBackgroundResource(R.drawable.round_frame_background);
        viewPager.setClipToOutline(true);
    }

    private void toggleSection(View content, ImageView arrow) {
        boolean isVisible = content.getVisibility() == View.VISIBLE;
        content.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        arrow.animate().rotation(isVisible ? 0f : 180f).setDuration(300).start();
    }

    private void startAutoScroll(int itemCount) {
        if (itemCount == 0) return;

        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = viewPager.getCurrentItem();
                currentItem++;
                if (currentItem >= itemCount) {
                    currentItem = 0;
                }
                viewPager.setCurrentItem(currentItem, true);
                autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY_MS);
            }
        };
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (autoScrollRunnable != null) {
            autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY_MS);
        }
    }
}