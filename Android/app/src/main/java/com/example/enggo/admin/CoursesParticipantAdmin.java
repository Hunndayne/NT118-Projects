package com.example.enggo.admin;
import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.admin.CourseAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CoursesParticipantAdmin extends BaseAdminActivity {
    public static final String EXTRA_COURSE_ID = "COURSE_ID";

    private RecyclerView recyclerView;
    private CourseParticipantsAdapter adapter;
    private List<CourseParticipant> participants;
    private Long courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_view_course_participants_list);

        setupAdminHeader();
        setupAdminFooter();

        courseId = getIntent().getLongExtra(EXTRA_COURSE_ID, -1);
        if (courseId == -1) {
            Toast.makeText(this, "Missing course id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerCourseParticipants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        participants = new ArrayList<>();
        adapter = new CourseParticipantsAdapter(participants, this::removeParticipant);
        recyclerView.setAdapter(adapter);

        Button btnAddParticipant = findViewById(R.id.btnAddUser);
        btnAddParticipant.setOnClickListener(v -> {
            Intent intent = new Intent(this, AvailableUserAdminActivity.class);
            intent.putExtra(EXTRA_COURSE_ID, courseId);
            startActivity(intent);
        });
        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> {
            finish();
        });

        loadParticipants();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadParticipants();
    }

    private void loadParticipants() {
        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getCourseParticipants(token, courseId)
                .enqueue(new Callback<List<CourseParticipant>>() {
                    @Override
                    public void onResponse(Call<List<CourseParticipant>> call,
                                           Response<List<CourseParticipant>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            participants.clear();
                            participants.addAll(response.body());
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(
                                    CoursesParticipantAdmin.this,
                                    "Load participants failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CourseParticipant>> call, Throwable t) {
                        Toast.makeText(
                                CoursesParticipantAdmin.this,
                                "Cannot connect to server",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void removeParticipant(CourseParticipant participant) {
        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        CourseParticipantsRequest request =
                new CourseParticipantsRequest(Collections.singletonList(participant.getId()));

        apiService.removeCourseParticipants(token, courseId, request)
                .enqueue(new Callback<CourseAdmin>() {
                    @Override
                    public void onResponse(Call<CourseAdmin> call, Response<CourseAdmin> response) {
                        if (response.isSuccessful()) {
                            participants.remove(participant);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(
                                    CoursesParticipantAdmin.this,
                                    "Remove failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CourseAdmin> call, Throwable t) {
                        Toast.makeText(
                                CoursesParticipantAdmin.this,
                                "Remove failed",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
