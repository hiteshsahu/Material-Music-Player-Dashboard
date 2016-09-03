package demo.domain;

import android.media.MediaPlayer;

import com.hiteshsahu.musicplayerconcept.visualizer.utils.PlayerUtil;

/**
 * Created by Hitesh on 31-08-2016.
 */
public class DemoPlayer {

    private static final String TAG = DemoPlayer.class.getSimpleName();
    private MediaPlayer mPlayer = new MediaPlayer();
    ;
    private boolean isInitialized;

    private static DemoPlayer ourInstance = new DemoPlayer();
    private int length;

    public static DemoPlayer getInstance() {
        return ourInstance;
    }

    private DemoPlayer() {
    }

    public String getCurrentDuration() {
        return PlayerUtil.milliSecondsToTimer(mPlayer.getCurrentPosition());

    }

    public int getProgress() {
        return PlayerUtil.getProgressPercentage(mPlayer.getCurrentPosition(), mPlayer.getDuration());
    }

    public void play(String songURL, final SongCompletionListener songCompleted) {

        try {
//            if (mPlayer.isPlaying()) {
            mPlayer.reset();
//            }

            mPlayer.setDataSource(songURL);
            mPlayer.prepare();
            mPlayer.start();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
//                    mp.release();
//                    mp = null;

                    songCompleted.onSongCompletion();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resume() {

        mPlayer.start();
        // mPlayer.seekTo(length);
    }

    public void stop() {
        if (mPlayer == null)
            return;

        mPlayer.stop();
    }

    public void pause() {

        mPlayer.pause();
        length = mPlayer.getCurrentPosition();
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
