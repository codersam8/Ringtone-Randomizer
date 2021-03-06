package com.adda.ours.ringtonerandomizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

/**
 * Helper class to detect incoming and outgoing calls.
 *
 * @author Moskvichev Andrey V.
 */
public class CallHelper {
    private static final String TAG = "CallHelper";
    /**
     * Listener to detect incoming calls.
     */
    private class CallStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    // called when someone is ringing to this phone
                    chooseARandomTone();
                    break;
            }
        }

        private void chooseARandomTone() {
            songsList = ctx.getSharedPreferences("SongsList", ctx.MODE_PRIVATE);
            Map<String, String> songsMap = (Map<String, String>)songsList.getAll();
            Collection<String> songsColl = songsMap.values();
            String[] songListArr = new String[songsColl.size()];
            songsColl.toArray(songListArr);

            Uri ringToneUri;
            Log.i(TAG, "Previous tone was " + RingtoneManager.getActualDefaultRingtoneUri(ctx, RingtoneManager.TYPE_RINGTONE));
            do {
                ringToneUri = Uri.parse(songListArr[new Random().nextInt(songListArr.length)]);
            } while (RingtoneManager.getActualDefaultRingtoneUri(ctx, RingtoneManager.TYPE_RINGTONE).equals(ringToneUri));
            Log.i(TAG, "Selected tone is " + ringToneUri);
            RingtoneManager.setActualDefaultRingtoneUri(ctx,
                    RingtoneManager.TYPE_RINGTONE,
                    ringToneUri);
        }
    }

    private SharedPreferences songsList;
    private Context ctx;
    private TelephonyManager tm;
    private CallStateListener callStateListener;

    public CallHelper(Context ctx) {
        this.ctx = ctx;

        callStateListener = new CallStateListener();
    }

    /**
     * Start calls detection.
     */
    public void start() {
        tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * Stop calls detection.
     */
    public void stop() {
        tm.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
    }

}
