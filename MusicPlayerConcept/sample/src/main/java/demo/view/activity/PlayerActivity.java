package demo.view.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.hiteshsahu.musicplayerconcept.visualizer.utils.PlayerUtil;
import com.hiteshsahu.musicplayerconcept.widget.MusicDashBoardLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import demo.R;
import demo.domain.DemoPlayer;
import demo.domain.SongCompletionListener;
import demo.model.DemoDataProvider;
import demo.view.adapter.DepthPageTransformer;
import demo.view.adapter.SongPagerAdapter;


public class PlayerActivity extends AppCompatActivity implements SongCompletionListener {


    private enum VisualizerMode {
        BAR, CIRCLE, CIRCLE_BAR, LINE
    }

    VisualizerMode currentVisualizer = VisualizerMode.BAR;

    //State variables
    private int currentSelectedSong = 0;
    private boolean IS_SONG_PAUSED = false;

    //Views
    private ViewPager slidingPager;
    private MusicDashBoardLayout dashBoardControls;

    //SOng progress Update Handler
    private Handler songProgressHandler = new Handler();
    protected static final int DELAY_MILLIS = 100;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;


    private Runnable updateProgressTask = new Runnable() {
        public void run() {
            new Handler(getApplicationContext().getMainLooper()).post(new Runnable() {
                @Override
                public void run() {

                    //Update Song progress info on Mian Thread
                    dashBoardControls.updateProgress(DemoPlayer.getInstance().getCurrentDuration(), DemoPlayer.getInstance().getProgress());
                }
            });
            // Running this thread after 100 milliseconds
            songProgressHandler.postDelayed(this, DELAY_MILLIS);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        slidingPager = (ViewPager) findViewById(R.id.slidingPager);

        //This is Our custom layout
        dashBoardControls = (MusicDashBoardLayout) findViewById(R.id.controls);

        //Handle Play Pause
        dashBoardControls.getPlayPauseButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DemoPlayer.getInstance().isPlaying()) {

                    //Stop Visualizer
                    dashBoardControls.getVisualizerView().setEnabled(false);

                    //animate pause song animation
                    dashBoardControls.animatePause();

                    //Stop Progressbar update
                    songProgressHandler.removeCallbacks(updateProgressTask);

                    //Finally Pause song
                    pauseThisSong();

                } else {

                    //Start Visualizer
                    dashBoardControls.getVisualizerView().setEnabled(true);

                    //animate play song animation
                    dashBoardControls.animatePlay();

                    //Start Playback
                    playThisSong();

                    //Start Progress bar update
                    songProgressHandler.postDelayed(updateProgressTask, DELAY_MILLIS);
                }
            }
        });


        dashBoardControls.getSongProgressBar().setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                songProgressHandler.removeCallbacks(updateProgressTask);

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {

                if (fromUser) {
                    songProgressHandler.removeCallbacks(updateProgressTask);

                    int totalDuration = DemoPlayer.getInstance().getPlayer()
                            .getDuration();

                    int currentPosition = PlayerUtil.progressToTimer(
                            seekBar.getProgress(), totalDuration);

                    // forward or backward to certain seconds
                    DemoPlayer.getInstance().getPlayer()
                            .seekTo(currentPosition);

                    //Start Progress bar update
                    songProgressHandler.postDelayed(updateProgressTask, DELAY_MILLIS);
                }

            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            // Pain in A$$ Marshmallow+ Permission APIs
            insertDummyContactWrapper();
        } else {
            // Pre-Marshmallow
            LoadSongsFromPhoneMemory();
        }
    }

    @Override
    public void onSongCompletion() {

        //Stop Progressbar update
        songProgressHandler.removeCallbacks(updateProgressTask);

        //Start Playback
        playNextSong();

    }

    private void LoadSongsFromPhoneMemory() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                DemoDataProvider.getInstance().getAllSongs(PlayerActivity.this);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                setUpUI();
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.MODIFY_AUDIO_SETTINGS, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    LoadSongsFromPhoneMemory();

                } else {
                    // Permission Denied
                    Toast.makeText(PlayerActivity.this, "Some Permission is Denied Exiting App", Toast.LENGTH_SHORT)
                            .show();

                    finish();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void insertDummyContactWrapper() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read SD Card");
        if (!addPermission(permissionsList, Manifest.permission.RECORD_AUDIO))
            permissionsNeeded.add("Record Audio");
        if (!addPermission(permissionsList, Manifest.permission.MODIFY_AUDIO_SETTINGS))
            permissionsNeeded.add("Modify Settings");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You must grant access to " + permissionsNeeded.get(0);

                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);

                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }

        //Load all songs from SD card
        LoadSongsFromPhoneMemory();
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(PlayerActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }


    private void setUpUI() {

        //Set up Bar Visualizer
        dashBoardControls.initVisualizer(DemoPlayer.getInstance().getPlayer());

        slidingPager.setAdapter(new SongPagerAdapter(this));

        slidingPager.setPageTransformer(true, new DepthPageTransformer());

        slidingPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                currentSelectedSong = position;

                if (DemoPlayer.getInstance().isPlaying()) {

                    //Update dashBoardControls with new song Info
                    dashBoardControls.updateSongInfo(
                            DemoDataProvider.getInstance().getSongName(position),
                            DemoDataProvider.getInstance().getAlbumName(position),
                            DemoDataProvider.getInstance().getSongID(position));


                    //Play selected song
                    if (!DemoPlayer.getInstance().isPlaying() && IS_SONG_PAUSED) {
                        //animate play song animation if UI was in pause state
                        dashBoardControls.animatePlay();
                    }

                    playThisSong();

                    //Start Progress bar update
                    songProgressHandler.postDelayed(updateProgressTask, DELAY_MILLIS);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //Lets begin with 0
        slidingPager.setCurrentItem(0, true);
    }

    /**
     * Start Playaback
     */
    private void playThisSong() {

        if (IS_SONG_PAUSED) {

            DemoPlayer.getInstance().resume();
        } else {

            DemoPlayer.getInstance().play(DemoDataProvider.getInstance().getSongPath(currentSelectedSong), PlayerActivity.this);
        }

        IS_SONG_PAUSED = false;
    }

    private void playNextSong() {

        //if nth song completed play n+1th till last song come , If last song came play 0th
        if (currentSelectedSong < DemoDataProvider.getInstance().getSongCount() - 1) {
            ++currentSelectedSong;
        } else {
            currentSelectedSong = 0;
        }

        slidingPager.setCurrentItem(currentSelectedSong, true);
        DemoPlayer.getInstance().play(DemoDataProvider.getInstance().getSongPath(currentSelectedSong), PlayerActivity.this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                switch (currentVisualizer) {
                    case BAR:
                        dashBoardControls.clearVisualizer();
                        dashBoardControls.useCircleVisualizer();
                        currentVisualizer = VisualizerMode.CIRCLE;
                        break;

                    case CIRCLE:
                        dashBoardControls.clearVisualizer();
                        dashBoardControls.useCircleBarVisualizer();
                        currentVisualizer = VisualizerMode.CIRCLE_BAR;
                        break;

                    case CIRCLE_BAR:
                        dashBoardControls.clearVisualizer();
                        dashBoardControls.useLineVisualizer();
                        currentVisualizer = VisualizerMode.LINE;

                        break;

                    case LINE:
                        dashBoardControls.clearVisualizer();
                        dashBoardControls.useBarVisualizer();
                        currentVisualizer = VisualizerMode.BAR;
                        break;
                }
                return true;

            case R.id.action_fork:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/hiteshsahu/Bottom-Music-Player-Dashboard-Concept")));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.

                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Pause Playaback
     */
    private void pauseThisSong() {

        DemoPlayer.getInstance().pause();

        IS_SONG_PAUSED = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Important dont forget to clean visualizer and free up resouce
        //Also it will crash on orientation change if you dont free up visualizer
        dashBoardControls.cleanUp();
    }


}
