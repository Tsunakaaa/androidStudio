package com.example.tilttheline;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.menumusic);
        mediaPlayer.setLooping(true);
        Log.d("MusicService", "MediaPlayer created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            // Vérifiez si une action de volume est reçue
            if ("SET_VOLUME".equals(intent.getAction())) {
                float volume = intent.getFloatExtra("volume", 1f);
                setVolume(volume);
            } else if (!mediaPlayer.isPlaying()) {
                SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
                int volume = preferences.getInt("volume", 100); // Valeur par défaut à 100
                float logVolume = (float) (1 - (Math.log(100 - volume) / Math.log(100))); // Convertir en échelle 0-1
                mediaPlayer.setVolume(logVolume, logVolume);
                mediaPlayer.start();
                Log.d("MusicService", "MediaPlayer started with volume: " + volume);
            }
        }
        return START_NOT_STICKY;
    }

    public void setVolume(float volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume, volume); // Mettre à jour le volume du MediaPlayer
            Log.d("MusicService", "MediaPlayer volume set to: " + volume);
        }
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d("MusicService", "MediaPlayer stopped and released");
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
