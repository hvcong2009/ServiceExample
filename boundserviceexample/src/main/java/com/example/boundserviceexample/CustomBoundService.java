package com.example.boundserviceexample;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;

public class CustomBoundService extends Service {

    private CustomBinder mCustomBinder;
    private MediaPlayer mMediaPlayer;

    public class CustomBinder extends Binder {
        public CustomBoundService getCustomBoundService() {
            return CustomBoundService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mCustomBinder = new CustomBinder();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mCustomBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != mMediaPlayer) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void startMusic() {
        if (null == mMediaPlayer) {
            mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.gio_jank);
        }
        mMediaPlayer.start();
    }
}
