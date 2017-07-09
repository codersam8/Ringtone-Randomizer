package com.adda.ours.ringtonerandomizer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ListView ringtonesList;
    private SelectionAdapter arrayAdapter;
    private Button randomizeTonesToggler;
    private SharedPreferences songsList;
    private SharedPreferences.Editor sonsListEditor;
    private SharedPreferences appPrefs;
    private SharedPreferences.Editor appPrefsEditor;

    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;
    private static final int SELECTED_A_FILE = 1;
    private static final int MY_PERMISSIONS_WRITE_SETTINGS = 2;
    private static final int MY_PERMISSIONS_READ_PHONE_STATE = 3;

    private static final String SONGS_LIST = "SongsList";
    private static final String APP_PREFS = "AppPrefs";
    private static final String BUTTON_TEXT = "buttonText";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkIfHasReadPhoneState();
        ringtonesList = (ListView) findViewById(R.id.ringtones_list);
        appPrefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        appPrefsEditor = appPrefs.edit();
        randomizeTonesToggler = (Button) findViewById(R.id.toggle_randomizing_tones);
        setButtonTextAndColor();
        randomizeTonesToggler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkIfWriteSettingsPrmsnGranted()) {
                    toggleCallDetectService();
                } else {
                    requestForWriteSettingPrmsn();
                }
            }
        });

        songsList = getSharedPreferences(SONGS_LIST, MODE_PRIVATE);
        sonsListEditor = songsList.edit();
        arrayAdapter = new SelectionAdapter(this, R.layout.a_ringtone_layout);
        Button addTone = (Button) findViewById(R.id.add_tone);
        addTone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkForPermissions();
            }
        });


        listSavedTones();
        ringtonesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        ringtonesList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
                if (checked) {
                    arrayAdapter.setNewSelection(position, checked);
                } else {
                    arrayAdapter.removeSelection(position);
                }
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.delete_tones:
                        arrayAdapter.deleteSelectedItems();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu_delete, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                arrayAdapter.clearSelection();
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
        });
    }

//    private void updateValues(String key, String value) {
//        sonsListEditor.putString(key, value);
//        sonsListEditor.commit();
//    }

    private void checkIfHasReadPhoneState() {
        int perm = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE);
        Log.i(TAG, "Granted " + perm);
        if (ContextCompat.checkSelfPermission(this,
                                              Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                                              new String[] {Manifest.permission.READ_PHONE_STATE},
                                              MY_PERMISSIONS_READ_PHONE_STATE);
        }
    }
    private void setButtonTextAndColor() {
        if (appPrefs == null) {
            randomizeTonesToggler.setText(RRConstants.OFF_STATE_TEXT);
            randomizeTonesToggler.setBackgroundColor(ContextCompat.getColor(this, RRConstants.OFF_STATE_COLOR));
            updatePrefs(BUTTON_TEXT, RRConstants.OFF_STATE_TEXT);
        } else {
            String buttonText = appPrefs.getString(BUTTON_TEXT, RRConstants.OFF_STATE_TEXT);
            randomizeTonesToggler.setText(buttonText);
            if(buttonText.equals(RRConstants.OFF_STATE_TEXT)) {
                randomizeTonesToggler.setBackgroundColor(ContextCompat.getColor(this, RRConstants.OFF_STATE_COLOR));
            } else {
                randomizeTonesToggler.setBackgroundColor(ContextCompat.getColor(this, RRConstants.ON_STATE_COLOR));
            }
        }
    }

    private void updatePrefs(String key, String value) {
        appPrefsEditor.putString(key, value);
        appPrefsEditor.commit();
    }

    private void toggleCallDetectService() {
        Intent intent = new Intent(this, CallDetectService.class);
        if (appPrefs.getString(BUTTON_TEXT, RRConstants.OFF_STATE_TEXT).equals(RRConstants.OFF_STATE_TEXT)) {
            startService(intent);
            randomizeTonesToggler.setText(RRConstants.ON_STATE_TEXT);
            randomizeTonesToggler.setBackgroundColor(ContextCompat.getColor(this, RRConstants.ON_STATE_COLOR));
            updatePrefs(BUTTON_TEXT, RRConstants.ON_STATE_TEXT);
        } else {
            stopService(intent);
            randomizeTonesToggler.setText(RRConstants.OFF_STATE_TEXT);
            randomizeTonesToggler.setBackgroundColor(ContextCompat.getColor(this, RRConstants.OFF_STATE_COLOR));
            updatePrefs(BUTTON_TEXT, RRConstants.OFF_STATE_TEXT);
        }
    }

