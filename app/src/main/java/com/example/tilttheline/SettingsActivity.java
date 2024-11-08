package com.example.tilttheline;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.ImageButton;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import android.view.View;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar volumeSeekBar;
    private ImageButton notificationToggle;
    private ImageButton vibrationToggle;
    private boolean notificationsEnabled;
    private boolean vibrationsEnabled;
    private Vibrator vibrator;

    private static final String CHANNEL_ID = "custom_channel_id";
    private static final int NOTIFICATION_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(view -> {
            view.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
                onBackPressed();
                view.animate().scaleX(1f).scaleY(1f).setDuration(100);
            });
        });

        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        notificationToggle = findViewById(R.id.notificationToggle);
        vibrationToggle = findViewById(R.id.vibrationToggle);
        SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);

        int previousVolume = preferences.getInt("volume", 50);
        volumeSeekBar.setProgress(previousVolume);
        setVolume(previousVolume / 100f);

        notificationsEnabled = preferences.getBoolean("notifications_enabled", true);
        vibrationsEnabled = preferences.getBoolean("vibrations_enabled", true);
        updateNotificationIcon();
        updateVibrationIcon();

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("volume", progress);
                editor.apply();
                setVolume(progress / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        notificationToggle.setOnClickListener(view -> {
            notificationsEnabled = !notificationsEnabled;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("notifications_enabled", notificationsEnabled);
            editor.apply();
            updateNotificationIcon();

            if (notificationsEnabled) {
                sendNotification();
            } else {
                Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show();
            }
        });

        vibrationToggle.setOnClickListener(view -> {
            vibrationsEnabled = !vibrationsEnabled;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("vibrations_enabled", vibrationsEnabled);
            editor.apply();
            updateVibrationIcon();

            if (vibrationsEnabled && vibrator != null) {
                vibrator.vibrate(200); // Vibration for 200 ms
            }
        });
    }

    private void setVolume(float volume) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction("SET_VOLUME");
        intent.putExtra("volume", volume);
        startService(intent);
    }

    private void sendNotification() {
        if (!notificationsEnabled) {
            return; // Do nothing if notifications are disabled
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission not granted", Toast.LENGTH_SHORT).show();
                return; // Don't proceed without permission
            }
        }

        Intent intent = new Intent(this, GameActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notif_spash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Custom Channel", NotificationManager.IMPORTANCE_DEFAULT);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.stringray)
                .setContentTitle("POISSON RARE !")
                .setContentText("UN LEVIATHAN EST APPARU")
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }



    private void updateNotificationIcon() {
        if (notificationsEnabled) {
            notificationToggle.setImageResource(R.drawable.notif_icon);
        } else {
            notificationToggle.setImageResource(R.drawable.notif_icon_disabled);
        }
    }

    private void updateVibrationIcon() {
        if (vibrationsEnabled) {
            vibrationToggle.setImageResource(R.drawable.vibra_icon);
        } else {
            vibrationToggle.setImageResource(R.drawable.vibra_icon_disabled);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
