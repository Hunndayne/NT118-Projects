package com.example.enggo.admin;
import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.enggo.user.HomeUserActivity;
import androidx.cardview.widget.CardView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeAdminActivity extends BaseAdminActivity{
    ImageView btnEdit1, btnEdit2;
    private CardView cardCourse1;
    private CardView cardCourse2;
    private TextView tvCourseTitle1;
    private TextView tvCourseMeta1;
    private TextView tvCourseTitle2;
    private TextView tvCourseMeta2;
    private TextView tvAccountName1;
    private TextView tvAccountMeta1;
    private TextView tvAccountName2;
    private TextView tvAccountMeta2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_admin);

        setupAdminHeader();
        setupAdminFooter();

        // Setup News section
        LinearLayout layoutNewsList = findViewById(R.id.layoutAdmin_NewsList);
        ImageView imgArrowNews = findViewById(R.id.imgArrowAdmin_News);
        imgArrowNews.setOnClickListener(v -> toggleSection(layoutNewsList, imgArrowNews));

        // Setup Manage Courses section
        LinearLayout layoutManageCourses = findViewById(R.id.layoutAdmin_ManageCourses);
        LinearLayout layoutManageCoursesList = findViewById(R.id.layoutAdmin_ManageCoursesList);
        ImageView imgArrowManageCourses = findViewById(R.id.imgArrowAdmin_ManageCourses);
        imgArrowManageCourses.setOnClickListener(v -> toggleSection(layoutManageCoursesList, imgArrowManageCourses));

        // Click on the whole Manage Courses card to navigate
        layoutManageCourses.setOnClickListener(v -> {
            Intent intent = new Intent(HomeAdminActivity.this, ManageCoursesAdminActivity.class);
            startActivity(intent);
        });

        // Setup Manage Accounts section
        LinearLayout layoutManageAccount = findViewById(R.id.layoutAdmin_ManageAccount);
        LinearLayout layoutManageAccountList = findViewById(R.id.layoutAdmin_ManageAccountList);
        ImageView imgArrowManageAccount = findViewById(R.id.imgArrowAdmin_ManageAccount);
        imgArrowManageAccount.setOnClickListener(v -> toggleSection(layoutManageAccountList, imgArrowManageAccount));

        // Click on the whole Manage Accounts card to navigate
        layoutManageAccount.setOnClickListener(v -> {
            Intent intent = new Intent(HomeAdminActivity.this, ManageAccountAdminActivity.class);
            startActivity(intent);
        });

        // Setup onclick for course cards
        cardCourse1 = findViewById(R.id.cardAdmin_Course1);
        cardCourse2 = findViewById(R.id.cardAdmin_Course2);
        tvCourseTitle1 = findViewById(R.id.tvAdminCourseTitle1);
        tvCourseMeta1 = findViewById(R.id.tvAdminCourseMeta1);
        tvCourseTitle2 = findViewById(R.id.tvAdminCourseTitle2);
        tvCourseMeta2 = findViewById(R.id.tvAdminCourseMeta2);
        tvAccountName1 = findViewById(R.id.tvAdminAccountName1);
        tvAccountMeta1 = findViewById(R.id.tvAdminAccountMeta1);
        tvAccountName2 = findViewById(R.id.tvAdminAccountName2);
        tvAccountMeta2 = findViewById(R.id.tvAdminAccountMeta2);
        
        cardCourse1.setOnClickListener(v -> {
            Intent intent = new Intent(HomeAdminActivity.this, ManageCoursesAdminActivity.class);
            startActivity(intent);
        });
        
        cardCourse2.setOnClickListener(v -> {
            Intent intent = new Intent(HomeAdminActivity.this, ManageCoursesAdminActivity.class);
            startActivity(intent);
        });

        // Setup onclick for account cards
        CardView cardAccount1 = findViewById(R.id.cardAdmin_Account1);
        CardView cardAccount2 = findViewById(R.id.cardAdmin_Account2);
        
        cardAccount1.setOnClickListener(v -> {
            Intent intent = new Intent(HomeAdminActivity.this, ManageAccountAdminActivity.class);
            startActivity(intent);
        });
        
        cardAccount2.setOnClickListener(v -> {
            Intent intent = new Intent(HomeAdminActivity.this, ManageAccountAdminActivity.class);
            startActivity(intent);
        });

        // Initialize the buttons
        btnEdit1 = findViewById(R.id.btnEdit1);
        btnEdit2 = findViewById(R.id.btnEdit2);

        // Set click listeners for the buttons
        btnEdit1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeAdminActivity.this, ManageAccountAdminActivity.class);
                startActivity(intent);
            }
        });
        btnEdit2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeAdminActivity.this, ManageAccountAdminActivity.class);
                startActivity(intent);
            }
        });

        // Initially show all sections
        layoutNewsList.setVisibility(View.VISIBLE);
        imgArrowNews.setRotation(180f);

        layoutManageCoursesList.setVisibility(View.VISIBLE);
        imgArrowManageCourses.setRotation(180f);

        layoutManageAccountList.setVisibility(View.VISIBLE);
        imgArrowManageAccount.setRotation(180f);

        loadDashboardCourses();
        loadDashboardAccounts();
    }

    private void toggleSection(View content, ImageView arrow) {
        boolean isVisible = content.getVisibility() == View.VISIBLE;
        content.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        arrow.animate().rotation(isVisible ? 0f : 180f).setDuration(300).start();
    }

    private void loadDashboardCourses() {
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAllCourses(token).enqueue(new Callback<List<CourseAdmin>>() {
            @Override
            public void onResponse(Call<List<CourseAdmin>> call, Response<List<CourseAdmin>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }
                List<CourseAdmin> courses = response.body();
                bindDashboardCourses(courses);
            }

            @Override
            public void onFailure(Call<List<CourseAdmin>> call, Throwable t) {
                Toast.makeText(HomeAdminActivity.this, "Cannot load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDashboardAccounts() {
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAllUsers(token).enqueue(new Callback<List<UserAdmin>>() {
            @Override
            public void onResponse(Call<List<UserAdmin>> call, Response<List<UserAdmin>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }
                bindDashboardAccounts(response.body());
            }

            @Override
            public void onFailure(Call<List<UserAdmin>> call, Throwable t) {
                Toast.makeText(HomeAdminActivity.this, "Cannot load accounts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindDashboardAccounts(List<UserAdmin> users) {
        if (users == null || users.isEmpty()) {
            setAccountCard(tvAccountName1, tvAccountMeta1, null);
            setAccountCard(tvAccountName2, tvAccountMeta2, null);
            return;
        }
        setAccountCard(tvAccountName1, tvAccountMeta1, users.get(0));
        if (users.size() > 1) {
            setAccountCard(tvAccountName2, tvAccountMeta2, users.get(1));
        } else {
            setAccountCard(tvAccountName2, tvAccountMeta2, null);
        }
    }

    private void setAccountCard(TextView nameView, TextView metaView, UserAdmin user) {
        if (nameView == null || metaView == null) {
            return;
        }
        if (user == null) {
            nameView.setText("No account");
            metaView.setText("");
            return;
        }
        String name = user.getFullName();
        if (name == null || name.trim().isEmpty()) {
            String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
            String last = user.getLastName() == null ? "" : user.getLastName().trim();
            name = (first + " " + last).trim();
        }
        if (name == null || name.trim().isEmpty()) {
            name = user.getUsername();
        }
        nameView.setText(name == null || name.trim().isEmpty() ? "User" : name);
        metaView.setText(formatRole(user.getRole()));
    }

    private String formatRole(String role) {
        if (role == null) {
            return "Student";
        }
        String normalized = role.trim().toUpperCase();
        switch (normalized) {
            case "SUPER_ADMIN":
            case "ADMIN":
                return "Admin";
            case "TEACHER":
                return "Teacher";
            case "STUDENT":
                return "Student";
            default:
                return role;
        }
    }

    private void bindDashboardCourses(List<CourseAdmin> courses) {
        if (courses == null || courses.isEmpty()) {
            if (cardCourse1 != null) cardCourse1.setVisibility(View.GONE);
            if (cardCourse2 != null) cardCourse2.setVisibility(View.GONE);
            return;
        }
        if (cardCourse1 != null) cardCourse1.setVisibility(View.VISIBLE);
        CourseAdmin first = courses.get(0);
        setCourseCard(tvCourseTitle1, tvCourseMeta1, first);
        if (courses.size() > 1) {
            if (cardCourse2 != null) cardCourse2.setVisibility(View.VISIBLE);
            CourseAdmin second = courses.get(1);
            setCourseCard(tvCourseTitle2, tvCourseMeta2, second);
        } else if (cardCourse2 != null) {
            cardCourse2.setVisibility(View.GONE);
        }
    }

    private void setCourseCard(TextView titleView, TextView metaView, CourseAdmin course) {
        if (course == null) {
            return;
        }
        String name = course.getName() == null ? "" : course.getName().trim();
        String code = course.getClassCode() == null ? "" : course.getClassCode().trim();
        String title = code.isEmpty() ? name : "[" + code + "] " + name;
        if (titleView != null) {
            titleView.setText(title.isEmpty() ? "Course" : title);
        }
        if (metaView != null) {
            metaView.setText(course.getLessonCount() + " lessons");
        }
    }
}
