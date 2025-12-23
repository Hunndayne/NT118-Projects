package com.example.enggo.admin;
import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.admin.CourseAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvailableUserAdminActivity extends BaseAdminActivity {
    private RecyclerView recyclerView;
    private AvailableUsersAdapter adapter;
    private List<CourseParticipant> users;
    private Long courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_course_participants_list);

        setupAdminHeader();
        setupAdminFooter();

        Button btnAddUser = findViewById(R.id.btnAdmin_ParticipantManager);
        btnAddUser.setOnClickListener(v -> {
            Intent intent = new Intent(AvailableUserAdminActivity.this, AddUserAdminActivity.class);
            startActivity(intent);
        });

        courseId = getIntent().getLongExtra(CoursesParticipantAdmin.EXTRA_COURSE_ID, -1);
        if (courseId == -1) {
            Toast.makeText(this, "Missing course id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerAvailableUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        users = new ArrayList<>();
        adapter = new AvailableUsersAdapter(users, this::addParticipant);
        recyclerView.setAdapter(adapter);

        loadEligibleUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEligibleUsers();
    }

    private void loadEligibleUsers() {
        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getEligibleParticipants(token, courseId)
                .enqueue(new Callback<List<CourseParticipant>>() {
                    @Override
                    public void onResponse(Call<List<CourseParticipant>> call,
                                           Response<List<CourseParticipant>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            users.clear();
                            for (CourseParticipant participant : response.body()) {
                                String role = participant.getRole();
                                if (role == null) {
                                    continue;
                                }
                                String normalized = role.trim().toUpperCase();
                                if ("STUDENT".equals(normalized) || "TEACHER".equals(normalized)) {
                                    users.add(participant);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(
                                    AvailableUserAdminActivity.this,
                                    "Load available users failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CourseParticipant>> call, Throwable t) {
                        Toast.makeText(
                                AvailableUserAdminActivity.this,
                                "Cannot connect to server",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void addParticipant(CourseParticipant participant) {
        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        CourseParticipantsRequest request =
                new CourseParticipantsRequest(Collections.singletonList(participant.getId()));

        apiService.addCourseParticipants(token, courseId, request)
                .enqueue(new Callback<CourseAdmin>() {
                    @Override
                    public void onResponse(Call<CourseAdmin> call, Response<CourseAdmin> response) {
                        if (response.isSuccessful()) {
                            users.remove(participant);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(
                                    AvailableUserAdminActivity.this,
                                    "Add failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CourseAdmin> call, Throwable t) {
                        Toast.makeText(
                                AvailableUserAdminActivity.this,
                                "Add failed",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