//    private void stopCallDetectService() {
//        Intent intent = new Intent(this, CallDetectService.class);
//        stopService(intent);
//    }

    private void listSavedTones() {
        Map<String, ?> songsKeyVal = songsList.getAll();

        for (Map.Entry<String, ?> entry : songsKeyVal.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            arrayAdapter.add(entry.getKey());
        }
        ringtonesList.setAdapter(arrayAdapter);
    }

//    private Cursor getMediaCursor() {
//        String[] mProjection =
//                {
//                        MediaStore.Audio.Media._ID,
//                        MediaStore.Audio.Media.ALBUM
//                };
//        String mSelectionClause = null;
//        String[] mSelectionArgs = null;
//        //        mSelectionArgs[0] = "";
//        String mSortOrder = null;
//        Log.i(TAG, "URI " + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
//        mCursor = getContentResolver().query(
//                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                mProjection,
//                mSelectionClause,
//                mSelectionArgs,
//                mSortOrder);
//        if (null == mCursor) {
//        } else if (mCursor.getCount() < 1) {
//        } else {
//            int limit = 1;
//            while (mCursor.moveToNext() && limit > 0) {
//                Log.i(TAG, "title " + mCursor.getString(0));
//                Log.i(TAG, "album " + mCursor.getString(1));
//            }
//        }
//        return mCursor;
//    }

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


    private void requestForWriteSettingPrmsn() {
//        if(! Settings.System.canWrite(this)) {
//            ActivityCompat.requestPermissions(this,
//                    new String[] {Manifest.permission.WRITE_SETTINGS},
//                    MY_PERMISSIONS_WRITE_SETTINGS);
//        }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                this.startActivityForResult(intent, MainActivity.MY_PERMISSIONS_WRITE_SETTINGS);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_SETTINGS}, MY_PERMISSIONS_WRITE_SETTINGS);
            }

    }

    private boolean checkIfWriteSettingsPrmsnGranted() {
        boolean permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(this);
        } else {
            permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }
        return permission;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseFile();
                }
                break;

            case MY_PERMISSIONS_WRITE_SETTINGS:
                toggleCallDetectService();
                break;
            case MY_PERMISSIONS_READ_PHONE_STATE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "READ_PHONE_STATE granted");
                }
        }
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        startActivityForResult(intent, SELECTED_A_FILE);
    }

    private void persistSong(String title, Uri uri) {
        Log.i(TAG, "title " + title);
//        TODO: solve this properly
        if(title == null) {
            return;
        }
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
        switch (requestCode) {
            case SELECTED_A_FILE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    Log.i(TAG, "Selected file " + data.getData());
//                playASong(data.getData());
                    Cursor cursor = getContentResolver().query(
                            uri,
                            null,
                            null,
                            null,
                            null);
                    while (cursor.moveToNext()) {
                        String[] allColumns = cursor.getColumnNames();
                        if(Arrays.asList(allColumns).contains(MediaStore.Audio.AudioColumns.TITLE)) {
                            persistSong(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE)), uri);
                        } else {
                            persistSong(cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)), uri);
                        }
                        Log.i(TAG, Arrays.toString(allColumns));

                    }
                }
                break;
            case MY_PERMISSIONS_WRITE_SETTINGS:
                if (Settings.System.canWrite(this)) {
                    toggleCallDetectService();
                }
                break;
        }
    }

    private class SelectionAdapter extends ArrayAdapter<String> {
        private Context context;

        private HashMap<Integer, Boolean> mSelection = new HashMap<>();

        public SelectionAdapter(Context context, int resource) {

            super(context, resource);
            this.context = context;
        }

        public void deleteSelectedItems() {
            Iterator it = mSelection.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry keyVal = (Map.Entry) it.next();
                if((Boolean)keyVal.getValue()) {
                    String title = getItem((Integer)keyVal.getKey());
                    arrayAdapter.remove(title);
                    sonsListEditor.remove(title);
                    Log.i(TAG, title);
                }
            }
            sonsListEditor.commit();
            notifyDataSetChanged();
        }

        public void setNewSelection(int position, boolean value) {
            mSelection.put(position, value);
            notifyDataSetChanged();
        }

        public boolean isPositionChecked(int position) {
            Boolean result = mSelection.get(position);
            return result == null ? false : result;
        }

        public Set<Integer> getCurrentCheckedPosition() {
            return mSelection.keySet();
        }

        public void removeSelection(int position) {
            mSelection.remove(position);
            notifyDataSetChanged();
        }

        public void clearSelection() {
            mSelection = new HashMap<>();
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);//let the adapter handle setting up the row views
            v.setBackgroundColor(ContextCompat.getColor(context,android.R.color.white)); //default color

            if (mSelection.get(position) != null) {
                v.setBackgroundColor(ContextCompat.getColor(context,android.R.color.holo_blue_light));// this is a selected position so make it red
            }
            return v;
        }
    }
}
