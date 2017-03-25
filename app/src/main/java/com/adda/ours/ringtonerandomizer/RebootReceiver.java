package com.adda.ours.ringtonerandomizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class RebootReceiver extends BroadcastReceiver {

    private static final String APP_PREFS = "AppPrefs";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, CallDetectService.class);
        context.startService(startServiceIntent);
        String buttonText = context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)
                .getString("buttonText", RRConstants.OFF_STATE_TEXT);
        if(buttonText.equals(RRConstants.ON_STATE_TEXT)) {
//            Intent startServiceIntent = new Intent(context, CallDetectService.class);
//            context.startService(startServiceIntent);
        }
    }
}
