package com.example.boundserviceexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnStartService;
    private Button btnStopService;

    private CustomBoundService mCustomBoundService;
    private boolean mIsServiceConnected;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mIsServiceConnected = true;
            CustomBoundService.CustomBinder customBinder = (CustomBoundService.CustomBinder) iBinder;
            mCustomBoundService = customBinder.getCustomBoundService();
            mCustomBoundService.startMusic();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // Callback này được gọi bởi system kill service đột ngột hoặc các lý do khác
            // Callback này không được gọi khi call unBindService()
            mIsServiceConnected = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartService = findViewById(R.id.btn_StartBoundService);
        btnStartService.setOnClickListener(this);
        btnStopService = findViewById(R.id.btn_StopBoundService);
        btnStopService.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_StartBoundService:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    executeStartService();
                }
                break;
            case R.id.btn_StopBoundService:
                executeStopService();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void executeStartService() {
        Intent intent = new Intent(this, CustomBoundService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE );
    }

    private void executeStopService() {
        if (mIsServiceConnected) {
            unbindService(mServiceConnection);
        }
    }
}