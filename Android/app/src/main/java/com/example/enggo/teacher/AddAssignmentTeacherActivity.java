package com.example.enggo.teacher;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.documentfile.provider.DocumentFile;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.api.PresignUploadRequest;
import com.example.enggo.api.PresignUploadResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAssignmentTeacherActivity extends BaseTeacherActivity {
    private EditText etTitle;
    private EditText etContent;
    private EditText etAttachLink;
    private EditText etStartTime;
    private EditText etDueTime;
    private Button btnAddAttachment;
    private Button btnCancel;
    private Button btnCreate;
    private Long courseId;
    private ActivityResultLauncher<String[]> filePickerLauncher;
    private Uri selectedFileUri;
    private String selectedFileName;
    private boolean isSaving;
    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_assignment_teacher);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        setupFilePicker();
        setupListeners();

        courseId = getIntent().getLongExtra(ManageAssignmentsTeacherActivity.EXTRA_COURSE_ID, -1);
    }

    private void initViews() {
        etTitle = findViewById(R.id.etAddAssignmentTitle_admin);
        etContent = findViewById(R.id.etAddAssignmentContent_admin);
        etAttachLink = findViewById(R.id.etAttachLinkContentAddAssignment_admin);
        etStartTime = findViewById(R.id.etStartTimeAddAssignment_admin);
        etDueTime = findViewById(R.id.etDueTimeAddAssignment_admin);
        btnAddAttachment = findViewById(R.id.buttonAddAttachment_Assignment_admin);
        btnCancel = findViewById(R.id.buttonCancelAddAssignment_admin);
        btnCreate = findViewById(R.id.buttonCreateAssignment_admin);
    }

    private void setupListeners() {
        if (btnAddAttachment != null) {
            btnAddAttachment.setOnClickListener(v -> filePickerLauncher.launch(new String[]{"*/*"}));
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> {
                createAssignment();
            });
        }

        if (etStartTime != null) {
            etStartTime.setOnClickListener(v -> {
                showDateTimePicker(etStartTime);
            });
        }

        if (etDueTime != null) {
            etDueTime.setOnClickListener(v -> {
                showDateTimePicker(etDueTime);
            });
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
        try {
            getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (Exception ignored) {
            // Best effort; some providers do not allow persistable permission.
        }
        selectedFileUri = uri;
        selectedFileName = getFileName(uri);
        if (btnAddAttachment != null) {
            String label = selectedFileName == null || selectedFileName.trim().isEmpty()
                    ? "Selected file"
                    : selectedFileName;
            btnAddAttachment.setText(label);
        }
    }

    private void createAssignment() {
        if (isSaving) {
            return;
        }
        if (courseId == null || courseId == -1) {
            Toast.makeText(this, "Missing course id", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTitle.getText().toString().trim();
        String description = etContent.getText().toString().trim();
        String attachLink = etAttachLink.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String dueTime = etDueTime.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Assignment title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = getTokenFromDb();
        if (token == null) {
            Toast.makeText(this, "Missing token", Toast.LENGTH_SHORT).show();
            return;
        }
        setSaving(true);
        Log.d("AddAssignment", "Creating assignment for courseId: " + courseId + ", title: " + title + ", dueTime: " + dueTime);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        AssignmentCreateRequest request = new AssignmentCreateRequest(
                title,
                description.isEmpty() ? null : description,
                null,
                dueTime.isEmpty() ? null : dueTime,
                startTime.isEmpty() ? null : startTime
        );

        apiService.createAssignment(token, courseId, request)
                .enqueue(new Callback<AssignmentResponse>() {
                    @Override
                    public void onResponse(Call<AssignmentResponse> call, Response<AssignmentResponse> response) {
                        Log.d("AddAssignment", "Response code: " + response.code());
                        if (response.isSuccessful() && response.body() != null && response.body().id != null) {
                            Log.d("AddAssignment", "Assignment created successfully");
                            attachResources(token, response.body().id, attachLink, selectedFileUri);
                        } else if (response.isSuccessful()) {
                            Toast.makeText(
                                    AddAssignmentTeacherActivity.this,
                                    "Assignment created but missing id",
                                    Toast.LENGTH_SHORT
                            ).show();
                            finishWithResult();
                        } else {
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                errorBody = e.getMessage();
                            }
                            Log.e("AddAssignment", "Create failed: " + response.code() + " - " + errorBody);
                            Toast.makeText(
                                    AddAssignmentTeacherActivity.this,
                                    "Create assignment failed: " + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                            setSaving(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<AssignmentResponse> call, Throwable t) {
                        Log.e("AddAssignment", "Network error: " + t.getMessage(), t);
                        Toast.makeText(
                                AddAssignmentTeacherActivity.this,
                                "Cannot connect to server: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                        setSaving(false);
                    }
                });
    }

    private void showDateTimePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timePicker = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                String formatted = String.format(
                                        Locale.getDefault(),
                                        "%04d-%02d-%02d %02d:%02d",
                                        year,
                                        month + 1,
                                        dayOfMonth,
                                        hourOfDay,
                                        minute
                                );
                                target.setText(formatted);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );
                    timePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void attachResources(String token, Long assignmentId, String link, Uri fileUri) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        final int[] pending = {0};
        final boolean[] hasError = {false};
        Runnable checkDone = () -> {
            if (pending[0] > 0) {
                return;
            }
            if (hasError[0]) {
                Toast.makeText(
                        AddAssignmentTeacherActivity.this,
                        "Assignment created, but some attachments failed",
                        Toast.LENGTH_SHORT
                ).show();
            }
            finishWithResult();
        };

        String trimmedLink = link == null ? "" : link.trim();
        if (!trimmedLink.isEmpty()) {
            pending[0] += 1;
            AssignmentResourceRequest linkRequest = new AssignmentResourceRequest(
                    "LINK",
                    "Attachment Link",
                    null,
                    trimmedLink,
                    null
            );
            apiService.addAssignmentResource(token, courseId, assignmentId, linkRequest)
                    .enqueue(new Callback<AssignmentResourceResponse>() {
                        @Override
                        public void onResponse(Call<AssignmentResourceResponse> call,
                                               Response<AssignmentResourceResponse> response) {
                            if (!response.isSuccessful()) {
                                hasError[0] = true;
                            }
                            pending[0] -= 1;
                            checkDone.run();
                        }

                        @Override
                        public void onFailure(Call<AssignmentResourceResponse> call, Throwable t) {
                            hasError[0] = true;
                            pending[0] -= 1;
                            checkDone.run();
                        }
                    });
        }

        if (fileUri != null) {
            pending[0] += 1;
            uploadAndAttachFile(apiService, token, assignmentId, fileUri, success -> {
                if (!success) {
                    hasError[0] = true;
                }
                pending[0] -= 1;
                checkDone.run();
            });
        }

        if (pending[0] == 0) {
            checkDone.run();
        }
    }

    private void uploadAndAttachFile(ApiService apiService,
                                     String token,
                                     Long assignmentId,
                                     Uri fileUri,
                                     AttachmentCallback callback) {
        String fileName = sanitizeFileName(getFileName(fileUri), fileUri);
        String contentType = resolveContentType(fileUri);
        Log.d("AddAssignment", "Uploading " + fileName + " to assignment " + assignmentId);
        Toast.makeText(this, "Uploading " + fileName, Toast.LENGTH_SHORT).show();

        PresignUploadRequest presignRequest = new PresignUploadRequest(
                "ASSIGNMENT_ATTACHMENT",
                fileName,
                contentType,
                courseId,
                null,
                assignmentId
        );
        apiService.presignUpload(token, presignRequest).enqueue(new Callback<PresignUploadResponse>() {
            @Override
            public void onResponse(Call<PresignUploadResponse> call, Response<PresignUploadResponse> response) {
                PresignUploadResponse presign = response.body();
                if (!response.isSuccessful() || presign == null || presign.uploadUrl == null || presign.publicUrl == null) {
                    Toast.makeText(
                            AddAssignmentTeacherActivity.this,
                            "Get upload URL failed (" + response.code() + ")",
                            Toast.LENGTH_SHORT
                    ).show();
                    callback.onComplete(false);
                    return;
                }
                uploadToPresignedUrl(presign.uploadUrl, contentType, fileUri, uploadSuccess -> {
                    if (!uploadSuccess) {
                        Toast.makeText(
                                AddAssignmentTeacherActivity.this,
                                "Upload file failed",
                                Toast.LENGTH_SHORT
                        ).show();
                        callback.onComplete(false);
                        return;
                    }
                    String title = fileName;
                    AssignmentResourceRequest fileRequest = new AssignmentResourceRequest(
                            "FILE",
                            title,
                            null,
                            null,
                            presign.publicUrl
                    );
                    apiService.addAssignmentResource(token, courseId, assignmentId, fileRequest)
                            .enqueue(new Callback<AssignmentResourceResponse>() {
                                @Override
                                public void onResponse(Call<AssignmentResourceResponse> call,
                                                       Response<AssignmentResourceResponse> response) {
                                    if (!response.isSuccessful()) {
                                        Toast.makeText(
                                                AddAssignmentTeacherActivity.this,
                                                "Attach file failed (" + response.code() + ")",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                        callback.onComplete(false);
                                        return;
                                    }
                                    callback.onComplete(true);
                                }

                                @Override
                                public void onFailure(Call<AssignmentResourceResponse> call, Throwable t) {
                                    Toast.makeText(
                                            AddAssignmentTeacherActivity.this,
                                            "Attach file failed",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    callback.onComplete(false);
                                }
                            });
                });
            }

            @Override
            public void onFailure(Call<PresignUploadResponse> call, Throwable t) {
                Toast.makeText(
                        AddAssignmentTeacherActivity.this,
                        "Get upload URL failed",
                        Toast.LENGTH_SHORT
                ).show();
                callback.onComplete(false);
            }
        });
    }

    private void uploadToPresignedUrl(String uploadUrl,
                                      String contentType,
                                      Uri fileUri,
                                      AttachmentCallback callback) {
        RequestBody requestBody = createRequestBody(fileUri, contentType);
        if (requestBody == null) {
            Toast.makeText(this, "Cannot read file for upload", Toast.LENGTH_SHORT).show();
            callback.onComplete(false);
            return;
        }
        Request request = new Request.Builder()
                .url(uploadUrl)
                .put(requestBody)
                .addHeader("Content-Type", contentType)
                .build();
        httpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> callback.onComplete(false));
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                boolean ok = response.isSuccessful();
                response.close();
                runOnUiThread(() -> callback.onComplete(ok));
            }
        });
    }

    private String resolveContentType(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType == null || mimeType.trim().isEmpty()) {
            return "application/octet-stream";
        }
        return mimeType;
    }

    private RequestBody createRequestBody(Uri uri, String contentType) {
        String resolved = contentType;
        if (resolved == null || resolved.trim().isEmpty()) {
            resolved = resolveContentType(uri);
        }
        MediaType mediaType = MediaType.parse(resolved);
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

    private String getFileName(Uri uri) {
        String name = null;
        if ("content".equals(uri.getScheme())) {
            name = queryDisplayName(uri);
            if ((name == null || name.trim().isEmpty() || looksEncodedName(name))
                    && DocumentsContract.isDocumentUri(this, uri)) {
                name = resolveDocumentDisplayName(uri);
            }
        }
        if (name == null || name.trim().isEmpty() || looksEncodedName(name)) {
            DocumentFile doc = DocumentFile.fromSingleUri(this, uri);
            if (doc != null && doc.getName() != null && !doc.getName().trim().isEmpty()) {
                name = doc.getName();
            }
        }
        if (name == null || name.trim().isEmpty() || looksEncodedName(name)) {
            String extension = getExtension(uri);
            name = extension != null ? "attachment." + extension : "attachment";
        }
        return name;
    }

    private boolean looksEncodedName(String name) {
        String lowered = name.toLowerCase(Locale.getDefault());
        return lowered.contains("%3a") || lowered.contains("document:") || lowered.contains("content://");
    }

    private String sanitizeFileName(String name, Uri uri) {
        String safe = name == null ? "" : name.trim();
        if (safe.isEmpty() || looksEncodedName(safe)) {
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

    private String resolveDocumentDisplayName(Uri uri) {
        try {
            String documentId = DocumentsContract.getDocumentId(uri);
            String[] parts = documentId.split(":");
            if (parts.length != 2) {
                return null;
            }
            String type = parts[0];
            String id = parts[1];
            Uri contentUri;
            switch (type) {
                case "image":
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    break;
                case "video":
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    break;
                case "audio":
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    break;
                default:
                    contentUri = MediaStore.Files.getContentUri("external");
                    break;
            }
            String selection = MediaStore.MediaColumns._ID + "=?";
            String[] selectionArgs = new String[]{id};
            try (Cursor cursor = getContentResolver().query(
                    contentUri,
                    new String[]{OpenableColumns.DISPLAY_NAME},
                    selection,
                    selectionArgs,
                    null
            )) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        return cursor.getString(index);
                    }
                }
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private void finishWithResult() {
        setResult(RESULT_OK);
        finish();
    }

    private void setSaving(boolean saving) {
        isSaving = saving;
        if (btnCreate != null) {
            btnCreate.setEnabled(!saving);
        }
        if (btnCancel != null) {
            btnCancel.setEnabled(!saving);
        }
        if (btnAddAttachment != null) {
            btnAddAttachment.setEnabled(!saving);
        }
        if (etTitle != null) {
            etTitle.setEnabled(!saving);
        }
        if (etContent != null) {
            etContent.setEnabled(!saving);
        }
        if (etAttachLink != null) {
            etAttachLink.setEnabled(!saving);
        }
        if (etStartTime != null) {
            etStartTime.setEnabled(!saving);
        }
        if (etDueTime != null) {
            etDueTime.setEnabled(!saving);
        }
    }

    private interface AttachmentCallback {
        void onComplete(boolean success);
    }
}
