package com.example.myapplication;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.RemoteViews;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class CustomForegroundService extends Service{
    public static String SONG_KEY = "Song_Key";

    public static final int ACTION_START = 100;
    public static final int ACTION_PAUSE = 200;
    public static final int ACTION_RESUME = 300;
    public static final int ACTION_STOP = 400;

    private final int INTENT_REQUEST_CODE = 0;
    private int FOREGROUND_SERVICE_ID = 1;

    public static final String NOTIFICATION_SEND_ACTION_TO_FOREGROUND_SERVICE_KEY = "notification_action_to_service_key";
    public static final String FOREGROUND_SERVICE_SEND_ACTION_TO_ACTIVITY_KEY = "service_action_to_activity_key";
    public static final String FOREGROUND_SERVICE_SEND_OBJECT_SONG_TO_ACTIVITY_KEY = "service_send_object_song_key";
    public static final String FOREGROUND_SERVICE_SEND_MEDIA_PLAYER_STATUS_TO_ACTIVITY_KEY = "service_send_media_player_key";
    public static final String FOREGROUND_SERVICE_SEND_MUSIC_ACTION_TO_ACTIVITY_KEY = "service_send_music_action_key";

    public static final String FOREGROUND_SERVICE_RECEIVER_MUSIC_ACTION_KEY = "music_action_key";

    private MediaPlayer mMediaPlayer;

    private Song mCurrentSong;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // region Data from MainActivity
        // Get string data
//        String dataIntent = intent.getStringExtra(KEY);
//        sendStringNotification(dataIntent);

        // Get object data
        Bundle bundle = intent.getExtras();
        if (null != bundle && null != bundle.get(SONG_KEY)) {
            mCurrentSong = (Song) bundle.get(SONG_KEY);
            startMusic(mCurrentSong);
            sendSongNotification(mCurrentSong);
        }
        // endregion

        // region Data from BroadcastReceiver
        // Get music action
        int musicAction = intent.getIntExtra(FOREGROUND_SERVICE_RECEIVER_MUSIC_ACTION_KEY, 0);
        if (0 != musicAction) {
            handleActionOnNotification(musicAction);
        }
        // endregion

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
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
        // App thông thường sẽ chỉ có 1 Service cụ thể, nên sẽ dùng Explicit Intent để send tường mình hơn, tránh rò rỉ data đến những nơi không cần thiết
        Intent intent = new Intent(this, MainActivity.class);

        // Sử dụng pending intent để khi user click vào Notification sẽ open Activity
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
        // App thông thường sẽ chỉ có 1 Service cụ thể, nên sẽ dùng Explicit Intent để send tường mình hơn, tránh rò rỉ data đến những nơi không cần thiết
        Intent intent = new Intent(this, SecondActivity.class);

        // Sử dụng pending intent để khi user click vào Notification sẽ open Activity
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
        remoteViews.setImageViewBitmap(R.id.img_songImage, bitmap);
        remoteViews.setImageViewResource(R.id.img_pauseOrResumeSong, R.drawable.img_pause_button);
        remoteViews.setImageViewResource(R.id.img_stopSong, R.drawable.img_delete_button);

        // Get action of use on Notification
        if (null != mMediaPlayer && mMediaPlayer.isPlaying()) {
            remoteViews.setOnClickPendingIntent(R.id.img_pauseOrResumeSong, getPendingIntent(this, ACTION_PAUSE));
            remoteViews.setImageViewResource(R.id.img_pauseOrResumeSong, R.drawable.img_pause_button);
        } else {
            remoteViews.setOnClickPendingIntent(R.id.img_pauseOrResumeSong, getPendingIntent(this, ACTION_RESUME));
            remoteViews.setImageViewResource(R.id.img_pauseOrResumeSong, R.drawable.img_resume_button);
        }
        // Click stop
        remoteViews.setOnClickPendingIntent(R.id.img_stopSong, getPendingIntent(this, ACTION_STOP));

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

    // Use BroadcastReceiver to send and receiver data
    private PendingIntent getPendingIntent(Context context, int action) {
        // App thông thường sẽ chỉ có 1 Service cụ thể, nên sẽ dùng Explicit Intent để send tường mình hơn, tránh rò rỉ data đến những nơi không cần thiết
        Intent intent = new Intent(this, ForegroundServiceReceiver.class);
        intent.putExtra(NOTIFICATION_SEND_ACTION_TO_FOREGROUND_SERVICE_KEY, action);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), action, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }else {
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), action, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return pendingIntent;
    }

    // region Music action
    private void startMusic(Song song) {
        if (null == mMediaPlayer) {
            mMediaPlayer = MediaPlayer.create(getApplicationContext(), song.getResource());
        }
        mMediaPlayer.start();

        // send action to activity
        sendActionToActivity(ACTION_START);
    }

    private void pauseMusic() {
        if (null != mMediaPlayer && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();

            // Send Notification lại để có thể update được view(từ button resume -> button pause)
            if (null != mCurrentSong) {
                sendSongNotification(mCurrentSong);
            }

            // send action to activity
            sendActionToActivity(ACTION_PAUSE);
        }
    }

    private void resumeMusic() {
        if (null != mMediaPlayer && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();

            // Send Notification lại để có thể update được view(từ button pause -> button resume)
            if (null != mCurrentSong) {
                sendSongNotification(mCurrentSong);
            }

            // send action to activity
            sendActionToActivity(ACTION_RESUME);
        }
    }
    // endregion

    // region Handle Action On Notification
    private void handleActionOnNotification(int action) {
        switch (action) {
            case ACTION_PAUSE:
            default:
                pauseMusic();
                break;
            case ACTION_RESUME:
                resumeMusic();
                break;
            case ACTION_STOP:
                // send action to activity
                sendActionToActivity(ACTION_STOP);
                stopSelf();
                break;
        }
    }
    // endregion

    // region Service work with Activity
    private void sendActionToActivity(int action) {
        // Bởi vì từ service có thể send data đến nhiều UI nên sẽ dùng implicit intent để send data tổng quát
        Intent intent = new Intent(FOREGROUND_SERVICE_SEND_ACTION_TO_ACTIVITY_KEY);
        Bundle bundle = new Bundle();
        bundle.putSerializable(FOREGROUND_SERVICE_SEND_OBJECT_SONG_TO_ACTIVITY_KEY, mCurrentSong);
        bundle.putBoolean(FOREGROUND_SERVICE_SEND_MEDIA_PLAYER_STATUS_TO_ACTIVITY_KEY, null != mMediaPlayer && mMediaPlayer.isPlaying() && mMediaPlayer.isPlaying());
        bundle.putInt(FOREGROUND_SERVICE_SEND_MUSIC_ACTION_TO_ACTIVITY_KEY, action);
        intent.putExtras(bundle);
        // Sử dụng LocalBroadcastManager để gửi nhận dữ liệu Từ ForegroundService tới Activity
        // Dùng LocalBroadcastManager sẽ đảm bảo dữ liệu chỉ gửi nhận trong app, không bị rò rỉ dữ liệu
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    // endregion
}
