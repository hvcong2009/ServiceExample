package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Intent serviceIntent;

    private EditText editTextIntentData;
    private Button btnStartService;
    private Button btnStopService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextIntentData = findViewById(R.id.et_IntentData);
        btnStartService = findViewById(R.id.btn_StartService);
        btnStartService.setOnClickListener(this);
        btnStopService =findViewById(R.id.btn_StopService);
        btnStopService.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_StartService:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    executeStartService();
                }
                break;
            case R.id.btn_StopService:
                executeStopService();
                break;
        }
    }

    private void executeStartService() {
        // Send string data
//        serviceIntent = new Intent(this, CustomService.class);
//        serviceIntent.putExtra(CustomService.KEY, editTextIntentData.getText().toString().trim());
//        startService(serviceIntent);

        // Send object data
        Song song = new Song("Gió", "Gió - JanK x KProx「Lo - Fi Ver」", R.drawable.img_emoji_smile, R.raw.gio_jank);
        Bundle bundle = new Bundle();
        bundle.putSerializable("song_key", song);

        serviceIntent = new Intent(this, CustomService.class);
        serviceIntent.putExtras(bundle);
        startService(serviceIntent);
    }

    private void executeStopService() {
        if (null != serviceIntent) {
            stopService(serviceIntent);
        }
    }
}