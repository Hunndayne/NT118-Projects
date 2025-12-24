package com.example.enggo.user;
import com.example.enggo.R;
import com.example.enggo.admin.CourseAdmin;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.common.ImageSliderAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeUserActivity extends BaseUserActivity {

    private static final long AUTO_SCROLL_DELAY_MS = 4000;

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ImageSliderAdapter sliderAdapter;
    private Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private Runnable autoScrollRunnable;
    private RecyclerView recyclerCourses;
    private CourseHomeAdapter courseAdapter;
    private List<CourseAdmin> courses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        setupHeader();
        setupFooter();

        recyclerCourses = findViewById(R.id.layoutCourseList);
        recyclerCourses.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        courses = new ArrayList<>();
        courseAdapter = new CourseHomeAdapter(courses, course -> {
            Intent intent = new Intent(this, ClassUserActivity.class);
            intent.putExtra("course_id", course.getId());
            intent.putExtra("course_name", course.getName());
            intent.putExtra("course_code", course.getClassCode());
            startActivity(intent);
        });
        recyclerCourses.setAdapter(courseAdapter);

        ImageView imgArrowMyCourses = findViewById(R.id.imgArrowMyCourses);
        imgArrowMyCourses.setOnClickListener(v -> toggleSection(recyclerCourses, imgArrowMyCourses));

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

        recyclerCourses.setVisibility(View.VISIBLE);
        imgArrowMyCourses.setRotation(180f);


        layoutNotificationList.setVisibility(View.VISIBLE);
        imgArrowNotification.setRotation(180f);

        viewPager.setBackgroundResource(R.drawable.round_frame_background);
        viewPager.setClipToOutline(true);

        loadCourses();
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
                    courseAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(
                            HomeUserActivity.this,
                            "Load courses failed",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<List<CourseAdmin>> call, Throwable t) {
                Toast.makeText(
                        HomeUserActivity.this,
                        "Cannot connect to server",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
