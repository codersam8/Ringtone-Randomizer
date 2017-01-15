package com.adda.ours.ringtonerandomizer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.UserDictionary;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.test.mock.MockContentResolver;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;
    private static final int SELECTED_A_FILE = 1;
    private Cursor mCursor;

    private static final String PREFS_NAME = "SongsList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getMediaCursor();

        Button addTone = (Button) findViewById(R.id.add_tone);
        addTone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkForPermissions();
            }
        });
    }

    private void playRingtone() {
        Ringtone defaultRingtone = RingtoneManager.getRingtone(getApplicationContext(),
                Settings.System.DEFAULT_RINGTONE_URI);
        defaultRingtone.play();
    }

    private void playASong(Uri uri) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }

    private Cursor getMediaCursor() {
        String[] mProjection =
                {
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.ALBUM
                };
        String mSelectionClause = null;
        String[] mSelectionArgs = null;
//        mSelectionArgs[0] = "";
        String mSortOrder = null;
        Log.i(TAG, "URI " + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        mCursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mProjection,
                mSelectionClause,
                mSelectionArgs,
                mSortOrder);
        if (null == mCursor) {
        } else if (mCursor.getCount() < 1) {
        } else {
            int limit = 1;
            while (mCursor.moveToNext() && limit > 0) {
                Log.i(TAG, "title " + mCursor.getString(0));
                Log.i(TAG, "album " + mCursor.getString(1));
            }
        }
        return mCursor;
    }

    private void getWordCursor() {
        String[] mSelectionArgs = {""};
        String mSearchString = "";
        String mSelectionClause = null;
        if (TextUtils.isEmpty(mSearchString)) {
            mSelectionClause = null;
            mSelectionArgs[0] = "";
        } else {
            mSelectionClause = UserDictionary.Words.WORD + " = ?";
            mSelectionArgs[0] = mSearchString;
        }
        String[] mProjection =
                {
                        UserDictionary.Words._ID,    // Contract class constant for the _ID column name
                        UserDictionary.Words.WORD,   // Contract class constant for the word column name
                        UserDictionary.Words.LOCALE  // Contract class constant for the locale column name
                };
        String mSortOrder = null;
        mCursor = getContentResolver().query(
                UserDictionary.Words.CONTENT_URI,  // The content URI of the words table
                mProjection,                       // The columns to return for each row
                mSelectionClause,                   // Either null, or the word the user entered
                mSelectionArgs,                    // Either empty, or the string the user entered
                mSortOrder);                       // The sort order for the returned rows
    }

    private void checkForPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //                        should show reason here
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        } else {
            chooseFile();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseFile();

                } else {
                }
                return;
            }
        }
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, SELECTED_A_FILE);

        String[] projection = new String[]{
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.TITLE};
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
        cursor.close();
    }

    private void persistSong(Uri uri) {
	String[] projection = new String[] {
	    MediaStore.Audio.AudioColumns.TITLE};
	Cursor cursor = getContentResolver().query(
						   uri,
						   projection,
						   null,
						   null,
						   null);
	while(cursor.moveToNext()) {
	    Log.i(TAG, "Title " + cursor.getString(0));
	}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SELECTED_A_FILE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Selected file " + data.getData());
                playASong(data.getData());
		persistSong(data.getData());
            }
        }
    }
}
