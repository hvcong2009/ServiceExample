package com.example.myapplication;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class CustomService extends Service{
    public static String KEY = "Custom_Service";
    private static String TAG = "CustomService";

    private int INTENT_REQUEST_CODE = 0;
    private int FOREGROUND_SERVICE_ID = 1;

    private MediaPlayer mMediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "CustomService.onBind");
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "CustomService.onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "CustomService.onStartCommand");
        // Get string data
//        String dataIntent = intent.getStringExtra(KEY);
//        sendStringNotification(dataIntent);

        // Get object data
        Bundle bundle = intent.getExtras();
        if (null != bundle) {
            Song song = (Song) bundle.get("song_key");
            if (null != song) {
                startMusic(song);
                sendSongNotification(song);
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "CustomService.onDestroy");
        super.onDestroy();

        // Release Data
        if (null != mMediaPlayer) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    // Foreground service cần có 1 Notification cho User thấy để thao tác
    // region Function get string data
    private void sendStringNotification(String data) {
        // Sử dụng pending intent để khi user click vào Notification sẽ open Activity
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(this, INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }else {
            pendingIntent = PendingIntent.getActivity(this, INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        Notification notification = new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                .setContentTitle("Title Notification Service")
                .setContentText(data)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .build();

        // Start foreground service
        startForeground(FOREGROUND_SERVICE_ID, notification);
    }
    // endregion

    // region Function get object data
    private void sendSongNotification(Song song) {
        // Sử dụng pending intent để khi user click vào Notification sẽ open Activity
        Intent intent = new Intent(this, SecondActivity.class);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(this, INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }else {
            pendingIntent = PendingIntent.getActivity(this, INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        // Để tương tác với custom notification thì cần phải sử dụng RemoteViews
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_custom_notification);
        remoteViews.setTextViewText(R.id.txt_songTitle, song.getTitle());
        remoteViews.setTextViewText(R.id.txt_songContent, song.getSingle());

        // TODO- Test thử việc nếu thay setImageViewBitmap thành setImageViewResource sẽ được không
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), song.getImage());
        remoteViews.setImageViewBitmap(R.id.img_song, bitmap);
        remoteViews.setImageViewResource(R.id.img_pauseOrContinueSong, R.drawable.img_pause_button);

        Notification notification = new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setCustomContentView(remoteViews)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(null)
                .build();

        // Start foreground service
        startForeground(FOREGROUND_SERVICE_ID, notification);
    }
    // endregion

    // region Start music
    private void startMusic(Song song) {
        if (null == mMediaPlayer) {
            mMediaPlayer = MediaPlayer.create(getApplicationContext(), song.getResource());
        }
        mMediaPlayer.start();
    }
    // endregion
}
