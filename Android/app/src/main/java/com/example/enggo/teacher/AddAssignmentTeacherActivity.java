package com.example.enggo.teacher;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.documentfile.provider.DocumentFile;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.api.PresignUploadRequest;
import com.example.enggo.api.PresignUploadResponse;
import com.example.enggo.teacher.AssignmentCreateRequest;
import com.example.enggo.teacher.AssignmentResponse;
import com.example.enggo.teacher.AssignmentResourceRequest;
import com.example.enggo.teacher.AssignmentResourceResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.MediaType;
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
    private TextView tvSelectedFileName;
    private Button btnAddAttachment;
    private Button btnCancel;
    private Button btnCreate;
    private Long courseId;
    private Long classIdForUpload;
    private ActivityResultLauncher<String> filePickerLauncher;
    private String selectedFileUri;

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
        tvSelectedFileName = findViewById(R.id.tvSelectedFileName);
        btnAddAttachment = findViewById(R.id.buttonAddAttachment_Assignment_admin);
        btnCancel = findViewById(R.id.buttonCancelAddAssignment_admin);
        btnCreate = findViewById(R.id.buttonCreateAssignment_admin);
    }

    private void setupListeners() {
        if (btnAddAttachment != null) {
            btnAddAttachment.setOnClickListener(v -> filePickerLauncher.launch("*/*"));
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> createAssignment());
        }

        if (etStartTime != null) {
            etStartTime.setOnClickListener(v -> showDateTimePicker(etStartTime));
        }

        if (etDueTime != null) {
            etDueTime.setOnClickListener(v -> showDateTimePicker(etDueTime));
        }
    }

    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
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
            // best effort
        }
        selectedFileUri = uri.toString();
        String fileName = getFileName(uri);
        if (tvSelectedFileName != null) {
            tvSelectedFileName.setText(fileName);
            tvSelectedFileName.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void createAssignment() {
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
                        if (response.isSuccessful()) {
                            AssignmentResponse assignment = response.body();
                            if (assignment != null && assignment.id != null) {
                                Long resolvedClassId = assignment.classId != null ? assignment.classId : courseId;
                                classIdForUpload = resolvedClassId;
                                attachResources(token, assignment.id, resolvedClassId, attachLink);
                            }
                            setResult(RESULT_OK);
                            finish();
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
                    }
                });
    }

    private void attachResources(String token, Long assignmentId, Long classId, String link) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        if (selectedFileUri != null && !selectedFileUri.trim().isEmpty()) {
            uploadAndAttachFile(apiService, token, assignmentId, classId, Uri.parse(selectedFileUri));
        }

        if (link != null && !link.trim().isEmpty()) {
            AssignmentResourceRequest linkRequest = new AssignmentResourceRequest(
                    "LINK",
                    "Attachment Link",
                    null,
                    link,
                    null
            );
            apiService.addAssignmentResource(token, courseId, assignmentId, linkRequest)
                    .enqueue(new Callback<AssignmentResourceResponse>() {
                        @Override
                        public void onResponse(Call<AssignmentResourceResponse> call, Response<AssignmentResourceResponse> response) {
                            // no-op
                        }

                        @Override
                        public void onFailure(Call<AssignmentResourceResponse> call, Throwable t) {
                            // no-op
                        }
                    });
        }
    }

    private void uploadAndAttachFile(ApiService apiService, String token, Long assignmentId, Long classId, Uri fileUri) {
        long contentLength = resolveContentLength(fileUri);
        if (contentLength <= 0) {
            Toast.makeText(this, "Cannot determine file size for upload", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestBody requestBody = createRequestBody(fileUri, contentLength);
        if (requestBody == null) {
            Toast.makeText(this, "Cannot read file for upload", Toast.LENGTH_SHORT).show();
            return;
        }
        String fileName = sanitizeFileName(getFileName(fileUri), fileUri);
        String contentType = resolveContentType(fileUri);
        Log.d("AddAssignmentTeacher", "Presign upload for " + fileName + " to assignment " + assignmentId);

        Long resolvedClassId = classId != null ? classId : courseId;
        PresignUploadRequest presignRequest = new PresignUploadRequest(
                "ASSIGNMENT_RESOURCE",
                fileName,
                contentType,
                resolvedClassId,
                null,
                assignmentId
        );

        apiService.presignUpload(token, presignRequest).enqueue(new Callback<PresignUploadResponse>() {
            @Override
            public void onResponse(Call<PresignUploadResponse> call, Response<PresignUploadResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().uploadUrl == null) {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorBody = e.getMessage();
                    }
                    Log.e("AddAssignmentTeacher", "Presign failed code=" + response.code() + " body=" + errorBody);
                    Toast.makeText(
                            AddAssignmentTeacherActivity.this,
                            "Upload file failed (" + response.code() + ")",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                PresignUploadResponse presign = response.body();
                String uploadContentType = presign.contentType != null ? presign.contentType : contentType;

                apiService.uploadToPresignedUrl(presign.uploadUrl, uploadContentType, contentLength, requestBody)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> uploadResponse) {
                                if (!uploadResponse.isSuccessful()) {
                                    Log.e("AddAssignmentTeacher", "Upload failed code=" + uploadResponse.code());
                                    Toast.makeText(
                                            AddAssignmentTeacherActivity.this,
                                            "Upload file failed (" + uploadResponse.code() + ")",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return;
                                }

                                AssignmentResourceRequest fileRequest = new AssignmentResourceRequest(
                                        "FILE",
                                        fileName,
                                        null,
                                        null,
                                        presign.publicUrl
                                );
                                apiService.addAssignmentResource(token, courseId, assignmentId, fileRequest)
                                        .enqueue(new Callback<AssignmentResourceResponse>() {
                                            @Override
                                            public void onResponse(Call<AssignmentResourceResponse> call, Response<AssignmentResourceResponse> response) {
                                                if (!response.isSuccessful()) {
                                                    Toast.makeText(
                                                            AddAssignmentTeacherActivity.this,
                                                            "Attach file failed (" + response.code() + ")",
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<AssignmentResourceResponse> call, Throwable t) {
                                                Toast.makeText(
                                                        AddAssignmentTeacherActivity.this,
                                                        "Attach file failed",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        });
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(
                                        AddAssignmentTeacherActivity.this,
                                        "Upload file failed",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
            }

            @Override
            public void onFailure(Call<PresignUploadResponse> call, Throwable t) {
                Log.e("AddAssignmentTeacher", "Presign failed", t);
                Toast.makeText(
                        AddAssignmentTeacherActivity.this,
                        "Upload file failed",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private long resolveContentLength(Uri uri) {
        long size = -1;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex >= 0) {
                        size = cursor.getLong(sizeIndex);
                    }
                }
            } catch (Exception ignored) {
                // best effort
            }
        }
        if (size <= 0) {
            DocumentFile doc = DocumentFile.fromSingleUri(this, uri);
            if (doc != null) {
                long docSize = doc.length();
                if (docSize > 0) {
                    size = docSize;
                }
            }
        }
        if (size <= 0) {
            try (AssetFileDescriptor afd = getContentResolver().openAssetFileDescriptor(uri, "r")) {
                if (afd != null) {
                    long afdSize = afd.getLength();
                    if (afdSize > 0) {
                        size = afdSize;
                    }
                }
            } catch (Exception ignored) {
                // best effort
            }
        }
        return size;
    }

    private String resolveContentType(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType != null) {
            return mimeType;
        }
        String name = getFileName(uri);
        if (name != null) {
            int dot = name.lastIndexOf('.');
            if (dot >= 0 && dot < name.length() - 1) {
                String extension = name.substring(dot + 1).toLowerCase(Locale.ROOT);
                String fromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (fromExt != null) {
                    return fromExt;
                }
            }
        }
        return "application/octet-stream";
    }

    private RequestBody createRequestBody(Uri uri, long contentLength) {
        String mimeType = resolveContentType(uri);
        MediaType mediaType = mimeType != null
                ? MediaType.parse(mimeType)
                : MediaType.parse("application/octet-stream");
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public long contentLength() {
                return contentLength;
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
}
