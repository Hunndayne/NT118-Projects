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
<<<<<<< HEAD
=======
import android.util.Log;
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6
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
<<<<<<< HEAD
import java.net.HttpURLConnection;
=======
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6
import java.net.URL;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
<<<<<<< HEAD
=======
import okhttp3.ResponseBody;
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeAvatarActivity extends BaseUserActivity {
<<<<<<< HEAD
=======
    private static final String TAG = "ChangeAvatarActivity";
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6

    private ImageView imgCurrentPicture;
    private LinearLayout dragDropArea;
    private TextView tvDeletePicture;
    private Button btnSaveChanges;
    private Button btnCancel;
<<<<<<< HEAD
    private ActivityResultLauncher<String> filePickerLauncher;
    private Uri selectedImageUri;
    private String currentAvatarUrl;
=======
    private ActivityResultLauncher<String[]> filePickerLauncher;

    private Uri selectedFileUri;
    private String selectedFileName;
    private long selectedFileLength;
    private String selectedContentType;
    private String currentAvatarUrl;
    private boolean deleteRequested;
    private boolean isSaving;
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avatar);
<<<<<<< HEAD
        setupHeader();
        setupFooter();

        initViews();
        setupFilePicker();
        setupListeners();
        loadCurrentAvatar();
    }

    private void initViews() {
        TextView tvBack = findViewById(R.id.tvBack);
        if (tvBack != null) {
            tvBack.setOnClickListener(v -> finish());
        }
        imgCurrentPicture = findViewById(R.id.imgCurrentPicture);
        dragDropArea = findViewById(R.id.dragDropArea);
        tvDeletePicture = findViewById(R.id.tvDeletePicture);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
=======

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
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6
                this::handleFilePicked
        );
    }

    private void setupListeners() {
        if (dragDropArea != null) {
<<<<<<< HEAD
            dragDropArea.setOnClickListener(v -> filePickerLauncher.launch("image/*"));
=======
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
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6
        }
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }
<<<<<<< HEAD
        if (btnSaveChanges != null) {
            btnSaveChanges.setOnClickListener(v -> uploadAvatar());
        }
        if (tvDeletePicture != null) {
            tvDeletePicture.setOnClickListener(v -> deleteAvatar());
        }
    }

    private void loadCurrentAvatar() {
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getCurrentUser(token).enqueue(new Callback<UserAdmin>() {
            @Override
            public void onResponse(Call<UserAdmin> call, Response<UserAdmin> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }
                currentAvatarUrl = response.body().getAvatarUrl();
                if (currentAvatarUrl != null && !currentAvatarUrl.trim().isEmpty()) {
                    loadImageFromUrl(currentAvatarUrl, imgCurrentPicture);
                }
            }

            @Override
            public void onFailure(Call<UserAdmin> call, Throwable t) {
                // no-op
            }
        });
=======
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6
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
<<<<<<< HEAD
        } catch (Exception ignored) {
            // best effort
        }
        selectedImageUri = uri;
=======
        } catch (SecurityException ignored) {
        }
        selectedFileUri = uri;
        selectedFileName = getFileName(uri);
        selectedFileLength = resolveContentLength(uri);
        selectedContentType = resolveContentType(uri);
        deleteRequested = false;
        Log.d(TAG, "Selected file name=" + selectedFileName + " size=" + selectedFileLength + " type=" + selectedContentType);

>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6
        if (imgCurrentPicture != null) {
            imgCurrentPicture.setImageURI(uri);
        }
    }

<<<<<<< HEAD
    private void uploadAvatar() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }
        long contentLength = resolveContentLength(selectedImageUri);
        if (contentLength <= 0) {
            Toast.makeText(this, "Cannot determine file size", Toast.LENGTH_SHORT).show();
=======
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
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6
            return;
        }
        String token = getTokenFromDb();
        if (token == null) {
<<<<<<< HEAD
            return;
        }

        String fileName = getFileName(selectedImageUri);
        String contentType = resolveContentType(selectedImageUri);
        RequestBody requestBody = createRequestBody(selectedImageUri, contentLength);
=======
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
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6

        PresignUploadRequest presignRequest = new PresignUploadRequest(
                "AVATAR",
                fileName,
                contentType,
                null,
                null,
                null
        );
