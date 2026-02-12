package com.example.kalkulatorsederhana;

import android.content.Context;
import android.media.MediaPlayer;

public class SoundPlayer {
    private final Context context;
    private MediaPlayer mediaPlayer;

    public SoundPlayer(Context context) { this.context = context; }

    public void playClick() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(context, R.raw.click);
        mediaPlayer.start();
    }
}