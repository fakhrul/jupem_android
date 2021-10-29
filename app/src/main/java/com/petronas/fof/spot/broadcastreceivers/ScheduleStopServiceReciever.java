package com.petronas.fof.spot.broadcastreceivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.petronas.fof.spot.utilities.Scheduler;
import com.petronas.fof.spot.services.ScanService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;

import static com.petronas.fof.spot.AppConstants.SHARED_PREF_NAME;
import static com.petronas.fof.spot.AppConstants.STOP_FOREGROUND_SERVICE;


/**
 *
 */
public class ScheduleStopServiceReciever extends BroadcastReceiver {
    int present_day;
    public final Calendar cal = Calendar.getInstance();
    String weekdays,hour;
    Context mContext;
    private SharedPreferences sharedPref;
    private Scheduler scheduler;
    @Override
    public void onReceive(Context context, Intent intent) {
        scheduler =new Scheduler(context);
        mContext = context;

        try {
            sharedPref = EncryptedSharedPreferences.create(
                    SHARED_PREF_NAME,
                    MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException ignored) {
        }

        weekdays = intent.getStringExtra("alarmDay");
        hour = intent.getStringExtra("hour");
        present_day = cal.get(Calendar.DAY_OF_WEEK);
        SharedPreferences.Editor editor =  sharedPref.edit();
        editor.putBoolean("serviceLearning", false);
        editor.commit();
        Intent scanService = new Intent(context, ScanService.class);
        scanService.setAction(STOP_FOREGROUND_SERVICE);
        context.startService(scanService);
        scheduler.repeatAlarmToStopService(Integer.parseInt(weekdays),Integer.parseInt(hour));
    }
}

