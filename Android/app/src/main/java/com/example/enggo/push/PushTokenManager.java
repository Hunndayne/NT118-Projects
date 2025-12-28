package com.example.enggo.push;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.api.DeviceTokenRequest;
import com.example.enggo.database.Database;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class PushTokenManager {

    private static final String TAG = "PushTokenManager";

    private PushTokenManager() {
    }

    public static void syncToken(Context context) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "FCM token fetch failed", task.getException());
                        return;
                    }
                    registerToken(context, task.getResult());
                });
    }

    public static void registerToken(Context context, String fcmToken) {
        if (fcmToken == null || fcmToken.trim().isEmpty()) {
            return;
        }
        Database.Dao dao = new Database.Dao(context);
        List<Database.Item> items = dao.getAll();
        if (items == null || items.isEmpty()) {
            return;
        }
        String authToken = items.get(0).token;
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        DeviceTokenRequest request = new DeviceTokenRequest(fcmToken, "ANDROID", deviceId);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.registerDeviceToken(authToken, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Log.w(TAG, "Register token failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.w(TAG, "Register token error", t);
            }
        });
    }
}