<<<<<<< HEAD

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.presignUpload(token, presignRequest).enqueue(new Callback<PresignUploadResponse>() {
            @Override
            public void onResponse(Call<PresignUploadResponse> call, Response<PresignUploadResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().uploadUrl == null) {
                    Toast.makeText(ChangeAvatarActivity.this,
                            "Upload failed (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                PresignUploadResponse presign = response.body();
=======
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
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6
                String uploadContentType = presign.contentType != null ? presign.contentType : contentType;
                apiService.uploadToPresignedUrl(presign.uploadUrl, uploadContentType, contentLength, requestBody)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> uploadResponse) {
<<<<<<< HEAD
                                if (!uploadResponse.isSuccessful()) {
                                    Toast.makeText(ChangeAvatarActivity.this,
                                            "Upload failed (" + uploadResponse.code() + ")",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                updateAvatarUrl(token, presign.publicUrl);
=======
                                Log.d(TAG, "Upload response code=" + uploadResponse.code());
                                if (!uploadResponse.isSuccessful()) {
                                    setSaving(false);
                                    Toast.makeText(ChangeAvatarActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                updateAvatarUrl(apiService, token, presign.publicUrl);
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
<<<<<<< HEAD
=======
                                Log.w(TAG, "Upload failed", t);
                                setSaving(false);
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6
                                Toast.makeText(ChangeAvatarActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onFailure(Call<PresignUploadResponse> call, Throwable t) {
<<<<<<< HEAD
                Toast.makeText(ChangeAvatarActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
=======
                Log.w(TAG, "Presign failed", t);
                setSaving(false);
                Toast.makeText(ChangeAvatarActivity.this, "Get upload URL failed", Toast.LENGTH_SHORT).show();
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6
            }
        });
    }

<<<<<<< HEAD
    private void updateAvatarUrl(String token, String avatarUrl) {
        UserUpdateRequest request = new UserUpdateRequest();
        request.avatarUrl = avatarUrl;
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.updateCurrentUser(token, request).enqueue(new Callback<UserAdmin>() {
            @Override
            public void onResponse(Call<UserAdmin> call, Response<UserAdmin> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChangeAvatarActivity.this, "Avatar updated", Toast.LENGTH_SHORT).show();
                    currentAvatarUrl = avatarUrl;
                } else {
                    Toast.makeText(ChangeAvatarActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserAdmin> call, Throwable t) {
                Toast.makeText(ChangeAvatarActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAvatar() {
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }
        UserUpdateRequest request = new UserUpdateRequest();
        request.avatarUrl = null;
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.updateCurrentUser(token, request).enqueue(new Callback<UserAdmin>() {
            @Override
            public void onResponse(Call<UserAdmin> call, Response<UserAdmin> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChangeAvatarActivity.this, "Avatar removed", Toast.LENGTH_SHORT).show();
                    currentAvatarUrl = null;
                    if (imgCurrentPicture != null) {
                        imgCurrentPicture.setImageDrawable(null);
                    }
                } else {
                    Toast.makeText(ChangeAvatarActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
=======
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
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6
                }
            }

            @Override
            public void onFailure(Call<UserAdmin> call, Throwable t) {
<<<<<<< HEAD
=======
                Log.w(TAG, "Update failed", t);
                setSaving(false);
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6
                Toast.makeText(ChangeAvatarActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

<<<<<<< HEAD
=======
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

>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6
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

<<<<<<< HEAD
    private String getFileName(Uri uri) {
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        String name = cursor.getString(index);
                        if (name != null && !name.trim().isEmpty()) {
                            return name;
                        }
                    }
                }
            } catch (Exception ignored) {
                // best effort
            }
        }
        String extension = getExtension(uri);
        return extension != null ? "avatar." + extension : "avatar";
=======
    private String sanitizeFileName(String name, Uri uri) {
        String safe = name == null ? "" : name.trim();
        if (safe.isEmpty()) {
            String extension = getExtension(uri);
            safe = extension != null ? "avatar." + extension : "avatar";
        }
        return safe;
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6
    }

    private String getExtension(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType == null) {
            return null;
        }
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
    }

<<<<<<< HEAD
    private void loadImageFromUrl(String url, ImageView imageView) {
        if (url == null || url.trim().isEmpty() || imageView == null) {
            return;
        }
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL imageUrl = new URL(url);
                connection = (HttpURLConnection) imageUrl.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();
                try (InputStream inputStream = connection.getInputStream()) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    runOnUiThread(() -> {
                        imageView.setImageTintList(null);
                        imageView.clearColorFilter();
                        imageView.setImageBitmap(bitmap);
                    });
                }
            } catch (Exception ignored) {
                // no-op
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
=======
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
>>>>>>> 2db38f09a53e3625f675503d27ffeadc2ae73dd6
    }
}
