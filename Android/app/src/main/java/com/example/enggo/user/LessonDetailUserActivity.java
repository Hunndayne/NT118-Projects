package com.example.enggo.user;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.teacher.LessonResourceResponse;
import com.example.enggo.teacher.LessonResponse;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.URLUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LessonDetailUserActivity extends BaseUserActivity {
    private TextView tvTitle;
    private TextView tvDescription;
    private LinearLayout resourceListContainer;
    private long classId;
    private long lessonId;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lesson_detail_user);
        setupHeader();
        setupFooter();

        tvTitle = findViewById(R.id.tvLessonTitle);
        tvDescription = findViewById(R.id.tvLessonDescription);
        resourceListContainer = findViewById(R.id.resourceListContainer);

        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        classId = intent != null ? intent.getLongExtra("class_id", -1L) : -1L;
        lessonId = intent != null ? intent.getLongExtra("lesson_id", -1L) : -1L;
        String title = intent != null ? intent.getStringExtra("lesson_title") : null;
        String description = intent != null ? intent.getStringExtra("lesson_description") : null;
        if (title != null) {
            tvTitle.setText(title);
        }
        if (description != null && !description.trim().isEmpty()) {
            tvDescription.setText(description);
        }

        String token = getTokenFromDb();
        if (token == null || classId <= 0 || lessonId <= 0) {
            Toast.makeText(this, "Missing lesson data", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService = ApiClient.getClient().create(ApiService.class);
        loadLessonDetail(token);
    }

    private void loadLessonDetail(String token) {
        apiService.getLesson(token, classId, lessonId).enqueue(new Callback<LessonResponse>() {
            @Override
            public void onResponse(Call<LessonResponse> call, Response<LessonResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LessonResponse lesson = response.body();
                    if (lesson.title != null) {
                        tvTitle.setText(lesson.title);
                    }
                    if (lesson.description != null && !lesson.description.trim().isEmpty()) {
                        tvDescription.setText(lesson.description);
                    }
                    showResources(lesson.resources);
                } else {
                    loadResources(token);
                }
            }

            @Override
            public void onFailure(Call<LessonResponse> call, Throwable t) {
                loadResources(token);
            }
        });
    }

    private void loadResources(String token) {
        apiService.getLessonResources(token, classId, lessonId).enqueue(new Callback<List<LessonResourceResponse>>() {
            @Override
            public void onResponse(Call<List<LessonResourceResponse>> call, Response<List<LessonResourceResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showResources(response.body());
                } else {
                    showResources(null);
                }
            }

            @Override
            public void onFailure(Call<List<LessonResourceResponse>> call, Throwable t) {
                showResources(null);
            }
        });
    }

    private void showResources(List<LessonResourceResponse> resources) {
        resourceListContainer.removeAllViews();
        if (resources == null || resources.isEmpty()) {
            addEmptyState();
            return;
        }
        List<LessonResourceResponse> uniqueResources = dedupeResources(resources);
        addGroupedResources(uniqueResources);
    }

    private List<LessonResourceResponse> dedupeResources(List<LessonResourceResponse> resources) {
        List<LessonResourceResponse> unique = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (LessonResourceResponse resource : resources) {
            String key = buildResourceKey(resource);
            if (seen.add(key)) {
                unique.add(resource);
            }
        }
        return unique;
    }

    private String buildResourceKey(LessonResourceResponse resource) {
        String type = resource.type == null ? "" : resource.type.trim().toLowerCase();
        String title = resource.title == null ? "" : resource.title.trim();
        String url = resource.url == null ? "" : resource.url.trim();
        String filePath = resource.filePath == null ? "" : resource.filePath.trim();
        String content = resource.content == null ? "" : resource.content.trim();
        return type + "|" + title + "|" + url + "|" + filePath + "|" + content;
    }

    private void addEmptyState() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemView = inflater.inflate(R.layout.lesson_resource_item, resourceListContainer, false);
        TextView tvTitle = itemView.findViewById(R.id.tvResourceTitle);
        TextView tvType = itemView.findViewById(R.id.tvResourceType);
        TextView tvContent = itemView.findViewById(R.id.tvResourceContent);
        TextView tvLink = itemView.findViewById(R.id.tvResourceLink);
        tvTitle.setText("No resources yet");
        tvType.setVisibility(View.GONE);
        tvContent.setVisibility(View.GONE);
        tvLink.setVisibility(View.GONE);
        resourceListContainer.addView(itemView);
    }

    private void addGroupedResources(List<LessonResourceResponse> resources) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View groupView = inflater.inflate(R.layout.lesson_resources_group, resourceListContainer, false);
        LinearLayout linkSection = groupView.findViewById(R.id.linkSection);
        LinearLayout linkListContainer = groupView.findViewById(R.id.linkListContainer);
        LinearLayout fileSection = groupView.findViewById(R.id.fileSection);
        LinearLayout fileListContainer = groupView.findViewById(R.id.fileListContainer);

        int linkCount = 0;
        int fileCount = 0;
        for (LessonResourceResponse resource : resources) {
            String type = resource.type != null ? resource.type.trim() : "";
            if ("LINK".equalsIgnoreCase(type)) {
                String link = resource.url != null ? resource.url.trim() : null;
                link = normalizeLink(link);
                if (link != null) {
                    addLinkItem(linkListContainer, link);
                    linkCount++;
                }
            } else {
                String link = null;
                if (resource.filePath != null && !resource.filePath.trim().isEmpty()) {
                    link = resource.filePath.trim();
                } else if (resource.url != null && !resource.url.trim().isEmpty()) {
                    link = resource.url.trim();
                }
                link = normalizeLink(link);
                if (link != null) {
                    String displayName = null;
                    if (resource.title != null && !resource.title.trim().isEmpty() && !looksEncodedName(resource.title)) {
                        displayName = resource.title.trim();
                    }
                    if (displayName == null) {
                        displayName = extractFileName(link);
                    }
                    addFileItem(fileListContainer, link, displayName);
                    fileCount++;
                }
            }
        }

        if (linkCount == 0) {
            linkSection.setVisibility(View.GONE);
        }
        if (fileCount == 0) {
            fileSection.setVisibility(View.GONE);
        }

        if (linkCount == 0 && fileCount == 0) {
            addEmptyState();
            return;
        }

        resourceListContainer.addView(groupView);
    }

    private void addLinkItem(LinearLayout container, String link) {
        TextView item = new TextView(this);
        item.setText(link);
        item.setTextColor(getResources().getColor(R.color.bluelogo));
        item.setTextSize(13f);
        item.setPadding(0, 6, 0, 6);
        item.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            startActivity(intent);
        });
        container.addView(item);
    }

    private void addFileItem(LinearLayout container, String link, String displayName) {
        String label = (displayName == null || displayName.trim().isEmpty()) ? link : displayName;
        TextView item = new TextView(this);
        item.setText(label);
        item.setTextColor(getResources().getColor(R.color.bluelogo));
        item.setTextSize(13f);
        item.setPadding(0, 6, 0, 6);
        item.setOnClickListener(v -> downloadFile(link, displayName));
        container.addView(item);
    }

    private String extractFileName(String link) {
        if (link == null || link.trim().isEmpty()) {
            return null;
        }
        int lastSlash = link.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < link.length() - 1) {
            return link.substring(lastSlash + 1);
        }
        return link;
    }

    private boolean looksEncodedName(String name) {
        String lowered = name.toLowerCase(Locale.getDefault());
        return lowered.contains("%3a") || lowered.contains("document:") || lowered.contains("content://");
    }

    private String normalizeLink(String link) {
        if (link == null || link.trim().isEmpty()) {
            return null;
        }
        String trimmed = link.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")
                || trimmed.startsWith("content://") || trimmed.startsWith("file://")) {
            return trimmed;
        }
        String baseUrl = ApiClient.getBaseUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return trimmed;
        }
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String cleanedLink = trimmed.startsWith("/") ? trimmed.substring(1) : trimmed;
        return normalizedBase + cleanedLink;
    }

    private void downloadFile(String url, String fileName) {
        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(this, "Invalid file link", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return;
        }
        String safeName = fileName;
        if (safeName == null || safeName.trim().isEmpty()) {
            safeName = URLUtil.guessFileName(url, null, null);
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName);
        request.setTitle(safeName);
        request.setDescription("Downloading attachment");
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            Toast.makeText(this, "Downloading " + safeName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show();
        }
    }
}
