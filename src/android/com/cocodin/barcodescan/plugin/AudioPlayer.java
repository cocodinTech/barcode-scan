package com.cocodin.barcodescan.plugin;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

public class AudioPlayer {

    private static final String ASSETS_PATH = "www/assets/";
    private final Context context;
    private MediaPlayer m;

    public AudioPlayer(Context context) {
        this.context = context;
    }

    public void play(String fileName, float vol) {
        try {
            if (m != null && m.isPlaying()) {
                m.stop();
                m.release();
            }

            m = new MediaPlayer();
            AssetFileDescriptor descriptor = context.getAssets().openFd(ASSETS_PATH + fileName);
            m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            m.prepare();
            m.setVolume(vol, vol);
            m.start();
//            m.stop();
//            m.release();
        } catch (Exception e) {
            Log.e("*** error audio play: ", e.getMessage());
        }

    }
}
