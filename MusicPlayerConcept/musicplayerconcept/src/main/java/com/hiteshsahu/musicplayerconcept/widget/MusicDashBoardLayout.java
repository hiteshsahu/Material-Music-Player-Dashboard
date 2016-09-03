package com.hiteshsahu.musicplayerconcept.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hiteshsahu.musicplayerconcept.R;
import com.hiteshsahu.musicplayerconcept.visualizer.VisualizerView;
import com.hiteshsahu.musicplayerconcept.visualizer.renderer.BarGraphRenderer;
import com.hiteshsahu.musicplayerconcept.visualizer.renderer.CircleBarRenderer;
import com.hiteshsahu.musicplayerconcept.visualizer.renderer.CircleRenderer;
import com.hiteshsahu.musicplayerconcept.visualizer.renderer.LineRenderer;
import com.hiteshsahu.musicplayerconcept.visualizer.utils.ColorGenerator;
import com.hiteshsahu.musicplayerconcept.visualizer.utils.TextDrawable;
import com.hiteshsahu.musicplayerconcept.visualizer.utils.TunnelPlayerWorkaround;

/**
 * Created by 663918 on 8/30/2016.
 */
public class MusicDashBoardLayout extends RelativeLayout {

    //Local contants
    private static final int ANIMATION_DURATION = 800;
    private static final int TEXT_DISPLACMENT = 100;
    private final static Uri sArtworkUri = Uri
            .parse("content://media/external/audio/albumart");
    protected static final int CLOCK_DISPLACMENT = 80;

    private LayoutInflater mInflater;
    private View rootView;
    private MediaPlayer mSilentPlayer;  /* to avoid tunnel player issue */
    private VisualizerView mVisualizerView;
    private FloatingActionButton playPauseButton;
    private TextView totalTime;
    private SeekBar songProgressBar;
    private CardView thumbContainer;
    private ImageView thumbNail;
    private ImageView clockIcon;
    private TextView songName;
    private TextView albumName;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;
    private TextDrawable drawable;

    //ProgressUpdate
    private int totalDuration;
    private int currentDuration;

    public MusicDashBoardLayout(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
        init();
    }

