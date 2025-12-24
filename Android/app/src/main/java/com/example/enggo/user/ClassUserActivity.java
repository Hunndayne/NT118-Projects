package com.example.enggo.user;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.teacher.AssignmentResponse;
import com.example.enggo.teacher.LessonResponse;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassUserActivity extends BaseUserActivity {
    private TextView tvCourseName;
    private TextView tvWelcome;
    private LinearLayout lectureListContainer;
    private LinearLayout homeworkListContainer;
    private long classId;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_course);
        setupHeader();
        setupFooter();

        tvCourseName = findViewById(R.id.tvCourseName);
        tvWelcome = findViewById(R.id.tvWelcome);
        lectureListContainer = findViewById(R.id.lectureListContainer);
        homeworkListContainer = findViewById(R.id.homeworkListContainer);

        Intent intent = getIntent();
        classId = intent != null ? intent.getLongExtra("course_id", -1L) : -1L;
        String courseName = intent != null ? intent.getStringExtra("course_name") : null;
        String courseCode = intent != null ? intent.getStringExtra("course_code") : null;
        if (courseName != null && !courseName.trim().isEmpty()) {
            tvCourseName.setText(courseName);
            String welcomeText = courseName;
            if (courseCode != null && !courseCode.trim().isEmpty()) {
                welcomeText = courseName + " " + courseCode;
            }
            tvWelcome.setText("Welcome back to " + welcomeText + "!");
        }

        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());

        setupSectionToggles();

        String token = getTokenFromDb();
        if (token == null || classId <= 0) {
            Toast.makeText(this, "Missing course data", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService = ApiClient.getClient().create(ApiService.class);
        loadLessons(token);
        loadAssignments(token);
    }

    private void setupSectionToggles() {
        LinearLayout headerAnnouncement = findViewById(R.id.headerAnnouncement);
        LinearLayout headerLecture = findViewById(R.id.headerLecture);
        LinearLayout headerHomework = findViewById(R.id.headerHomework);
        LinearLayout headerForum = findViewById(R.id.headerForum);

        CardView contentAnnouncement = findViewById(R.id.contentAnnouncement);
        CardView contentLecture = findViewById(R.id.contentLecture);
        CardView contentHomework = findViewById(R.id.contentHomework);
        CardView contentForum = findViewById(R.id.contentForum);

        ImageView imgArrow1 = findViewById(R.id.imgArrow1);
        ImageView imgArrow2 = findViewById(R.id.imgArrow2);
        ImageView imgArrowHomework = findViewById(R.id.imgArrowHomework);
        ImageView imgArrow4 = findViewById(R.id.imgArrow4);

        headerAnnouncement.setOnClickListener(v -> toggleSection(contentAnnouncement, imgArrow1));
        headerLecture.setOnClickListener(v -> toggleSection(contentLecture, imgArrow2));
        headerHomework.setOnClickListener(v -> toggleSection(contentHomework, imgArrowHomework));
        headerForum.setOnClickListener(v -> toggleSection(contentForum, imgArrow4));
    }

    private void loadLessons(String token) {
        apiService.getLessons(token, classId).enqueue(new Callback<List<LessonResponse>>() {
            @Override
            public void onResponse(Call<List<LessonResponse>> call, Response<List<LessonResponse>> response) {
                lectureListContainer.removeAllViews();
                if (response.isSuccessful() && response.body() != null) {
                    List<LessonResponse> lessons = response.body();
                    if (lessons.isEmpty()) {
                        addEmptyState(lectureListContainer, "No lessons yet");
                        return;
                    }
                    for (LessonResponse lesson : lessons) {
                        addListItem(
                                lectureListContainer,
                                lesson.title,
                                lesson.description,
                                null,
                                v -> {
                                    Intent intent = new Intent(ClassUserActivity.this, LessonDetailUserActivity.class);
                                    intent.putExtra("lesson_id", lesson.id);
                                    intent.putExtra("class_id", classId);
                                    intent.putExtra("lesson_title", lesson.title);
                                    intent.putExtra("lesson_description", lesson.description);
                                    startActivity(intent);
                                }
                        );
                    }
                } else {
                    addEmptyState(lectureListContainer, "Load lessons failed");
                }
            }

            @Override
            public void onFailure(Call<List<LessonResponse>> call, Throwable t) {
                lectureListContainer.removeAllViews();
                addEmptyState(lectureListContainer, "Cannot connect to server");
            }
        });
    }

    private void loadAssignments(String token) {
        apiService.getAssignments(token, classId).enqueue(new Callback<List<AssignmentResponse>>() {
            @Override
            public void onResponse(Call<List<AssignmentResponse>> call, Response<List<AssignmentResponse>> response) {
                homeworkListContainer.removeAllViews();
                if (response.isSuccessful() && response.body() != null) {
                    List<AssignmentResponse> assignments = response.body();
                    if (assignments.isEmpty()) {
                        addEmptyState(homeworkListContainer, "No assignments yet");
                        return;
                    }
                    for (AssignmentResponse assignment : assignments) {
                        String dueText = formatDeadline(assignment.deadline);
                        String meta = dueText == null ? null : "Due: " + dueText;
                        addListItem(
                                homeworkListContainer,
                                assignment.title,
                                assignment.description,
                                meta,
                                v -> {
                                    Intent intent = new Intent(ClassUserActivity.this, SubmitHomeworkUserActivity.class);
                                    intent.putExtra("assignment_id", assignment.id);
                                    intent.putExtra("class_id", classId);
                                    startActivity(intent);
                                }
                        );
                    }
                } else {
                    addEmptyState(homeworkListContainer, "Load assignments failed");
                }
            }

            @Override
            public void onFailure(Call<List<AssignmentResponse>> call, Throwable t) {
                homeworkListContainer.removeAllViews();
                addEmptyState(homeworkListContainer, "Cannot connect to server");
            }
        });
    }

    private void addEmptyState(LinearLayout container, String message) {
        addListItem(container, message, null, null, null);
    }

    private void addListItem(
            LinearLayout container,
            String title,
            String description,
            String meta,
            View.OnClickListener clickListener
    ) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemView = inflater.inflate(R.layout.course_content_item, container, false);

        TextView tvTitle = itemView.findViewById(R.id.tvTitle);
        TextView tvDescription = itemView.findViewById(R.id.tvDescription);
        TextView tvMeta = itemView.findViewById(R.id.tvMeta);

        tvTitle.setText(title != null ? title : "");
        if (description == null || description.trim().isEmpty()) {
            tvDescription.setVisibility(View.GONE);
        } else {
            tvDescription.setText(description);
        }

        if (meta == null || meta.trim().isEmpty()) {
            tvMeta.setVisibility(View.GONE);
        } else {
            tvMeta.setText(meta);
        }

        if (clickListener != null) {
            itemView.setOnClickListener(clickListener);
        }

        container.addView(itemView);

        View divider = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        );
        params.topMargin = 6;
        params.bottomMargin = 6;
        divider.setLayoutParams(params);
        divider.setBackgroundColor(getResources().getColor(R.color.divider_color));
        container.addView(divider);
    }

    private String formatDeadline(String deadline) {
        if (deadline == null || deadline.trim().isEmpty()) {
            return null;
        }
        SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        SimpleDateFormat[] inputs = new SimpleDateFormat[]{
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mmX", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        };
        for (SimpleDateFormat input : inputs) {
            try {
                Date parsed = input.parse(deadline.trim());
                if (parsed != null) {
                    return output.format(parsed);
                }
            } catch (Exception ignored) {
            }
        }
        return deadline;
    }

    /**
     * Hàm trợ giúp để ẩn/hiện nội dung và xoay mũi tên
     * @param content View nội dung (CardView) để ẩn/hiện
     * @param arrow View mũi tên (ImageView) để xoay
     */
    private void toggleSection(View content, View arrow) {
        boolean isVisible = content.getVisibility() == View.VISIBLE;

        if (isVisible) {
            content.setVisibility(View.GONE);
            arrow.animate().rotation(0).setDuration(300).start();
        } else {
            content.setVisibility(View.VISIBLE);
            arrow.animate().rotation(180).setDuration(300).start();
        }
    }
}
