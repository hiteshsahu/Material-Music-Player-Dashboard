package com.hiteshsahu.musicplayerconcept.visualizer.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Hitesh on 31-08-2016.
 */
public class VisualizerPlayer {

    private static final String TAG = VisualizerPlayer.class.getSimpleName();
    private MediaPlayer mPlayer;
    private boolean isInitialized;

    private static VisualizerPlayer ourInstance = new VisualizerPlayer();

    public static VisualizerPlayer getInstance() {
        return ourInstance;
    }

    private VisualizerPlayer() {
    }

    public void init(Context appContext, int mediaResource) {

        if (isInitialized) {
            Log.wtf(TAG, "Already initialized");
            return;
        }

        isInitialized = true;
        mPlayer = MediaPlayer.create(appContext, mediaResource);
        mPlayer.setLooping(true);
        mPlayer.start();
    }


    public String getCurrentDuration() {
        return PlayerUtil.milliSecondsToTimer(mPlayer.getCurrentPosition());
    }

    public String getTotalDuration() {
        return PlayerUtil.milliSecondsToTimer(mPlayer.getDuration());
    }

    public int getProgress() {
        return PlayerUtil.getProgressPercentage(mPlayer.getCurrentPosition(), mPlayer.getDuration());
    }

    public void start() throws IOException {

        if (mPlayer == null)
            return;

        if (mPlayer.isPlaying()) {
            return;
        }
        mPlayer.prepare();
        mPlayer.start();
    }

    public void stop() {
        if (mPlayer == null)
            return;

        mPlayer.stop();
    }

    public void pause() {
        if (mPlayer == null)
            return;

        if (!mPlayer.isPlaying()) {
            return;
        }
        mPlayer.pause();
    }

    public void cleanUp() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        return false;
    }

    public MediaPlayer getPlayer() {
        return mPlayer;
    }
}
