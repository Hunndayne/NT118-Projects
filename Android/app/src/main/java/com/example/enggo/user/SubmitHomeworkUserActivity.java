package com.example.enggo.user;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.api.FileUploadResponse;
import com.example.enggo.teacher.AssignmentResourceResponse;
import com.example.enggo.teacher.AssignmentResponse;

import android.app.DownloadManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatButton;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmitHomeworkUserActivity extends BaseUserActivity {
    private TextView tvTitle;
    private TextView tvOpenedValue;
    private TextView tvDueValue;
    private TextView tvDescription;
    private TextView tvSubmissionStatusValue;
    private TextView tvScoringStatusValue;
    private TextView tvTimeRemainingValue;
    private TextView tvLastEditedValue;
    private TextView tvSelectedFileName;
    private LinearLayout resourcesContainer;
    private LinearLayout dropZone;
    private AppCompatButton btnSaveChanges;
    private AppCompatButton btnRemove;
    private long classId;
    private long assignmentId;
    private String fallbackAttachmentUrl;
    private ActivityResultLauncher<String[]> filePickerLauncher;
    private Uri selectedFileUri;
    private String selectedFileName;
    private Date assignmentDeadline;
    private boolean deadlinePassed;
    private SubmissionResponse currentSubmission;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submit_homework);

        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());
        setupHeader();
        setupFooter();

        tvTitle = findViewById(R.id.tvTitle);
        tvOpenedValue = findViewById(R.id.tvOpenedValue);
        tvDueValue = findViewById(R.id.tvDueValue);
        tvDescription = findViewById(R.id.tvAssignmentDescription);
        tvSubmissionStatusValue = findViewById(R.id.tvSubmissionStatusValue);
        tvScoringStatusValue = findViewById(R.id.tvScoringStatusValue);
        tvTimeRemainingValue = findViewById(R.id.tvTimeRemainingValue);
        tvLastEditedValue = findViewById(R.id.tvLastEditedValue);
        tvSelectedFileName = findViewById(R.id.tvSelectedFileName);
        resourcesContainer = findViewById(R.id.assignmentResourcesContainer);
        dropZone = findViewById(R.id.dropZone);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnRemove = findViewById(R.id.btnRemove);

        Intent intent = getIntent();
        classId = intent.getLongExtra("class_id", -1L);
        assignmentId = intent.getLongExtra("assignment_id", -1L);

        setupFilePicker();
        setupListeners();
        loadAssignment();
    }

    private void setupListeners() {
        if (dropZone != null) {
            dropZone.setOnClickListener(v -> {
                if (deadlinePassed) {
                    Toast.makeText(this, "Deadline has passed", Toast.LENGTH_SHORT).show();
                    return;
                }
                filePickerLauncher.launch(new String[]{"*/*"});
            });
        }
        if (btnSaveChanges != null) {
            btnSaveChanges.setOnClickListener(v -> submitAssignment());
        }
        if (btnRemove != null) {
            btnRemove.setOnClickListener(v -> removeSubmission());
        }
    }

    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::handleFilePicked
        );
    }

    private void handleFilePicked(Uri uri) {
        if (uri == null) {
            return;
        }
        selectedFileUri = uri;
        selectedFileName = getFileName(uri);
        if (tvSelectedFileName != null) {
            tvSelectedFileName.setText(selectedFileName == null ? "Selected file" : selectedFileName);
        }
        Toast.makeText(this, "Selected: " + selectedFileName, Toast.LENGTH_SHORT).show();
    }

    private void loadAssignment() {
        if (classId <= 0 || assignmentId <= 0) {
            Toast.makeText(this, "Missing assignment info", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAssignment(token, classId, assignmentId).enqueue(new Callback<AssignmentResponse>() {
            @Override
            public void onResponse(Call<AssignmentResponse> call, Response<AssignmentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AssignmentResponse assignment = response.body();
                    tvTitle.setText(assignment.title == null ? "Assignment" : assignment.title);
                    tvDescription.setText(assignment.description == null ? "" : assignment.description);
                    tvOpenedValue.setText(formatDateTime(assignment.createdAt));
                    tvDueValue.setText(formatDateTime(assignment.deadline));
                    fallbackAttachmentUrl = assignment.attachmentUrl;
                    assignmentDeadline = parseDate(assignment.deadline);
                    updateTimeRemaining();
                    updateSubmissionActions();
                }
            }

            @Override
            public void onFailure(Call<AssignmentResponse> call, Throwable t) {
                // no-op
            }
        });

        apiService.getAssignmentResources(token, classId, assignmentId)
                .enqueue(new Callback<List<AssignmentResourceResponse>>() {
                    @Override
                    public void onResponse(Call<List<AssignmentResourceResponse>> call, Response<List<AssignmentResourceResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            renderResources(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<AssignmentResourceResponse>> call, Throwable t) {
                        // no-op
                    }
                });

        loadSubmission(token, apiService);
    }

    private void loadSubmission(String token, ApiService apiService) {
        apiService.getMySubmission(token, assignmentId).enqueue(new Callback<SubmissionResponse>() {
            @Override
            public void onResponse(Call<SubmissionResponse> call, Response<SubmissionResponse> response) {
                if (response.isSuccessful()) {
                    bindSubmission(response.body());
                    return;
                }
                if (response.code() == 404) {
                    bindSubmission(null);
                }
            }

            @Override
            public void onFailure(Call<SubmissionResponse> call, Throwable t) {
                // no-op
            }
        });
    }

    private void submitAssignment() {
        if (deadlinePassed) {
            Toast.makeText(this, "Deadline has passed", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentSubmission != null && currentSubmission.score != null) {
            Toast.makeText(this, "Graded submissions cannot be updated", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedFileUri == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            bindSubmission(currentSubmission);
            return;
        }
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        uploadFileAndSubmit(token, apiService, selectedFileUri);
    }

    private void uploadFileAndSubmit(String token, ApiService apiService, Uri fileUri) {
        RequestBody requestBody = createRequestBody(fileUri);
        if (requestBody == null) {
            Toast.makeText(this, "Cannot read file for upload", Toast.LENGTH_SHORT).show();
            return;
        }
        String fileName = sanitizeFileName(selectedFileName, fileUri);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", fileName, requestBody);
        apiService.uploadFile(token, part).enqueue(new Callback<FileUploadResponse>() {
            @Override
            public void onResponse(Call<FileUploadResponse> call, Response<FileUploadResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().fileUrl == null) {
                    Toast.makeText(
                            SubmitHomeworkUserActivity.this,
                            "Upload file failed (" + response.code() + ")",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
                SubmissionRequest request = new SubmissionRequest(null, response.body().fileUrl);
                apiService.submitAssignment(token, assignmentId, request)
                        .enqueue(new Callback<SubmissionResponse>() {
                            @Override
                            public void onResponse(Call<SubmissionResponse> call, Response<SubmissionResponse> response) {
                                if (response.isSuccessful()) {
                                    selectedFileUri = null;
                                    selectedFileName = null;
                                    if (tvSelectedFileName != null) {
                                        tvSelectedFileName.setText("No file selected");
                                    }
                                    bindSubmission(response.body());
                                } else {
                                    Toast.makeText(
                                            SubmitHomeworkUserActivity.this,
                                            "Save submission failed",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<SubmissionResponse> call, Throwable t) {
                                Toast.makeText(
                                        SubmitHomeworkUserActivity.this,
                                        "Save submission failed",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
            }

            @Override
            public void onFailure(Call<FileUploadResponse> call, Throwable t) {
                Toast.makeText(SubmitHomeworkUserActivity.this, "Upload file failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeSubmission() {
        if (deadlinePassed) {
            Toast.makeText(this, "Deadline has passed", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentSubmission != null && currentSubmission.score != null) {
            Toast.makeText(this, "Graded submissions cannot be removed", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentSubmission == null || currentSubmission.id == null) {
            Toast.makeText(this, "No submission to remove", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.deleteMySubmission(token, assignmentId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    currentSubmission = null;
                    if (tvSelectedFileName != null) {
                        tvSelectedFileName.setText("No file selected");
                    }
                    bindSubmission(null);
                } else {
                    Toast.makeText(SubmitHomeworkUserActivity.this, "Remove failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SubmitHomeworkUserActivity.this, "Remove failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindSubmission(SubmissionResponse submission) {
        currentSubmission = submission;
        boolean hasFile = submission != null && submission.fileUrl != null && !submission.fileUrl.trim().isEmpty();
        tvSubmissionStatusValue.setText(hasFile ? "Submitted" : "No submission");
        if (submission != null && submission.score != null) {
            tvScoringStatusValue.setText(submission.score + "/100");
        } else {
            tvScoringStatusValue.setText("Not graded");
        }
        tvLastEditedValue.setText(submission != null ? formatDateTime(submission.submittedAt) : "-");
        if (tvSelectedFileName != null) {
            if (selectedFileName != null && !selectedFileName.trim().isEmpty()) {
                tvSelectedFileName.setText(selectedFileName.trim());
            } else if (hasFile) {
                tvSelectedFileName.setText(extractFileName(submission.fileUrl));
            } else {
                tvSelectedFileName.setText("No file selected");
            }
        }
        updateSubmissionActions();
    }

    private void updateSubmissionActions() {
        boolean editable = !deadlinePassed;
        if (currentSubmission != null && currentSubmission.score != null) {
            editable = false;
        }
        if (dropZone != null) {
            dropZone.setEnabled(editable);
            dropZone.setClickable(editable);
            dropZone.setAlpha(editable ? 1f : 0.5f);
        }
        if (btnSaveChanges != null) {
            btnSaveChanges.setEnabled(editable);
        }
        if (btnRemove != null) {
            boolean canRemove = editable && currentSubmission != null && currentSubmission.fileUrl != null
                    && !currentSubmission.fileUrl.trim().isEmpty();
            btnRemove.setEnabled(canRemove);
        }
    }

    private void updateTimeRemaining() {
        if (assignmentDeadline == null) {
            tvTimeRemainingValue.setText("-");
            deadlinePassed = false;
            return;
        }
        long diffMs = assignmentDeadline.getTime() - System.currentTimeMillis();
        if (diffMs <= 0) {
            tvTimeRemainingValue.setText("Overdue");
            deadlinePassed = true;
            return;
        }
        long totalHours = diffMs / (1000 * 60 * 60);
        long days = totalHours / 24;
        long hours = totalHours % 24;
        String remaining;
        if (days > 0) {
            if (hours > 0) {
                remaining = days + " days " + hours + " hours";
            } else {
                remaining = days + " days";
            }
        } else {
            remaining = hours + " hours";
        }
        tvTimeRemainingValue.setText(remaining);
        deadlinePassed = false;
    }

    private void renderResources(List<AssignmentResourceResponse> resources) {
        resourcesContainer.removeAllViews();
        if (resources == null || resources.isEmpty()) {
            if (fallbackAttachmentUrl != null && !fallbackAttachmentUrl.trim().isEmpty()) {
                List<AssignmentResourceResponse> fallback = new ArrayList<>();
                AssignmentResourceResponse resource = new AssignmentResourceResponse();
                resource.type = "LINK";
                resource.title = "Attachment Link";
                resource.url = fallbackAttachmentUrl;
                fallback.add(resource);
                resources = fallback;
            } else {
                return;
            }
        }

        LinearLayout linkSection = new LinearLayout(this);
        linkSection.setOrientation(LinearLayout.VERTICAL);
        TextView linkTitle = new TextView(this);
        linkTitle.setText("Attachment Link");
        linkTitle.setTextColor(getResources().getColor(R.color.text_primary));
        linkTitle.setTextSize(13f);
        linkTitle.setTypeface(linkTitle.getTypeface(), android.graphics.Typeface.BOLD);
        linkSection.addView(linkTitle);

        LinearLayout linkList = new LinearLayout(this);
        linkList.setOrientation(LinearLayout.VERTICAL);
        linkSection.addView(linkList);

        LinearLayout fileSection = new LinearLayout(this);
        fileSection.setOrientation(LinearLayout.VERTICAL);
        TextView fileTitle = new TextView(this);
        fileTitle.setText("File attachment");
        fileTitle.setTextColor(getResources().getColor(R.color.text_primary));
        fileTitle.setTextSize(13f);
        fileTitle.setTypeface(fileTitle.getTypeface(), android.graphics.Typeface.BOLD);
        fileSection.addView(fileTitle);

        LinearLayout fileList = new LinearLayout(this);
        fileList.setOrientation(LinearLayout.VERTICAL);
        fileSection.addView(fileList);

        int linkCount = 0;
        int fileCount = 0;
        for (AssignmentResourceResponse resource : resources) {
            String type = resource.type != null ? resource.type.trim() : "";
            if ("LINK".equalsIgnoreCase(type)) {
                String link = resource.url != null ? resource.url.trim() : null;
                if (link != null) {
                    addLinkItem(linkList, link);
                    linkCount++;
                }
            } else if ("FILE".equalsIgnoreCase(type)) {
                String link = resource.filePath != null ? resource.filePath.trim() : null;
                if (link != null) {
                    String displayName = resource.title != null && !resource.title.trim().isEmpty()
                            ? resource.title.trim()
                            : extractFileName(link);
                    addFileItem(fileList, link, displayName);
                    fileCount++;
                }
            }
        }

        if (linkCount > 0) {
            resourcesContainer.addView(linkSection);
        }
        if (fileCount > 0) {
            if (linkCount > 0) {
                TextView spacer = new TextView(this);
                spacer.setText(" ");
                resourcesContainer.addView(spacer);
            }
            resourcesContainer.addView(fileSection);
        }
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

    private String formatDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
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
                Date parsed = input.parse(value.trim());
                if (parsed != null) {
                    return output.format(parsed);
                }
            } catch (Exception ignored) {
            }
        }
        return value;
    }

    private Date parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
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
                Date parsed = input.parse(value.trim());
                if (parsed != null) {
                    return parsed;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private RequestBody createRequestBody(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        MediaType mediaType = mimeType != null
                ? MediaType.parse(mimeType)
                : MediaType.parse("application/octet-stream");
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                    if (inputStream == null) {
                        throw new IOException("Cannot open input stream");
                    }
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        sink.write(buffer, 0, read);
                    }
                }
            }
        };
    }

    private String sanitizeFileName(String name, Uri uri) {
        String safe = name == null ? "" : name.trim();
        if (safe.isEmpty()) {
            String extension = getExtension(uri);
            safe = extension != null ? "attachment." + extension : "attachment";
        }
        return safe;
    }

    private String getExtension(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType == null) {
            return null;
        }
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
    }

    private String getFileName(Uri uri) {
        String name = null;
        if ("content".equals(uri.getScheme())) {
            name = queryDisplayName(uri);
        }
        if (name == null || name.trim().isEmpty()) {
            String extension = getExtension(uri);
            name = extension != null ? "attachment." + extension : "attachment";
        }
        return name;
    }

    private String queryDisplayName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }
        }
        return null;
    }
}
