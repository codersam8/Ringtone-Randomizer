package com.adda.ours.ringtonerandomizer;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.UserDictionary;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Cursor mCursor;
    private ListView ringtonesList;
    private ArrayAdapter<String> arrayAdapter;
    private Button RandomizeTonesToggler;
    private SharedPreferences songsList;
    private SharedPreferences.Editor sonsListEditor;
    private SharedPreferences appPrefs;
    private SharedPreferences.Editor appPrefsEditor;

    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;
    private static final int MY_PERMISSIONS_WRITE_SETTINGS = 2;
    private static final int SELECTED_A_FILE = 1;
    private static final String ON_STATE_TEXT = "Stop";
    private static final String OFF_STATE_TEXT = "Randomize Tones";
    private static final String SONGS_LIST = "SongsList";
    private static final String APP_PREFS = "AppPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //        getMediaCursor();

        checkForWriteSettingsPermsn();
        appPrefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        appPrefsEditor = appPrefs.edit();
        RandomizeTonesToggler = (Button) findViewById(R.id.toggle_randomizing_tones);
        setButtonText();
        RandomizeTonesToggler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCallDetectService();
            }
        });

        songsList = getSharedPreferences(SONGS_LIST, MODE_PRIVATE);
        sonsListEditor = songsList.edit();
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.a_ringtone_layout);
        Button addTone = (Button) findViewById(R.id.add_tone);
        addTone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkForPermissions();
            }
        });

        listSavedTones();
    }

    private void updateValues(String key, String value) {
        sonsListEditor.putString(key, value);
        sonsListEditor.commit();
    }

    private void setButtonText() {
        if (appPrefs == null) {
            RandomizeTonesToggler.setText(OFF_STATE_TEXT);
            updatePrefs("buttonText", OFF_STATE_TEXT);
        } else {
            RandomizeTonesToggler.setText(appPrefs.getString("buttonText", OFF_STATE_TEXT));
        }
    }

    private void updatePrefs(String key, String value) {
        appPrefsEditor.putString(key, value);
        appPrefsEditor.commit();
    }

    private void toggleCallDetectService() {
        Intent intent = new Intent(this, CallDetectService.class);
        if (appPrefs.getString("buttonText", OFF_STATE_TEXT).equals(OFF_STATE_TEXT)) {
            startService(intent);
            RandomizeTonesToggler.setText(ON_STATE_TEXT);
            updatePrefs("buttonText", ON_STATE_TEXT);
        } else {
            stopService(intent);
            RandomizeTonesToggler.setText(OFF_STATE_TEXT);
            updatePrefs("buttonText", OFF_STATE_TEXT);
        }
    }

    private void stopCallDetectService() {
        Intent intent = new Intent(this, CallDetectService.class);
        stopService(intent);
    }

    private void listSavedTones() {
        ringtonesList = (ListView) findViewById(R.id.ringtones_list);
        Map<String, ?> songsKeyVal = songsList.getAll();

        for (Map.Entry<String, ?> entry : songsKeyVal.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            arrayAdapter.add(entry.getKey());
        }
        ringtonesList.setAdapter(arrayAdapter);
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

    private void checkForWriteSettingsPermsn() {
//        if(! Settings.System.canWrite(this)) {
//            ActivityCompat.requestPermissions(this,
//                    new String[] {Manifest.permission.WRITE_SETTINGS},
//                    MY_PERMISSIONS_WRITE_SETTINGS);
//        }
        boolean permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(this);
        } else {
            permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }
        if (permission) {
            //do your code
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                this.startActivityForResult(intent, MainActivity.MY_PERMISSIONS_WRITE_SETTINGS);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_SETTINGS}, MainActivity.MY_PERMISSIONS_WRITE_SETTINGS);
            }
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
            case MY_PERMISSIONS_WRITE_SETTINGS: {

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

    private void persistSong(String title, Uri uri) {
        Log.i(TAG, "title " + title);
        if(wasSongAlreadyAdded(title)) {
            Toast.makeText(this, "Tone already exists!", Toast.LENGTH_SHORT).show();
        } else {
            arrayAdapter.add(title);
            sonsListEditor.putString(title, uri.toString());
            sonsListEditor.commit();
        }
    }

    private boolean wasSongAlreadyAdded(String title) {
        return songsList.contains(title);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SELECTED_A_FILE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                Log.i(TAG, "Selected file " + data.getData());
//                playASong(data.getData());
                String[] projection = new String[]{
                        MediaStore.Audio.AudioColumns.TITLE};
                Cursor cursor = getContentResolver().query(
                        uri,
                        projection,
                        null,
                        null,
                        null);
                while (cursor.moveToNext()) {
                    persistSong(cursor.getString(0), uri);
                }
            }
        } else {

        }
    }
}