    public MusicDashBoardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        init();
    }

    public MusicDashBoardLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInflater = LayoutInflater.from(context);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MusicDashBoardLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mInflater = LayoutInflater.from(context);
        init();
    }

    private static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    /**
     * use bar visualizer
     */
    public void useBarVisualizer() {
        addBarGraphRenderers();
    }

    /**
     * use Circle Visualizer
     */
    public void useCircleVisualizer() {
        addCircleRenderer();
    }

    /**
     * use CircleBar Visualizer
     */
    public void useCircleBarVisualizer() {
        addCircleBarRenderer();
    }

    /**
     * use Line Visualizer
     */
    public void useLineVisualizer() {
        addLineRenderer();
    }

    /**
     * clear Visualizer
     */
    public void clearVisualizer() {
        mVisualizerView.clearRenderers();
    }


    public void updateSongInfo(String songNameValue, String albumNameValue, long albumArt) {
        songName.setText(songNameValue);
        albumName.setText(albumNameValue);

        drawable = mDrawableBuilder.build(String.valueOf(songNameValue.charAt(0)), mColorGenerator
                .getColor(songNameValue));

        Glide.with(getContext())
                .load(ContentUris.withAppendedId(sArtworkUri,
                        albumArt))
                .centerCrop()
                .placeholder(R.drawable.test_image)
                .error(drawable).crossFade()
                .into(thumbNail);
    }

    public SeekBar getSongProgressBar() {
        return songProgressBar;
    }

    private void init() {
        rootView = mInflater.inflate(R.layout.music_progress_layout, this, true);
        mVisualizerView = (VisualizerView) rootView.findViewById(R.id.visualizerView);
        playPauseButton = (FloatingActionButton) rootView.findViewById(R.id.play);
        totalTime = (TextView) rootView.findViewById(R.id.total_time);
        songProgressBar = (SeekBar) rootView.findViewById(R.id.song_progress);
        thumbNail = (ImageView) rootView.findViewById(R.id.album_art);
        clockIcon = (ImageView) rootView.findViewById(R.id.clock);
        thumbContainer = (CardView) rootView.findViewById(R.id.thumb_container);
        songName = (TextView) rootView.findViewById(R.id.song_name);
        songName.setSelected(true);
        albumName = (TextView) rootView.findViewById(R.id.album_name);
        albumName.setSelected(true);
        mDrawableBuilder = TextDrawable.builder().beginConfig().withBorder(4)
                .endConfig().roundRect(10);

    }

    public void animatePlay() {

        playPauseButton.setImageDrawable(ContextCompat.getDrawable(getContext(), android.R.drawable.ic_media_pause));

        //Animate Exit animation
        ObjectAnimator thumbArtExitAnimation = ObjectAnimator.ofFloat(thumbContainer, "x", 0, -getScreenWidth());
        thumbArtExitAnimation.setDuration(ANIMATION_DURATION);
        thumbArtExitAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        thumbArtExitAnimation.start();

        ObjectAnimator songInfoExitAnimation = ObjectAnimator.ofFloat(songName, "x", songName.getX(), songName.getX() - TEXT_DISPLACMENT);
        songInfoExitAnimation.setDuration(ANIMATION_DURATION / 2);
        songInfoExitAnimation.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator songInfExitAlphaAnimation = ObjectAnimator.ofFloat(songName, "alpha", 1, 0);
        songInfExitAlphaAnimation.setDuration(ANIMATION_DURATION);
        songInfExitAlphaAnimation.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator albumInfoExitAnimation = ObjectAnimator.ofFloat(albumName, "x", albumName.getX(), albumName.getX() - TEXT_DISPLACMENT);
        albumInfoExitAnimation.setDuration(ANIMATION_DURATION / 2);
        albumInfoExitAnimation.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator albumInfoExitAlphaAnimation = ObjectAnimator.ofFloat(albumName, "alpha", 1, 0);
        albumInfoExitAlphaAnimation.setDuration(ANIMATION_DURATION);
        albumInfoExitAlphaAnimation.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator visualizerEntryAlphaAnimation = ObjectAnimator.ofFloat(mVisualizerView, "alpha", 0, 1);
        visualizerEntryAlphaAnimation.setDuration(ANIMATION_DURATION);
        visualizerEntryAlphaAnimation.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator progressEntryAlphaAnimation = ObjectAnimator.ofFloat(songProgressBar, "alpha", 0, 1);
        progressEntryAlphaAnimation.setDuration(ANIMATION_DURATION);
        progressEntryAlphaAnimation.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator clockExitAnimation = ObjectAnimator.ofFloat(clockIcon, "x", clockIcon.getX(), clockIcon.getX() - CLOCK_DISPLACMENT);
        clockExitAnimation.setDuration(ANIMATION_DURATION / 2);
        clockExitAnimation.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator timeExitAnimation = ObjectAnimator.ofFloat(totalTime, "x", totalTime.getX(), totalTime.getX() - CLOCK_DISPLACMENT);
        timeExitAnimation.setDuration(ANIMATION_DURATION / 2);
        timeExitAnimation.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator clockExitAlphaAnimation = ObjectAnimator.ofFloat(clockIcon, "alpha", 1, 0);
        clockExitAlphaAnimation.setDuration(ANIMATION_DURATION);
        clockExitAlphaAnimation.setInterpolator(new FastOutSlowInInterpolator());

        AnimatorSet set = new AnimatorSet();
        set.playTogether(songInfoExitAnimation, songInfExitAlphaAnimation,
                albumInfoExitAlphaAnimation, albumInfoExitAnimation,
                visualizerEntryAlphaAnimation,
                progressEntryAlphaAnimation,
                clockExitAnimation, clockExitAlphaAnimation,
                timeExitAnimation);

        set.start();

    }

    public void animatePause() {

        playPauseButton.setImageDrawable(ContextCompat.getDrawable(getContext(), android.R.drawable.ic_media_play));

        //Animate Entry Animation
        ObjectAnimator thumbArtEntryAnimation = ObjectAnimator.ofFloat(thumbContainer, "x", -getScreenWidth(), 0);
        thumbArtEntryAnimation.setDuration(ANIMATION_DURATION);
        thumbArtEntryAnimation.setInterpolator(new FastOutSlowInInterpolator());
        thumbArtEntryAnimation.start();

        ObjectAnimator songInfoEntryAnimation = ObjectAnimator.ofFloat(songName, "x", songName.getX(), songName.getX() + TEXT_DISPLACMENT);
        songInfoEntryAnimation.setDuration(ANIMATION_DURATION / 2);
        songInfoEntryAnimation.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator songInfoEntryAlphaAnimation = ObjectAnimator.ofFloat(songName, "alpha", 0, 1);
        songInfoEntryAlphaAnimation.setDuration(ANIMATION_DURATION);
        songInfoEntryAlphaAnimation.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator albumInfoEntryAnimation = ObjectAnimator.ofFloat(albumName, "x", albumName.getX(), albumName.getX() + TEXT_DISPLACMENT);
        albumInfoEntryAnimation.setDuration(ANIMATION_DURATION / 2);
        albumInfoEntryAnimation.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator clockEntryAnimation = ObjectAnimator.ofFloat(clockIcon, "x", clockIcon.getX(), clockIcon.getX() + CLOCK_DISPLACMENT);
        clockEntryAnimation.setDuration(ANIMATION_DURATION / 2);
        clockEntryAnimation.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator albumInfoEntryAlphaAnimation = ObjectAnimator.ofFloat(albumName, "alpha", 0, 1);
        albumInfoEntryAlphaAnimation.setDuration(ANIMATION_DURATION);
        albumInfoEntryAlphaAnimation.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator visualizerExitAlphaAnimation = ObjectAnimator.ofFloat(mVisualizerView, "alpha", 1, 0);
        visualizerExitAlphaAnimation.setDuration(ANIMATION_DURATION);
        visualizerExitAlphaAnimation.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator progressExitAlphaAnimation = ObjectAnimator.ofFloat(songProgressBar, "alpha", 1, 0);
        visualizerExitAlphaAnimation.setDuration(ANIMATION_DURATION);
        visualizerExitAlphaAnimation.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator timeEntryAnimation = ObjectAnimator.ofFloat(totalTime, "x", totalTime.getX(), totalTime.getX() + CLOCK_DISPLACMENT);
        timeEntryAnimation.setDuration(ANIMATION_DURATION / 2);
        timeEntryAnimation.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator clockEntryAlphaAnimation = ObjectAnimator.ofFloat(clockIcon, "alpha", 0, 1);
        clockEntryAlphaAnimation.setDuration(ANIMATION_DURATION);
        clockEntryAlphaAnimation.setInterpolator(new FastOutSlowInInterpolator());

        AnimatorSet set = new AnimatorSet();
        set.playTogether(songInfoEntryAnimation, songInfoEntryAlphaAnimation,
                albumInfoEntryAnimation, albumInfoEntryAlphaAnimation,
                progressExitAlphaAnimation,
                visualizerExitAlphaAnimation,
                clockEntryAnimation, clockEntryAlphaAnimation,
                timeEntryAnimation
        );

        set.start();

    }

    /**
     * Start Visualizer in DashBoard
     *
     * @param player
     */
    public void initVisualizer(MediaPlayer player) {

        initTunnelPlayerWorkaround();

        // We need to link the visualizer view to the media player so that
        // it displays something
        mVisualizerView.link(player);

        // Start with just line renderer
        addBarGraphRenderers();
    }

    public VisualizerView getVisualizerView() {
        return mVisualizerView;
    }

    public void cleanUp() {
        mVisualizerView.release();

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

    /**
     * Update song progress
     *
     * @param currentDuration
     * @param progress
     */
    public void updateProgress(String currentDuration, int progress) {

        // Displaying Total Duration time
        totalTime.setText(currentDuration);
        songProgressBar.setProgress(progress);

    }

    public FloatingActionButton getPlayPauseButton() {
        return playPauseButton;
    }

}


