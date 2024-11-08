package com.example.tilttheline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

public class VolumeChangeReceiver extends BroadcastReceiver {

    private MediaPlayer mediaPlayer;

    public VolumeChangeReceiver(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.media.VOLUME_CHANGED_ACTION".equals(intent.getAction())) {
            // Ici, tu pourrais mettre à jour le volume du MediaPlayer si nécessaire
            // Pour l'exemple, rien de spécial est fait ici.
        }
    }
}
