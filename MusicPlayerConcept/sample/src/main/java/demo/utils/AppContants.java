package demo.utils;

import android.net.Uri;
import android.os.Environment;

/**
 * Created by Hitesh on 23-07-2016.
 */
public interface AppContants {

    final public static Uri sArtworkUri = Uri
            .parse("content://media/external/audio/albumart");

    public static final String RECORDING_PATH = Environment
            .getExternalStorageDirectory() + "/Recordings";

}
