package com.petronas.fof.spot.broadcastreceivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.petronas.fof.spot.AppConstants;
import com.petronas.fof.spot.utilities.Scheduler;
import com.petronas.fof.spot.services.ScanService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;

import static com.petronas.fof.spot.AppConstants.SHARED_PREF_NAME;

public class ScheduleReciever extends BroadcastReceiver {
    int present_day;
    public final Calendar cal = Calendar.getInstance();
    String dayOfTheWeek,hour;
    SharedPreferences sharedPref;

    @Override
    public void onReceive(Context context, Intent intent) {

        dayOfTheWeek = intent.getStringExtra(AppConstants.DAY_OF_THE_WEEK_EXTRA);
        hour = intent.getStringExtra(AppConstants.HOUR_EXTRA);
        present_day = cal.get(Calendar.DAY_OF_WEEK);

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

        SharedPreferences.Editor editor =  sharedPref.edit();
        editor.putBoolean("serviceLearning", true);
        editor.commit();

        Intent actionIntent = new Intent(context, ScanService.class);
        actionIntent.putExtra("source","scheduler");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(actionIntent);
        } else {
            context.startService(actionIntent);
        }

        // reminder set for next week
        Scheduler scheduler = new Scheduler(context);
        scheduler.repeatAlarmToStartService(Integer.parseInt(dayOfTheWeek),Integer.parseInt(hour));
    }
}

