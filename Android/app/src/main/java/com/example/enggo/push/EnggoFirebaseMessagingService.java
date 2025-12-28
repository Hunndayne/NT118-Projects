package com.example.enggo.push;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.enggo.R;
import com.example.enggo.user.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class EnggoFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "EnggoFcmService";
    private static final String CHANNEL_ID = "enggo_default";

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "FCM token refreshed");
        PushTokenManager.registerToken(getApplicationContext(), token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String title = null;
        String body = null;
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }
        Map<String, String> data = remoteMessage.getData();
        if (title == null) {
            title = data.get("title");
        }
        if (body == null) {
            body = data.get("body");
        }
        if (title == null && body == null) {
            return;
        }
        showNotification(title, body, data);
    }

    private void showNotification(String title, String body, Map<String, String> data) {
        createChannelIfNeeded();
        String safeTitle = title == null ? "" : title;
        String safeBody = body == null ? "" : body;
        Intent intent = new Intent(this, MainActivity.class);
        if (data != null && data.containsKey("notificationId")) {
            intent.putExtra("notificationId", data.get("notificationId"));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(safeTitle)
                .setContentText(safeBody)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(safeBody))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(this).notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "EngGo Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("EngGo notifications");
        manager.createNotificationChannel(channel);
    }
}
