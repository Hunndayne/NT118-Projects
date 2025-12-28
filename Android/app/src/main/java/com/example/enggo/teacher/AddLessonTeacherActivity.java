package com.example.enggo.teacher;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddLessonTeacherActivity extends BaseTeacherActivity {
    private EditText etLessonName;
    private EditText etVideoLink;
    private EditText etPracticeLink;
    private Button btnCancel;
    private Button btnCreate;
    private Button btnUpload;
    private TextView tvSelectedFileName;
    private Long courseId;
    private ActivityResultLauncher<String[]> filePickerLauncher;
    private String selectedFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_lessons_teacher);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        setupFilePicker();
        setupListeners();

        courseId = getIntent().getLongExtra(ManageLessonsTeacherActivity.EXTRA_COURSE_ID, -1);
    }

    private void initViews() {
        etLessonName = findViewById(R.id.etLessonTitle_Admin);
        etVideoLink = findViewById(R.id.etAddLessonContent_Admin);
        etPracticeLink = findViewById(R.id.etUpAttachLinkContent_Admim);
        btnCancel = findViewById(R.id.buttonCancelUpLesson_Admin);
        btnCreate = findViewById(R.id.buttonUpLesson_Admin);
        btnUpload = findViewById(R.id.buttonUploadAttachmentAdmin);
        tvSelectedFileName = findViewById(R.id.tvSelectedFileName);
    }

    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::handleFilePicked
        );
    }

    private void setupListeners() {
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (btnUpload != null) {
            btnUpload.setOnClickListener(v -> filePickerLauncher.launch(new String[]{"*/*"}));
        }

        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> {
                createLesson();
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
        Log.d("AddLessonTeacher", "Selected file: " + selectedFileUri + " name=" + fileName);
    }

    private void createLesson() {
        if (courseId == null || courseId == -1) {
            Toast.makeText(this, "Missing course id", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etLessonName.getText().toString().trim();
        String content = etVideoLink.getText().toString().trim();
        String link = etPracticeLink.getText().toString().trim();
        String description = content;

        if (title.isEmpty()) {
            Toast.makeText(this, "Lesson title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = getTokenFromDb();
        Log.d("AddLesson", "Creating lesson for courseId: " + courseId + ", title: " + title);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        LessonCreateRequest request = new LessonCreateRequest(title, description, null);

        apiService.createLesson(token, courseId, request)
                .enqueue(new Callback<LessonResponse>() {
                    @Override
                    public void onResponse(Call<LessonResponse> call, Response<LessonResponse> response) {
                        Log.d("AddLesson", "Response code: " + response.code());
                        if (response.isSuccessful()) {
                            LessonResponse lesson = response.body();
                            if (lesson != null) {
                                Log.d("AddLesson", "Lesson created with id: " + lesson.id);
                                attachResources(token, lesson.id, link);
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
                            Log.e("AddLesson", "Create failed: " + response.code() + " - " + errorBody);
                            Toast.makeText(
                                    AddLessonTeacherActivity.this,
                                    "Create lesson failed: " + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LessonResponse> call, Throwable t) {
                        Log.e("AddLesson", "Network error: " + t.getMessage(), t);
                        Toast.makeText(
                                AddLessonTeacherActivity.this,
                                "Cannot connect to server: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void attachResources(String token, Long lessonId, String link) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        if (selectedFileUri != null && !selectedFileUri.trim().isEmpty()) {
            uploadAndAttachFile(apiService, token, lessonId, Uri.parse(selectedFileUri));
        }

        if (link != null && !link.trim().isEmpty()) {
            LessonResourceRequest linkRequest = new LessonResourceRequest(
                    "LINK",
                    "Attachment Link",
                    null,
                    link,
                    null
            );
            apiService.addLessonResource(token, courseId, lessonId, linkRequest).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // no-op
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // no-op
                }
            });
        }
    }

    private void uploadAndAttachFile(ApiService apiService, String token, Long lessonId, Uri fileUri) {
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
        Log.d("AddLessonTeacher", "Presign upload for " + fileName + " to lesson " + lessonId);

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
                    Log.e("AddLessonTeacher", "Presign failed code=" + response.code());
                    Toast.makeText(
                            AddLessonTeacherActivity.this,
                            "Upload file failed (" + response.code() + ")",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                PresignUploadResponse presign = response.body();
                String uploadContentType = presign.contentType != null ? presign.contentType : contentType;
                Log.d("AddLessonTeacher", "Uploading to presigned url");

                apiService.uploadToPresignedUrl(presign.uploadUrl, uploadContentType, contentLength, requestBody)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> uploadResponse) {
                                if (!uploadResponse.isSuccessful()) {
                                    Log.e("AddLessonTeacher", "Upload failed code=" + uploadResponse.code());
                                    Toast.makeText(
                                            AddLessonTeacherActivity.this,
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
                                                    Log.e("AddLessonTeacher", "Attach failed code=" + response.code());
                                                    Toast.makeText(
                                                            AddLessonTeacherActivity.this,
                                                            "Attach file failed (" + response.code() + ")",
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<Void> call, Throwable t) {
                                                Log.e("AddLessonTeacher", "Attach failed", t);
                                                Toast.makeText(
                                                        AddLessonTeacherActivity.this,
                                                        "Attach file failed",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        });
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Log.e("AddLessonTeacher", "Upload failed", t);
                                Toast.makeText(
                                        AddLessonTeacherActivity.this,
                                        "Upload file failed",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
            }

            @Override
            public void onFailure(Call<PresignUploadResponse> call, Throwable t) {
                Log.e("AddLessonTeacher", "Presign failed", t);
                Toast.makeText(
                        AddLessonTeacherActivity.this,
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
