package com.example.enggo.common;

import com.example.enggo.R;
import com.example.enggo.admin.UserAdmin;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.api.PresignUploadRequest;
import com.example.enggo.api.PresignUploadResponse;
import com.example.enggo.api.UserUpdateRequest;
import com.example.enggo.user.BaseUserActivity;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.documentfile.provider.DocumentFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeAvatarActivity extends BaseUserActivity {
    private static final String TAG = "ChangeAvatarActivity";

    private ImageView imgCurrentPicture;
    private LinearLayout dragDropArea;
    private TextView tvDeletePicture;
    private Button btnSaveChanges;
    private Button btnCancel;
    private ActivityResultLauncher<String[]> filePickerLauncher;

    private Uri selectedFileUri;
    private String selectedFileName;
    private long selectedFileLength;
    private String selectedContentType;
    private String currentAvatarUrl;
    private boolean deleteRequested;
    private boolean isSaving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avatar);

        TextView tvBack = findViewById(R.id.tvBack);
        if (tvBack != null) {
            tvBack.setOnClickListener(v -> finish());
        }
        setupHeader();
        setupFooter();

        imgCurrentPicture = findViewById(R.id.imgCurrentPicture);
        dragDropArea = findViewById(R.id.dragDropArea);
        tvDeletePicture = findViewById(R.id.tvDeletePicture);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnCancel = findViewById(R.id.btnCancel);

        setupFilePicker();
        setupListeners();
        loadProfile();
    }

    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::handleFilePicked
        );
    }

    private void setupListeners() {
        if (dragDropArea != null) {
            dragDropArea.setOnClickListener(v -> filePickerLauncher.launch(new String[]{"image/*"}));
        }
        if (tvDeletePicture != null) {
            tvDeletePicture.setOnClickListener(v -> {
                deleteRequested = true;
                selectedFileUri = null;
                selectedFileName = null;
                selectedFileLength = 0L;
                selectedContentType = null;
                if (imgCurrentPicture != null) {
                    imgCurrentPicture.setImageDrawable(null);
                }
            });
        }
        if (btnSaveChanges != null) {
            btnSaveChanges.setOnClickListener(v -> saveChanges());
        }
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }
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
        } catch (SecurityException ignored) {
        }
        selectedFileUri = uri;
        selectedFileName = getFileName(uri);
        selectedFileLength = resolveContentLength(uri);
        selectedContentType = resolveContentType(uri);
        deleteRequested = false;
        Log.d(TAG, "Selected file name=" + selectedFileName + " size=" + selectedFileLength + " type=" + selectedContentType);

        if (imgCurrentPicture != null) {
            imgCurrentPicture.setImageURI(uri);
        }
    }

    private void loadProfile() {
        String token = getTokenFromDb();
        if (token == null) {
            Log.w(TAG, "Missing token, cannot load profile");
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getCurrentUser(token).enqueue(new Callback<UserAdmin>() {
            @Override
            public void onResponse(Call<UserAdmin> call, Response<UserAdmin> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.w(TAG, "Load profile failed code=" + response.code());
                    return;
                }
                currentAvatarUrl = response.body().getAvatarUrl();
                if (currentAvatarUrl != null && !currentAvatarUrl.trim().isEmpty()) {
                    loadImageFromUrl(currentAvatarUrl);
                }
            }

            @Override
            public void onFailure(Call<UserAdmin> call, Throwable t) {
                Log.w(TAG, "Load profile failed", t);
                // no-op
            }
        });
    }

    private void saveChanges() {
        if (isSaving) {
            return;
        }
        if (selectedFileUri == null && !deleteRequested) {
            Toast.makeText(this, "No changes to save", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = getTokenFromDb();
        if (token == null) {
            Log.w(TAG, "Missing token, cannot update avatar");
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        setSaving(true);
        if (deleteRequested) {
            updateAvatarUrl(apiService, token, "");
            return;
        }
        uploadAvatar(apiService, token, selectedFileUri);
    }

    private void uploadAvatar(ApiService apiService, String token, Uri fileUri) {
        long contentLength = selectedFileLength > 0 ? selectedFileLength : resolveContentLength(fileUri);
        if (contentLength <= 0) {
            setSaving(false);
            Toast.makeText(this, "Cannot determine file size", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestBody requestBody = createRequestBody(fileUri, contentLength);
        if (requestBody == null) {
            setSaving(false);
            Toast.makeText(this, "Cannot read file", Toast.LENGTH_SHORT).show();
            return;
        }
        String contentType = selectedContentType != null ? selectedContentType : resolveContentType(fileUri);
        String fileName = sanitizeFileName(selectedFileName, fileUri);

        PresignUploadRequest presignRequest = new PresignUploadRequest(
                "AVATAR",
                fileName,
                contentType,
                null,
                null,
                null
        );
        apiService.presignUpload(token, presignRequest).enqueue(new Callback<PresignUploadResponse>() {
            @Override
            public void onResponse(Call<PresignUploadResponse> call, Response<PresignUploadResponse> response) {
                PresignUploadResponse presign = response.body();
                Log.d(TAG, "Presign response code=" + response.code());
                if (!response.isSuccessful() || presign == null || presign.uploadUrl == null || presign.publicUrl == null) {
                    setSaving(false);
                    Toast.makeText(ChangeAvatarActivity.this, "Get upload URL failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "Presign success publicUrl=" + presign.publicUrl);
                String uploadContentType = presign.contentType != null ? presign.contentType : contentType;
                apiService.uploadToPresignedUrl(presign.uploadUrl, uploadContentType, contentLength, requestBody)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> uploadResponse) {
                                Log.d(TAG, "Upload response code=" + uploadResponse.code());
                                if (!uploadResponse.isSuccessful()) {
                                    setSaving(false);
                                    Toast.makeText(ChangeAvatarActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                updateAvatarUrl(apiService, token, presign.publicUrl);
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Log.w(TAG, "Upload failed", t);
                                setSaving(false);
                                Toast.makeText(ChangeAvatarActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onFailure(Call<PresignUploadResponse> call, Throwable t) {
                Log.w(TAG, "Presign failed", t);
                setSaving(false);
                Toast.makeText(ChangeAvatarActivity.this, "Get upload URL failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAvatarUrl(ApiService apiService, String token, String avatarUrl) {
        UserUpdateRequest request = new UserUpdateRequest();
        request.avatarUrl = avatarUrl;
        apiService.updateCurrentUser(token, request).enqueue(new Callback<UserAdmin>() {
            @Override
            public void onResponse(Call<UserAdmin> call, Response<UserAdmin> response) {
                setSaving(false);
                Log.d(TAG, "Update profile response code=" + response.code());
                if (!response.isSuccessful()) {
                    String detail = readErrorBody(response.errorBody());
                    Log.w(TAG, "Update failed body=" + detail);
                    String message = "Update failed (" + response.code() + ")";
                    if (detail != null && !detail.isEmpty()) {
                        message = message + ": " + detail;
                    }
                    Toast.makeText(ChangeAvatarActivity.this, message, Toast.LENGTH_SHORT).show();
                    return;
                }
                String updatedAvatar = avatarUrl;
                if (response.body() != null && response.body().getAvatarUrl() != null) {
                    updatedAvatar = response.body().getAvatarUrl();
                }
                if (updatedAvatar == null || updatedAvatar.trim().isEmpty()) {
                    if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                        updatedAvatar = avatarUrl;
                    }
                }
                currentAvatarUrl = (updatedAvatar == null || updatedAvatar.trim().isEmpty()) ? null : updatedAvatar;
                selectedFileUri = null;
                selectedFileName = null;
                selectedFileLength = 0L;
                selectedContentType = null;
                deleteRequested = false;

                if (currentAvatarUrl == null) {
                    if (imgCurrentPicture != null) {
                        imgCurrentPicture.setImageDrawable(null);
                    }
                    Toast.makeText(ChangeAvatarActivity.this, "Avatar removed", Toast.LENGTH_SHORT).show();
                } else {
                    loadImageFromUrl(currentAvatarUrl);
                    Toast.makeText(ChangeAvatarActivity.this, "Avatar updated", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserAdmin> call, Throwable t) {
                Log.w(TAG, "Update failed", t);
                setSaving(false);
                Toast.makeText(ChangeAvatarActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadImageFromUrl(String url) {
        if (imgCurrentPicture == null) {
            return;
        }
        new Thread(() -> {
            try (InputStream inputStream = new URL(url).openStream()) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap != null) {
                    runOnUiThread(() -> imgCurrentPicture.setImageBitmap(bitmap));
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void setSaving(boolean saving) {
        isSaving = saving;
        if (btnSaveChanges != null) {
            btnSaveChanges.setEnabled(!saving);
        }
        if (btnCancel != null) {
            btnCancel.setEnabled(!saving);
        }
        if (dragDropArea != null) {
            dragDropArea.setEnabled(!saving);
            dragDropArea.setClickable(!saving);
            dragDropArea.setAlpha(saving ? 0.5f : 1f);
        }
        if (tvDeletePicture != null) {
            tvDeletePicture.setEnabled(!saving);
        }
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

    private String sanitizeFileName(String name, Uri uri) {
        String safe = name == null ? "" : name.trim();
        if (safe.isEmpty()) {
            String extension = getExtension(uri);
            safe = extension != null ? "avatar." + extension : "avatar";
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
            name = extension != null ? "avatar." + extension : "avatar";
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

    private String readErrorBody(ResponseBody errorBody) {
        if (errorBody == null) {
            return null;
        }
        try {
            String value = errorBody.string();
            return value == null ? null : value.trim();
        } catch (IOException ignored) {
            return null;
        }
    }
}
