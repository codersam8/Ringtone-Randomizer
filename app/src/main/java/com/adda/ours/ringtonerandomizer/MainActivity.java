package com.adda.ours.ringtonerandomizer;

import android.media.Ringtone;
import android.media.RingtoneManager;
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
    }
}
