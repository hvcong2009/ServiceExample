package com.example.myapplication;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MyApplication extends Application {
    public static String CHANNEL_ID = "channel_service_id";
    public static String CHANNEL_NAME = "channel_service_name";

    @Override
    public void onCreate() {
        super.onCreate();

        createChannelNotification();
    }

    private void createChannelNotification() {
        // Từ Android API 26 thì cần phải sử dụng channel để tạo notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setSound(null, null);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (null != notificationManager) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }
}
