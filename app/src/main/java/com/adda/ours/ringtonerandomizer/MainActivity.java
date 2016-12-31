package com.adda.ours.ringtonerandomizer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;
    private static final int SELECTED_A_FILE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        code to choose files
        Button myButton = (Button) findViewById(R.id.add_tone);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, SELECTED_A_FILE);
//        code to choose files ends
        Ringtone defaultRingtone = RingtoneManager.getRingtone(getApplicationContext(),
                Settings.System.DEFAULT_RINGTONE_URI);
        defaultRingtone.play();
//        checking for permissions
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            
        }
        else {
            System.out.println("hello");
	    listMedia();
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
		listMedia();

            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
            return;
        }

	    // other 'case' lines to check for other
	    // permissions this app might request
	}
    }

    private void listMedia() {
	
	// permission was granted, yay! Do the
	// contacts-related task you need to do.
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
	System.out.println("SAMPATH");
	while (cursor.moveToNext()) {
	    // Extract the song title.
	    String title = cursor.getString(titleIdx);
	    System.out.println(title);
	    // Extract the album name.
	    String album = cursor.getString(albumIdx);
	    System.out.println("Sampath " + album);
	    result[cursor.getPosition()] = title + " (" + album + ")";
	}
	// Close the Cursor.
	cursor.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	// Check which request we're responding to
	if (requestCode == SELECTED_A_FILE) {
	    // Make sure the request was successful
	    if (resultCode == RESULT_OK) {
		System.out.println(data);
	    }
	}
    }
}
