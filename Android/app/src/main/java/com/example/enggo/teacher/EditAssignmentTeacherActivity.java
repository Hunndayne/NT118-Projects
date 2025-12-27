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
import android.widget.LinearLayout;
import android.view.LayoutInflater;
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

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditAssignmentTeacherActivity extends BaseTeacherActivity {
    private EditText etTitle;
    private EditText etContent;
    private EditText etStartTime;
    private EditText etDueTime;
    private Button btnCancel;
    private Button btnSave;
    private Button btnAttachment;
    private Button btnAddLink;
    private LinearLayout fileListContainer;
    private LinearLayout linkListContainer;
    private TextView tvSelectedFileName;
    private Long courseId;
    private Long assignmentId;
    private Long classIdForUpload;
    private ActivityResultLauncher<String[]> filePickerLauncher;
    private String selectedFileUri;
    private final List<LinkRow> linkRows = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_assignment_teacher);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        setupFilePicker();
        loadAssignmentData();
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etEditAssignmentTitle_admin);
        etContent = findViewById(R.id.etEditAssignmentContent_admin);
        etStartTime = findViewById(R.id.etStartTimeEditAssignment_admin);
        etDueTime = findViewById(R.id.etDueTimeEditAssignment_admin);
        btnCancel = findViewById(R.id.buttonCancelEditAssignment_admin);
        btnSave = findViewById(R.id.buttonSaveEditAssignment_admin);
        btnAttachment = findViewById(R.id.buttonAttachment_EditAssignment_admin);
        btnAddLink = findViewById(R.id.btnAddLink);
        fileListContainer = findViewById(R.id.fileListContainer);
        linkListContainer = findViewById(R.id.linkListContainer);
        tvSelectedFileName = findViewById(R.id.tvSelectedFileName);
    }

    private void loadAssignmentData() {
        if (getIntent() != null) {
            courseId = getIntent().getLongExtra(ManageAssignmentsTeacherActivity.EXTRA_COURSE_ID, -1);
            assignmentId = getIntent().getLongExtra("assignment_id", -1);
            String title = getIntent().getStringExtra("assignment_title");
            String content = getIntent().getStringExtra("assignment_content");
            String startTime = getIntent().getStringExtra("start_time");
            String dueTime = getIntent().getStringExtra("due_time");
            if (title != null) {
                etTitle.setText(title);
            }
            if (content != null) {
                etContent.setText(content);
            }
            if (startTime != null) {
                etStartTime.setText(formatDateTime(startTime));
            }
            if (dueTime != null) {
                etDueTime.setText(formatDateTime(dueTime));
            }
        }
        loadAssignmentFromApi();
        loadResources();
    }

    private void loadAssignmentFromApi() {
        if (courseId == null || courseId == -1 || assignmentId == null || assignmentId == -1) {
            return;
        }
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAssignment(token, courseId, assignmentId).enqueue(new Callback<AssignmentResponse>() {
            @Override
            public void onResponse(Call<AssignmentResponse> call, Response<AssignmentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AssignmentResponse assignment = response.body();
                    if (assignment.classId != null) {
                        classIdForUpload = assignment.classId;
                    }
                    if (assignment.title != null) {
                        etTitle.setText(assignment.title);
                    }
                    if (assignment.description != null) {
                        etContent.setText(assignment.description);
                    }
                    if (assignment.createdAt != null) {
                        etStartTime.setText(formatDateTime(assignment.createdAt));
                    }
                    if (assignment.deadline != null) {
                        etDueTime.setText(formatDateTime(assignment.deadline));
                    }
                }
            }

            @Override
            public void onFailure(Call<AssignmentResponse> call, Throwable t) {
                // no-op
            }
        });
    }

    private void loadResources() {
        if (courseId == null || courseId == -1 || assignmentId == null || assignmentId == -1) {
            return;
        }
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAssignmentResources(token, courseId, assignmentId).enqueue(new Callback<List<AssignmentResourceResponse>>() {
            @Override
            public void onResponse(Call<List<AssignmentResourceResponse>> call, Response<List<AssignmentResourceResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }
                linkRows.clear();
                linkListContainer.removeAllViews();
                fileListContainer.removeAllViews();
                for (AssignmentResourceResponse resource : response.body()) {
                    String type = resource.type != null ? resource.type.trim() : "";
                    if ("LINK".equalsIgnoreCase(type)) {
                        addLinkRow(resource.id, resource.title, resource.url);
                    } else if ("FILE".equalsIgnoreCase(type)) {
                        addFileRow(resource);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AssignmentResourceResponse>> call, Throwable t) {
                // no-op
            }
        });
    }

    private void setupListeners() {
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (btnAttachment != null) {
            btnAttachment.setOnClickListener(v -> filePickerLauncher.launch(new String[]{"*/*"}));
        }

        if (btnAddLink != null) {
            btnAddLink.setOnClickListener(v -> addLinkRow(null, "", ""));
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> updateAssignment());
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
                new ActivityResultContracts.OpenDocument(),
                this::handleFilePicked
        );
    }

    private void handleFilePicked(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (Exception ignored) {
            // no-op
        }
        selectedFileUri = uri.toString();
        String fileName = getFileName(uri);
        if (tvSelectedFileName != null) {
            tvSelectedFileName.setText(fileName);
            tvSelectedFileName.setVisibility(android.view.View.VISIBLE);
        }
        Log.d("EditAssignmentTeacher", "Selected file: " + selectedFileUri + " name=" + fileName);
        String token = getTokenFromDb();
        if (token == null) {
            Toast.makeText(this, "Missing token", Toast.LENGTH_SHORT).show();
            return;
        }
        if (courseId == null || courseId == -1 || assignmentId == null || assignmentId == -1) {
            Toast.makeText(this, "Missing assignment info", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        uploadAndAttachFile(apiService, token, uri);
    }

    private void updateAssignment() {
        if (courseId == null || courseId == -1 || assignmentId == null || assignmentId == -1) {
            Toast.makeText(this, "Missing assignment info", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTitle.getText().toString().trim();
        String description = etContent.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String dueTime = etDueTime.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Assignment title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        AssignmentUpdateRequest request = new AssignmentUpdateRequest(
                title,
                description.isEmpty() ? null : description,
                null,
                dueTime.isEmpty() ? null : dueTime,
                startTime.isEmpty() ? null : startTime
        );

        apiService.updateAssignment(token, courseId, assignmentId, request)
                .enqueue(new Callback<AssignmentResponse>() {
                    @Override
                    public void onResponse(Call<AssignmentResponse> call, Response<AssignmentResponse> response) {
                        if (response.isSuccessful()) {
                            saveLinkResources(token);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(
                                    EditAssignmentTeacherActivity.this,
                                    "Update assignment failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AssignmentResponse> call, Throwable t) {
                        Toast.makeText(
                                EditAssignmentTeacherActivity.this,
                                "Cannot connect to server",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void saveLinkResources(String token) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        for (LinkRow row : new ArrayList<>(linkRows)) {
            String title = row.titleInput.getText().toString().trim();
            String url = row.urlInput.getText().toString().trim();

            if (url.isEmpty()) {
                if (row.resourceId != null) {
                    deleteResource(row.resourceId, null);
                }
                continue;
            }

            String finalTitle = title.isEmpty() ? "Attachment Link" : title;
            AssignmentResourceRequest request = new AssignmentResourceRequest(
                    "LINK",
                    finalTitle,
                    null,
                    url,
                    null
            );

            if (row.resourceId == null) {
                apiService.addAssignmentResource(token, courseId, assignmentId, request).enqueue(new Callback<AssignmentResourceResponse>() {
                    @Override
                    public void onResponse(Call<AssignmentResourceResponse> call, Response<AssignmentResourceResponse> response) {
                        // no-op
                    }

                    @Override
                    public void onFailure(Call<AssignmentResourceResponse> call, Throwable t) {
                        // no-op
                    }
                });
            } else {
                apiService.updateAssignmentResource(token, courseId, assignmentId, row.resourceId, request)
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
    }

    private void addLinkRow(Long resourceId, String title, String url) {
        LayoutInflater inflater = LayoutInflater.from(this);
        android.view.View rowView = inflater.inflate(R.layout.lesson_link_item, linkListContainer, false);
        EditText etLinkTitle = rowView.findViewById(R.id.etLinkTitle);
        EditText etLinkUrl = rowView.findViewById(R.id.etLinkUrl);
        Button btnDelete = rowView.findViewById(R.id.btnDeleteLink);

        etLinkTitle.setText(title != null ? title : "");
        etLinkUrl.setText(url != null ? url : "");

        LinkRow row = new LinkRow(resourceId, rowView, etLinkTitle, etLinkUrl);
        linkRows.add(row);

        btnDelete.setOnClickListener(v -> {
            if (resourceId == null) {
                linkRows.remove(row);
                linkListContainer.removeView(rowView);
                return;
            }
            deleteResource(resourceId, () -> {
                linkRows.remove(row);
                linkListContainer.removeView(rowView);
            });
        });

        linkListContainer.addView(rowView);
    }

    private void addFileRow(AssignmentResourceResponse resource) {
        LayoutInflater inflater = LayoutInflater.from(this);
        android.view.View rowView = inflater.inflate(R.layout.lesson_file_item, fileListContainer, false);
        TextView tvFileName = rowView.findViewById(R.id.tvFileName);
        Button btnDelete = rowView.findViewById(R.id.btnDeleteFile);

        String name = resource.title;
        if (name == null || name.trim().isEmpty() || looksEncodedName(name)) {
            String link = resource.filePath != null ? resource.filePath : resource.url;
            name = extractFileName(link);
        }
        if (name == null || name.trim().isEmpty()) {
            name = "Attachment";
        }
        tvFileName.setText(name);

        Long resourceId = resource.id;
        btnDelete.setOnClickListener(v -> {
            if (resourceId == null) {
                fileListContainer.removeView(rowView);
                return;
            }
            deleteResource(resourceId, () -> fileListContainer.removeView(rowView));
        });

        fileListContainer.addView(rowView);
    }

    private void deleteResource(Long resourceId, Runnable onSuccess) {
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.deleteAssignmentResource(token, courseId, assignmentId, resourceId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() && onSuccess != null) {
                    onSuccess.run();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // no-op
            }
        });
    }

    private void uploadAndAttachFile(ApiService apiService, String token, Uri fileUri) {
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
        Log.d("EditAssignmentTeacher", "Presign upload for " + fileName + " to assignment " + assignmentId);

        Long resolvedClassId = classIdForUpload != null ? classIdForUpload : courseId;
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
                    Log.e("EditAssignmentTeacher", "Presign failed code=" + response.code() + " body=" + errorBody);
                    Toast.makeText(
                            EditAssignmentTeacherActivity.this,
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
                                    Toast.makeText(
                                            EditAssignmentTeacherActivity.this,
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
                                                            EditAssignmentTeacherActivity.this,
                                                            "Attach file failed (" + response.code() + ")",
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                    return;
                                                }
                                                if (tvSelectedFileName != null) {
                                                    tvSelectedFileName.setText("");
                                                    tvSelectedFileName.setVisibility(android.view.View.GONE);
                                                }
                                                loadResources();
                                            }

                                            @Override
                                            public void onFailure(Call<AssignmentResourceResponse> call, Throwable t) {
                                                Toast.makeText(
                                                        EditAssignmentTeacherActivity.this,
                                                        "Attach file failed",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        });
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(
                                        EditAssignmentTeacherActivity.this,
                                        "Upload file failed",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
            }

            @Override
            public void onFailure(Call<PresignUploadResponse> call, Throwable t) {
                Toast.makeText(
                        EditAssignmentTeacherActivity.this,
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

    private String formatDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
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
                // no-op
            }
        }
        return value;
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
        String mimeType = resolveContentType(uri);
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

    private static class LinkRow {
        private final Long resourceId;
        private final android.view.View view;
        private final EditText titleInput;
        private final EditText urlInput;

        private LinkRow(Long resourceId, android.view.View view, EditText titleInput, EditText urlInput) {
            this.resourceId = resourceId;
            this.view = view;
            this.titleInput = titleInput;
            this.urlInput = urlInput;
        }
    }
}
