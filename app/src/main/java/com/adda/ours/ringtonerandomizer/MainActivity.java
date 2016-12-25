package com.adda.ours.ringtonerandomizer;

import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Ringtone defaultRingtone = RingtoneManager.getRingtone(getApplicationContext(),
                Settings.System.DEFAULT_RINGTONE_URI);
        defaultRingtone.play();
//        test code for projection
        String[] projection = new String[] {
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.TITLE };
        Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = getContentResolver().query(contentUri,
                projection, null, null, null);
        // Get the index of the columns we need.
        int albumIdx = cursor
                .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM);
        int titleIdx = cursor
                .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE);
        // Create an array to store the result set.
        String[] result = new String[cursor.getCount()];
                /*
                 * Iterate over the Cursor, extracting each album name and song
                 * title.
                 */
        while (cursor.moveToNext()) {
            // Extract the song title.
            String title = cursor.getString(titleIdx);
            // Extract the album name.
            String album = cursor.getString(albumIdx);
            result[cursor.getPosition()] = title + " (" + album + ")";
        }
        // Close the Cursor.
        cursor.close();

    }
}
