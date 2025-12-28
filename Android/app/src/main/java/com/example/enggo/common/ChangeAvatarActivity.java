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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeAvatarActivity extends BaseUserActivity {

    private ImageView imgCurrentPicture;
    private LinearLayout dragDropArea;
    private TextView tvDeletePicture;
    private Button btnSaveChanges;
    private Button btnCancel;
    private ActivityResultLauncher<String> filePickerLauncher;
    private Uri selectedImageUri;
    private String currentAvatarUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avatar);
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
                this::handleFilePicked
        );
    }

    private void setupListeners() {
        if (dragDropArea != null) {
            dragDropArea.setOnClickListener(v -> filePickerLauncher.launch("image/*"));
        }
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }
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
        selectedImageUri = uri;
        if (imgCurrentPicture != null) {
            imgCurrentPicture.setImageURI(uri);
        }
    }

    private void uploadAvatar() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }
        long contentLength = resolveContentLength(selectedImageUri);
        if (contentLength <= 0) {
            Toast.makeText(this, "Cannot determine file size", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = getTokenFromDb();
        if (token == null) {
            return;
        }

        String fileName = getFileName(selectedImageUri);
        String contentType = resolveContentType(selectedImageUri);
        RequestBody requestBody = createRequestBody(selectedImageUri, contentLength);

        PresignUploadRequest presignRequest = new PresignUploadRequest(
                "AVATAR",
                fileName,
                contentType,
                null,
                null,
                null
        );

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
                String uploadContentType = presign.contentType != null ? presign.contentType : contentType;
                apiService.uploadToPresignedUrl(presign.uploadUrl, uploadContentType, contentLength, requestBody)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> uploadResponse) {
                                if (!uploadResponse.isSuccessful()) {
                                    Toast.makeText(ChangeAvatarActivity.this,
                                            "Upload failed (" + uploadResponse.code() + ")",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                updateAvatarUrl(token, presign.publicUrl);
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(ChangeAvatarActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onFailure(Call<PresignUploadResponse> call, Throwable t) {
                Toast.makeText(ChangeAvatarActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
                }
            }

            @Override
            public void onFailure(Call<UserAdmin> call, Throwable t) {
                Toast.makeText(ChangeAvatarActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
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
    }

    private String getExtension(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType == null) {
            return null;
        }
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
    }

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
    }
}
