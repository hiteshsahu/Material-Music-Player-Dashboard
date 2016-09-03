package demo.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.hiteshsahu.musicplayerconcept.visualizer.utils.PlayerUtil;

import java.util.ArrayList;

/**
 * Created by 663918 on 8/31/2016.
 */
public class DemoDataProvider {

    private static DemoDataProvider testDataHoldewr;

    private DemoDataProvider() {
    }

    private ArrayList<Songs> songList = new ArrayList<>();

    public static DemoDataProvider getInstance() {
        if (null == testDataHoldewr) {
            testDataHoldewr = new DemoDataProvider();
        }
        return testDataHoldewr;
    }

    public String getSongPath(int position) {
        return songList.get(position).getFullPath();
    }

    public long getSongID(int position) {
        return songList.get(position).getSongID();
    }

    public String getSongName(int position) {
        return songList.get(position).getSongName();
    }

    public String getAlbumName(int position) {
        return songList.get(position).getAlbumName();
    }

    public int getSongCount() {
        return songList.size();
    }

    /**
     * It Not used AnyWhere in Project but it's Helpful while you need to get all mp3 file in sdcard.
     * it's nothing but just for future used
     */
    public void getAllSongs(Context context) {

        Uri allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] STAR = null;
        Cursor cursor = context.getContentResolver().query(allsongsuri, STAR, selection, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String song_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    int song_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

                    String fullpath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String Duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                    songList.add(new Songs(
                            cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                            cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))));


                } while (cursor.moveToNext());

            }
            cursor.close();
        }
    }

    private class Songs {

        public String getSongName() {
            return songName;
        }

        public String getAlbumName() {
            return albumName;
        }

        public String getFullPath() {
            return fullPath;
        }

        public String getSongLength() {
            return PlayerUtil.milliSecondsToTimer(songLength);
        }

        public long getSongID() {
            return songID;
        }

        public Songs(long songID, String songName, String albumName, String fullPath, long songLength) {
            this.songID = songID;
            this.songName = songName;
            this.albumName = albumName;
            this.fullPath = fullPath;
            this.songLength = songLength;
        }


        private long songID;
        private String songName;
        private String albumName;
        private String fullPath;
        private long songLength;


    }
}
