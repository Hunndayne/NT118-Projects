package com.example.enggo.teacher;

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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.documentfile.provider.DocumentFile;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.api.PresignUploadRequest;
import com.example.enggo.api.PresignUploadResponse;
import com.example.enggo.teacher.LessonResourceResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditLessonTeacherActivity extends BaseTeacherActivity {
    private EditText etLessonName;
    private EditText etVideoLink;
    private Button btnCancel;
    private Button btnSave;
    private Button btnUpload;
    private Button btnAddLink;
    private LinearLayout fileListContainer;
    private LinearLayout linkListContainer;
    private TextView tvSelectedFileName;
    private Long courseId;
    private Long lessonId;
    private ActivityResultLauncher<String[]> filePickerLauncher;
    private String selectedFileUri;
    private final List<LinkRow> linkRows = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_lessons_teacher);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        setupFilePicker();
        loadLessonData();
        setupListeners();
    }

    private void initViews() {
        etLessonName = findViewById(R.id.etLessonTitle);
        etVideoLink = findViewById(R.id.etLessonContent);
        btnCancel = findViewById(R.id.buttonCancelLesson);
        btnSave = findViewById(R.id.buttonSaveLesson);
        btnUpload = findViewById(R.id.buttonAttachment);
        btnAddLink = findViewById(R.id.btnAddLink);
        fileListContainer = findViewById(R.id.fileListContainer);
        linkListContainer = findViewById(R.id.linkListContainer);
        tvSelectedFileName = findViewById(R.id.tvSelectedFileName);
    }

    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::handleFilePicked
        );
    }

    private void loadLessonData() {
        // Load lesson data from intent
        if (getIntent() != null) {
            String lessonName = getIntent().getStringExtra("lesson_name");
            String description = getIntent().getStringExtra("lesson_description");
            courseId = getIntent().getLongExtra(ManageLessonsTeacherActivity.EXTRA_COURSE_ID, -1);
            lessonId = getIntent().getLongExtra("lesson_id", -1);
            
            if (lessonName != null && etLessonName != null) {
                etLessonName.setText(lessonName);
            }
            if (description != null && etVideoLink != null) {
                etVideoLink.setText(description);
            }
        }
        loadResources();
    }

    private void setupListeners() {
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (btnUpload != null) {
            btnUpload.setOnClickListener(v -> filePickerLauncher.launch(new String[]{"*/*"}));
        }

        if (btnAddLink != null) {
            btnAddLink.setOnClickListener(v -> addLinkRow(null, "", ""));
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                updateLesson();
            });
        }
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
            // Best effort; some providers do not allow persistable permission.
        }
        selectedFileUri = uri.toString();
        String fileName = getFileName(uri);
        if (tvSelectedFileName != null) {
            tvSelectedFileName.setText(fileName);
            tvSelectedFileName.setVisibility(android.view.View.VISIBLE);
        }
        Log.d("EditLessonTeacher", "Selected file: " + selectedFileUri + " name=" + fileName);
        String token = getTokenFromDb();
        if (token == null) {
            Toast.makeText(this, "Missing token", Toast.LENGTH_SHORT).show();
            return;
        }
        if (courseId == null || courseId == -1 || lessonId == null || lessonId == -1) {
            Toast.makeText(this, "Missing lesson info", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        uploadAndAttachFile(apiService, token, uri);
    }

    private void updateLesson() {
        if (courseId == null || courseId == -1 || lessonId == null || lessonId == -1) {
            Toast.makeText(this, "Missing lesson info", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etLessonName.getText().toString().trim();
        String content = etVideoLink.getText().toString().trim();
        String description = content;

        if (title.isEmpty()) {
            Toast.makeText(this, "Lesson title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        LessonUpdateRequest request = new LessonUpdateRequest(title, description, null);

        apiService.updateLesson(token, courseId, lessonId, request)
                .enqueue(new Callback<LessonResponse>() {
                    @Override
                    public void onResponse(Call<LessonResponse> call, Response<LessonResponse> response) {
                        if (response.isSuccessful()) {
                            saveLinkResources(token);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(
                                    EditLessonTeacherActivity.this,
                                    "Update lesson failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LessonResponse> call, Throwable t) {
                        Toast.makeText(
                                EditLessonTeacherActivity.this,
                                "Cannot connect to server",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void loadResources() {
        if (courseId == null || courseId == -1 || lessonId == null || lessonId == -1) {
            return;
        }
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getLessonResources(token, courseId, lessonId).enqueue(new Callback<List<LessonResourceResponse>>() {
            @Override
            public void onResponse(Call<List<LessonResourceResponse>> call, Response<List<LessonResourceResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }
                linkRows.clear();
                linkListContainer.removeAllViews();
                fileListContainer.removeAllViews();
                for (LessonResourceResponse resource : response.body()) {
                    String type = resource.type != null ? resource.type.trim() : "";
                    if ("LINK".equalsIgnoreCase(type)) {
                        addLinkRow(resource.id, resource.title, resource.url);
                    } else if ("FILE".equalsIgnoreCase(type)) {
                        addFileRow(resource);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<LessonResourceResponse>> call, Throwable t) {
                // no-op
            }
        });
    }

    private void addLinkRow(Long resourceId, String title, String url) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View rowView = inflater.inflate(R.layout.lesson_link_item, linkListContainer, false);
        EditText etTitle = rowView.findViewById(R.id.etLinkTitle);
        EditText etUrl = rowView.findViewById(R.id.etLinkUrl);
        Button btnDelete = rowView.findViewById(R.id.btnDeleteLink);

        etTitle.setText(title != null ? title : "");
        etUrl.setText(url != null ? url : "");

        LinkRow row = new LinkRow(resourceId, rowView, etTitle, etUrl);
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

    private void addFileRow(LessonResourceResponse resource) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View rowView = inflater.inflate(R.layout.lesson_file_item, fileListContainer, false);
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
            LessonResourceRequest request = new LessonResourceRequest(
                    "LINK",
                    finalTitle,
                    null,
                    url,
                    null
            );

            if (row.resourceId == null) {
                apiService.addLessonResource(token, courseId, lessonId, request).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        // no-op
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        // no-op
                    }
                });
            } else {
                apiService.updateLessonResource(token, courseId, lessonId, row.resourceId, request)
                        .enqueue(new Callback<LessonResourceResponse>() {
                            @Override
                            public void onResponse(Call<LessonResourceResponse> call, Response<LessonResourceResponse> response) {
                                // no-op
                            }

                            @Override
                            public void onFailure(Call<LessonResourceResponse> call, Throwable t) {
                                // no-op
                            }
                        });
            }
        }
    }

    private void deleteResource(Long resourceId, Runnable onSuccess) {
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.deleteLessonResource(token, courseId, lessonId, resourceId).enqueue(new Callback<Void>() {
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
        Log.d("EditLessonTeacher", "Presign upload for " + fileName + " to lesson " + lessonId);

        PresignUploadRequest presignRequest = new PresignUploadRequest(
                "LESSON_RESOURCE",
                fileName,
                contentType,
                courseId,
                lessonId,
                null
        );

        apiService.presignUpload(token, presignRequest).enqueue(new Callback<PresignUploadResponse>() {
            @Override
            public void onResponse(Call<PresignUploadResponse> call, Response<PresignUploadResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().uploadUrl == null) {
                    Log.e("EditLessonTeacher", "Presign failed code=" + response.code());
                    Toast.makeText(
                            EditLessonTeacherActivity.this,
                            "Upload file failed (" + response.code() + ")",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                PresignUploadResponse presign = response.body();
                String uploadContentType = presign.contentType != null ? presign.contentType : contentType;
                Log.d("EditLessonTeacher", "Uploading to presigned url");

                apiService.uploadToPresignedUrl(presign.uploadUrl, uploadContentType, contentLength, requestBody)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> uploadResponse) {
                                if (!uploadResponse.isSuccessful()) {
                                    Log.e("EditLessonTeacher", "Upload failed code=" + uploadResponse.code());
                                    Toast.makeText(
                                            EditLessonTeacherActivity.this,
                                            "Upload file failed (" + uploadResponse.code() + ")",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return;
                                }

                                String title = fileName;
                                LessonResourceRequest fileRequest = new LessonResourceRequest(
                                        "FILE",
                                        title,
                                        null,
                                        null,
                                        presign.publicUrl
                                );
                                apiService.addLessonResource(token, courseId, lessonId, fileRequest)
                                        .enqueue(new Callback<Void>() {
                                            @Override
                                            public void onResponse(Call<Void> call, Response<Void> response) {
                                                if (!response.isSuccessful()) {
                                                    Log.e("EditLessonTeacher", "Attach failed code=" + response.code());
                                                    Toast.makeText(
                                                            EditLessonTeacherActivity.this,
                                                            "Attach file failed (" + response.code() + ")",
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                    return;
                                                }
                                                Log.d("EditLessonTeacher", "Attach success");
                                                if (tvSelectedFileName != null) {
                                                    tvSelectedFileName.setText("");
                                                    tvSelectedFileName.setVisibility(android.view.View.GONE);
                                                }
                                                loadResources();
                                            }

                                            @Override
                                            public void onFailure(Call<Void> call, Throwable t) {
                                                Log.e("EditLessonTeacher", "Attach failed", t);
                                                Toast.makeText(
                                                        EditLessonTeacherActivity.this,
                                                        "Attach file failed",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        });
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Log.e("EditLessonTeacher", "Upload failed", t);
                                Toast.makeText(
                                        EditLessonTeacherActivity.this,
                                        "Upload file failed",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
            }

            @Override
            public void onFailure(Call<PresignUploadResponse> call, Throwable t) {
                Log.e("EditLessonTeacher", "Presign failed", t);
                Toast.makeText(
                        EditLessonTeacherActivity.this,
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

    private static class LinkRow {
        private final Long resourceId;
        private final View view;
        private final EditText titleInput;
        private final EditText urlInput;

        private LinkRow(Long resourceId, View view, EditText titleInput, EditText urlInput) {
            this.resourceId = resourceId;
            this.view = view;
            this.titleInput = titleInput;
            this.urlInput = urlInput;
        }
    }
}
