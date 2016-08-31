package com.hiteshsahu.musicplayerconcept.widget;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hiteshsahu.musicplayerconcept.R;
import com.hiteshsahu.musicplayerconcept.visualizer.VisualizerView;
import com.hiteshsahu.musicplayerconcept.visualizer.renderer.BarGraphRenderer;
import com.hiteshsahu.musicplayerconcept.visualizer.renderer.CircleBarRenderer;
import com.hiteshsahu.musicplayerconcept.visualizer.renderer.CircleRenderer;
import com.hiteshsahu.musicplayerconcept.visualizer.renderer.LineRenderer;
import com.hiteshsahu.musicplayerconcept.visualizer.utils.PlayerUtil;
import com.hiteshsahu.musicplayerconcept.visualizer.utils.TunnelPlayerWorkaround;
import com.hiteshsahu.musicplayerconcept.visualizer.utils.VisualizerPlayer;

import java.io.IOException;

/**
 * Created by 663918 on 8/30/2016.
 */
public class MusicProgressLayout extends RelativeLayout {

    private Handler mHandler = new Handler();
    LayoutInflater mInflater;
    private View rootView;
    // private MediaPlayer mPlayer;
    private MediaPlayer mSilentPlayer;  /* to avoid tunnel player issue */
    private VisualizerView mVisualizerView;
    private FloatingActionButton playPauseButton;
    private TextView totalTime;
    private TextView timeLapse;
    private ProgressBar songProgressBar;
    private RelativeLayout topSongInfoContainer, bottomSongProgressContainer;

    public void updateProgressBar() {
        mHandler.postDelayed(updateProgressTask, 100);
    }

