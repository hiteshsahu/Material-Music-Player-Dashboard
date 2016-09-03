/**
 *
 */
package demo.utils;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * @author Hitesh
 */
public class PreferenceHelper {


    public static final String SUFFLE_ENABLE = "SuffleSongs";
    public static final String LOOP_SONG = "LoopThisSong";

    public static final String SONG_LIST_MODE = "showSongGrid";
    public static final String ALBUM_LIST_MODE = "showAlbumGrid";
    public static final String ARTIST_LIST_MODE = "showArtistGrid";

    public static final String FIRST_TIME = "FirstTime";
    public static final String WHATS_NEW_LAST_SHOWN = "whats_new_last_shown";

    // Audio prefernces
    public static final String ECHO_CANCELER = "AcousticEchoCanceler";
    public static final String AUTO_GAIN = "AutomaticGainControl";
    public static final String NOISE_SUPPPRESSOR = "NoiseSuppressor";
    public static final String BASS_BOOST = "BassBoost";
    public static final String VIRTUALIZER = "Virtualizer";
    public static final String EQUILIZER = "Equilizer";

    // View Preferences
    public static final String SONG_LIST_ANIMATION = "SongListAnime";
    public static final String ALBUM_LIST_ANIMATION = "AlbumListAnime";
    public static final String ARTIST_LIST_ANIMATION = "ArtistListAnime";
    public static final String VIEW_PAGER_ANIME = "PagerAnime";
    public static final String PLAY_LIST_ANIMATION = "PlayListAnime";
    public static final String GENRE_LIST_ANIMATION = "GenreListAnim";
    public static final String PLAYER_COLOR = "playerColor";
    public static final String PLAYER_BACKGROUND = "PlayerBackground";

    // Hardware preferences
    public static final String START_PLUGED_EARPHONE = "START_PLUGED_EARPHONE";
    public static final String STOP_UNPLUG_EARPHONE = "STOP_UNPLUG_EARPHONE";
    public static final String START_PLUGED_BLUETOOTH = "START_PLUGED_BLUETOOTH";
    public static final String STOP_UNPLUG_BLUETOOTH = "STOP_UNPLUG_BLUETOOTH";
    public static final String VIBRATE = "VIBRATE";

    public static final String SUBMIT_LOGS = "CrashLogs";
    public static final String SORT_ORDER = "SongSortingOrder";

    private PreferenceHelper() {
    }

    private static PreferenceHelper preferenceHelperInstance = new PreferenceHelper();

    public void init(Context context) {

        if (!getPrefernceHelperInstace().getBoolean(context,
                FIRST_TIME, false)) {
            setBoolean(context, FIRST_TIME, true);
            setBoolean(context, SUBMIT_LOGS, true);
        }
    }

    public static PreferenceHelper getPrefernceHelperInstace() {

        return preferenceHelperInstance;
    }

    public void setBoolean(Context appContext, String key, Boolean value) {

        PreferenceManager.getDefaultSharedPreferences(appContext).edit()
                .putBoolean(key, value).apply();
    }

    public void setInteger(Context appContext, String key, int value) {

        PreferenceManager.getDefaultSharedPreferences(appContext).edit()
                .putInt(key, value).apply();
    }

    public void setFloat(Context appContext, String key, float value) {

        PreferenceManager.getDefaultSharedPreferences(appContext).edit()
                .putFloat(key, value).apply();
    }

    public void setString(Context appContext, String key, String value) {

        PreferenceManager.getDefaultSharedPreferences(appContext).edit()
                .putString(key, value).apply();
    }

    // To retrieve values from shared preferences:

    public boolean getBoolean(Context appContext, String key,
                              Boolean defaultValue) {

        return PreferenceManager.getDefaultSharedPreferences(appContext)
                .getBoolean(key, defaultValue);
    }

    public int getInteger(Context appContext, String key, int defaultValue) {

        return PreferenceManager.getDefaultSharedPreferences(appContext)
                .getInt(key, defaultValue);
    }

    public float getString(Context appContext, String key, float defaultValue) {

        return PreferenceManager.getDefaultSharedPreferences(appContext)
                .getFloat(key, defaultValue);
    }

    public String getString(Context appContext, String key, String defaultValue) {

        return PreferenceManager.getDefaultSharedPreferences(appContext)
                .getString(key, defaultValue);
    }
}
