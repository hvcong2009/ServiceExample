package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Intent serviceIntent;

    private EditText editTextIntentData;
    private Button btnStartService;
    private Button btnStopService;

    private RelativeLayout layoutBottom;
    private ImageView imgSong, imgPauseOrResumeSong, imgStopSong;
    private TextView txtSongTitle, txtSongContent;

    private Song mCurrentSong;
    private boolean mIsPlaying; // phải tạo variable này để control status play music bởi vì không thể send cả object MediaPlayer(vì nó là class của system không thể extends Serializable)
    private int mMusicAction;

    public static final String ACTIVITY_SEND_ACTION_TO_FOREGROUND_SERVICE_KEY = "activity_action_to_service_key";

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get song data from foreground service
            Bundle bundle = intent.getExtras();
            if (null != bundle) {
                if (null != bundle.get(CustomForegroundService.FOREGROUND_SERVICE_SEND_OBJECT_SONG_TO_ACTIVITY_KEY)) {
                    mCurrentSong = (Song) bundle.get(CustomForegroundService.FOREGROUND_SERVICE_SEND_OBJECT_SONG_TO_ACTIVITY_KEY);
                }
                mIsPlaying = bundle.getBoolean(CustomForegroundService.FOREGROUND_SERVICE_SEND_MEDIA_PLAYER_STATUS_TO_ACTIVITY_KEY, false);
                mMusicAction = bundle.getInt(CustomForegroundService.FOREGROUND_SERVICE_SEND_MUSIC_ACTION_TO_ACTIVITY_KEY, 0);

                // Update music layout on
                handleLayoutMusic(mMusicAction);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextIntentData = findViewById(R.id.et_IntentData);
        btnStartService = findViewById(R.id.btn_StartService);
        btnStartService.setOnClickListener(this);
        btnStopService = findViewById(R.id.btn_StopService);
        btnStopService.setOnClickListener(this);

        layoutBottom = findViewById(R.id.layout_BottomInfo);
        imgSong = findViewById(R.id.img_songImage);
        imgPauseOrResumeSong = findViewById(R.id.img_pauseOrResumeSong);
        imgPauseOrResumeSong.setOnClickListener(this);
        imgStopSong = findViewById(R.id.img_stopSong);
        imgStopSong.setOnClickListener(this);
        txtSongTitle = findViewById(R.id.txt_songTitle);
        txtSongContent = findViewById(R.id.txt_songContent);

        // Sử dụng LocalBroadcastManager để gửi nhận dữ liệu Từ ForegroundService tới Activity
        // Dùng LocalBroadcastManager sẽ đảm bảo dữ liệu chỉ gửi nhận trong app, không bị rò rỉ dữ liệu
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(CustomForegroundService.FOREGROUND_SERVICE_SEND_ACTION_TO_ACTIVITY_KEY));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // unregisterReceiver for mBroadcastReceiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
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
            case R.id.img_pauseOrResumeSong:
                if (mIsPlaying) {
                    sendMusicActionToService(CustomForegroundService.ACTION_PAUSE);
                } else {
                    sendMusicActionToService(CustomForegroundService.ACTION_RESUME);
                }
                break;
            case R.id.img_stopSong:
                layoutBottom.setVisibility(View.GONE);
                executeStopService();
                break;
        }
    }

    private void executeStartService() {
        // Send string data
//        serviceIntent = new Intent(this, CustomForegroundService.class);
//        serviceIntent.putExtra(CustomForegroundService.KEY, editTextIntentData.getText().toString().trim());
//        startService(serviceIntent);

        // Send object data
        Song song = new Song("Gió", "Gió - JanK x KProx「Lo - Fi Ver」", R.drawable.img_emoji_smile, R.raw.gio_jank);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CustomForegroundService.SONG_KEY, song);

        // App thông thường sẽ chỉ có 1 Service cụ thể, nên sẽ dùng Explicit Intent để send tường mình hơn, tránh rò rỉ data đến những nơi không cần thiết
        serviceIntent = new Intent(this, CustomForegroundService.class);
        serviceIntent.putExtras(bundle);
        startService(serviceIntent);
    }

    private void executeStopService() {
        if (null != serviceIntent) {
            stopService(serviceIntent);
        }
    }

    private void sendMusicActionToService(int action) {
        // App thông thường sẽ chỉ có 1 Service cụ thể, nên sẽ dùng Explicit Intent để send tường mình hơn, tránh rò rỉ data đến những nơi không cần thiết
        Intent intent = new Intent(this, CustomForegroundService.class);
        Bundle bundle = new Bundle();
        bundle.putInt(CustomForegroundService.FOREGROUND_SERVICE_RECEIVER_MUSIC_ACTION_KEY, action);
        intent.putExtras(bundle);

        // Call startService để send data từ Activity đến Service
        startService(intent);
    }

    private void handleLayoutMusic(int action) {
        switch (action) {
            case CustomForegroundService.ACTION_START:
            default:
                layoutBottom.setVisibility(View.VISIBLE);
                loadSongLayout();
                updateSongStatus();
                break;
            case CustomForegroundService.ACTION_PAUSE:
            case CustomForegroundService.ACTION_RESUME:
                updateSongStatus();
                break;
            case CustomForegroundService.ACTION_STOP:
                updateSongStatus();
                layoutBottom.setVisibility(View.GONE);
                break;
        }
    }

    private void loadSongLayout() {
        if (null != mCurrentSong) {
            imgSong.setImageResource(mCurrentSong.getImage());
            txtSongTitle.setText(mCurrentSong.getTitle());
            txtSongContent.setText(mCurrentSong.getSingle());
        }
    }

    private void updateSongStatus() {
        if (mIsPlaying) {
            imgPauseOrResumeSong.setImageResource(R.drawable.img_pause_button);
        } else {
            imgPauseOrResumeSong.setImageResource(R.drawable.img_resume_button);
        }
    }
}