    //ProgressUpdate
    private int totalDuration;
    private int currentDuration;
    /**
     * Background Runnable thread
     */
    private Runnable updateProgressTask = new Runnable() {
        public void run() {

            // Displaying Total Duration time
            timeLapse.setText(VisualizerPlayer.getInstance().getCurrentDuration());

            totalTime.setText(VisualizerPlayer.getInstance().getTotalDuration());

            // Updating progress bar
            int progress = (int) (PlayerUtil.getProgressPercentage(currentDuration, totalDuration));

            //Log.d("Progress", ""+progress);
            songProgressBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    public void init() {
        rootView = mInflater.inflate(R.layout.music_progress_layout, this, true);
        mVisualizerView = (VisualizerView) rootView.findViewById(R.id.visualizerView);
        playPauseButton = (FloatingActionButton) rootView.findViewById(R.id.play);
        timeLapse = (TextView) rootView.findViewById(R.id.time_lapse);
        totalTime = (TextView) rootView.findViewById(R.id.total_time);
        songProgressBar = (ProgressBar) rootView.findViewById(R.id.song_progress);

        topSongInfoContainer = (RelativeLayout) rootView.findViewById(R.id.song_info_container);
        bottomSongProgressContainer = (RelativeLayout) rootView.findViewById(R.id.song_progress_container);

        playPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.vinyl));

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (VisualizerPlayer.getInstance().isPlaying()) {
                    VisualizerPlayer.getInstance().stop();
                    playPauseButton.setImageDrawable(ContextCompat.getDrawable(getContext(), android.R.drawable.ic_media_play));

                    ObjectAnimator outAnim = ObjectAnimator.ofFloat(topSongInfoContainer, "x", -getScreenWidth(), 0);
                    outAnim.setDuration(1000);
                    outAnim.start();

                    updateProgressBar();
                } else {
//                    try {
//                        startPressed();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    try {
                        VisualizerPlayer.getInstance().start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    playPauseButton.setImageDrawable(ContextCompat.getDrawable(getContext(), android.R.drawable.ic_media_pause));

                    ObjectAnimator outAnim = ObjectAnimator.ofFloat(topSongInfoContainer, "x", 0, -getScreenWidth());
                    outAnim.setDuration(1000);
                    outAnim.start();

                    mHandler.removeCallbacks(updateProgressTask);
                }

            }
        });

        initTunnelPlayerWorkaround();
        initVisualizer();
    }

    public MusicProgressLayout(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
        init();
    }

    public MusicProgressLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        init();
    }

    public MusicProgressLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInflater = LayoutInflater.from(context);
        init();
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)

    public MusicProgressLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mInflater = LayoutInflater.from(context);
        init();
    }

    private void initVisualizer() {
//        mPlayer = MediaPlayer.create(getContext(), R.raw.test);
//        mPlayer.setLooping(true);
//        mPlayer.start();

        VisualizerPlayer.getInstance().init(getContext(), R.raw.test);

        // We need to link the visualizer view to the media player so that
        // it displays something

        mVisualizerView.link(VisualizerPlayer.getInstance().getPlayer());

        // Start with just line renderer
        addBarGraphRenderers();
    }

    private void cleanUp() {
        if (VisualizerPlayer.getInstance().getPlayer() != null) {
            mVisualizerView.release();
//            mPlayer.release();
//            mPlayer = null;

            VisualizerPlayer.getInstance().cleanUp();
        }

        if (mSilentPlayer != null) {
            mSilentPlayer.release();
            mSilentPlayer = null;
        }
    }

    // Workaround (for Galaxy S4)
    //
    // "Visualization does not work on the new Galaxy devices"
    //    https://github.com/felixpalmer/android-visualizer/issues/5
    //
    // NOTE:
    //   This code is not required for visualizing default "test.mp3" file,
    //   because tunnel player is used when duration is longer than 1 minute.
    //   (default "test.mp3" file: 8 seconds)
    //
    private void initTunnelPlayerWorkaround() {
        // Read "tunnel.decode" system property to determine
        // the workaround is needed
        if (TunnelPlayerWorkaround.isTunnelDecodeEnabled(getContext())) {
            mSilentPlayer = TunnelPlayerWorkaround.createSilentMediaPlayer(getContext());
        }
    }

    // Methods for adding renderers to visualizer
    private void addBarGraphRenderers() {
        Paint paint = new Paint();
        paint.setStrokeWidth(20f);
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        BarGraphRenderer barGraphRendererBottom = new BarGraphRenderer(16, paint, false);
        mVisualizerView.addRenderer(barGraphRendererBottom);

//        Paint paint2 = new Paint();
//        paint2.setStrokeWidth(12f);
//        paint2.setAntiAlias(true);
//        paint2.setColor(Color.argb(200, 181, 111, 233));
//        BarGraphRenderer barGraphRendererTop = new BarGraphRenderer(4, paint2, true);
//        mVisualizerView.addRenderer(barGraphRendererTop);
    }


    //You can show other type of visualizers

    private void addCircleBarRenderer() {
        Paint paint = new Paint();
        paint.setStrokeWidth(8f);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
        paint.setColor(Color.argb(255, 222, 92, 143));
        CircleBarRenderer circleBarRenderer = new CircleBarRenderer(paint, 32, true);
        mVisualizerView.addRenderer(circleBarRenderer);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        cleanUp();
    }

    private void addCircleRenderer() {
        Paint paint = new Paint();
        paint.setStrokeWidth(3f);
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(255, 222, 92, 143));
        CircleRenderer circleRenderer = new CircleRenderer(paint, true);
        mVisualizerView.addRenderer(circleRenderer);
    }

    private void addLineRenderer() {
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(1f);
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.argb(88, 0, 128, 255));

        Paint lineFlashPaint = new Paint();
        lineFlashPaint.setStrokeWidth(5f);
        lineFlashPaint.setAntiAlias(true);
        lineFlashPaint.setColor(Color.argb(188, 255, 255, 255));
        LineRenderer lineRenderer = new LineRenderer(linePaint, lineFlashPaint, true);
        mVisualizerView.addRenderer(lineRenderer);
    }

    // Actions for buttons defined in xml
//    public void startPressed() throws IllegalStateException, IOException {
//        if (mPlayer.isPlaying()) {
//            return;
//        }
//        mPlayer.prepare();
//        mPlayer.start();
//
//    }
//
//    public void stopPressed() {
//        mPlayer.stop();
//
//    }


    public void barPressed(View view) {
        addBarGraphRenderers();
    }

    public void circlePressed(View view) {
        addCircleRenderer();
    }

    public void circleBarPressed(View view) {
        addCircleBarRenderer();
    }

    public void linePressed(View view) {
        addLineRenderer();
    }

    public void clearPressed(View view) {
        mVisualizerView.clearRenderers();
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}


