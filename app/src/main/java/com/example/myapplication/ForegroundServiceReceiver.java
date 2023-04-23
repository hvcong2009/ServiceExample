package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ForegroundServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int action = intent.getIntExtra(CustomForegroundService.NOTIFICATION_SEND_ACTION_TO_FOREGROUND_SERVICE_KEY, 0);

        // App thông thường sẽ chỉ có 1 Service cụ thể, nên sẽ dùng Explicit Intent để send tường mình hơn, tránh rò rỉ data đến những nơi không cần thiết
        Intent foregroundServiceIntent = new Intent(context, CustomForegroundService.class);
        foregroundServiceIntent.putExtra(CustomForegroundService.FOREGROUND_SERVICE_RECEIVER_MUSIC_ACTION_KEY, action);

        // Việc call startService nhiều lần sẽ không tạo lại Service, nó chỉ nhảy vào callback onStartCommand thôi
        context.startService(foregroundServiceIntent);
    }
}
