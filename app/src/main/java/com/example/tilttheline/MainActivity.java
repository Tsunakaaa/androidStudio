package com.example.tilttheline;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private VolumeChangeReceiver volumeChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton button = findViewById(R.id.buttonOpenGame);
        TextView textPlay = findViewById(R.id.textPlay);
        ImageButton settingsButton = findViewById(R.id.buttonOpenSettings);
        TextView textSettings = findViewById(R.id.textSettings);
        ImageButton leaveButton = findViewById(R.id.buttonLeave);
        TextView textQuit = findViewById(R.id.textQuit);

        button.setOnClickListener(view -> {
            button.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100);
            textPlay.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                startActivity(intent);
                button.animate().scaleX(1f).scaleY(1f).setDuration(100);
                textPlay.animate().scaleX(1f).scaleY(1f).setDuration(100);
            });
        });

        settingsButton.setOnClickListener(view -> {
            settingsButton.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100);
            textSettings.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                settingsButton.animate().scaleX(1f).scaleY(1f).setDuration(100);
                textSettings.animate().scaleX(1f).scaleY(1f).setDuration(100);
            });
        });

        leaveButton.setOnClickListener(v -> {
            leaveButton.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100);
            textQuit.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
                leaveButton.animate().scaleX(1f).scaleY(1f).setDuration(100);
                textQuit.animate().scaleX(1f).scaleY(1f).setDuration(100).withEndAction(() -> {
                    finish();
                    System.exit(0);
                });
            });
        });

        mediaPlayer = MediaPlayer.create(this, R.raw.menumusic);
        mediaPlayer.setLooping(true);

        volumeChangeReceiver = new VolumeChangeReceiver(mediaPlayer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isServiceRunning(MusicService.class)) {
            Log.d("MusicService", "Starting music service");
            Intent bgMusic = new Intent(this, MusicService.class);
            startService(bgMusic);
        }
        registerReceiver(volumeChangeReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(volumeChangeReceiver);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
