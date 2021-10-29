package com.petronas.fof.spot.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.petronas.fof.spot.AppConstants;
import com.petronas.fof.spot.utilities.Scheduler;
import com.petronas.fof.spot.services.ScanService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import timber.log.Timber;

import static com.petronas.fof.spot.AppConstants.FRIDAY_START_TIME_HOUR;
import static com.petronas.fof.spot.AppConstants.FRIDAY_STOP_TIME_HOUR;
import static com.petronas.fof.spot.AppConstants.MONDAY_START_TIME_HOUR;
import static com.petronas.fof.spot.AppConstants.MONDAY_STOP_TIME_HOUR;
import static com.petronas.fof.spot.AppConstants.SATURDAY_START_TIME_HOUR;
import static com.petronas.fof.spot.AppConstants.SATURDAY_STOP_TIME_HOUR;
import static com.petronas.fof.spot.AppConstants.SHARED_PREF_NAME;
import static com.petronas.fof.spot.AppConstants.SUNDAY_START_TIME_HOUR;
import static com.petronas.fof.spot.AppConstants.SUNDAY_STOP_TIME_HOUR;
import static com.petronas.fof.spot.AppConstants.THURSDAY_START_TIME_HOUR;
import static com.petronas.fof.spot.AppConstants.THURSDAY_STOP_TIME_HOUR;
import static com.petronas.fof.spot.AppConstants.TUESDAY_START_TIME_HOUR;
import static com.petronas.fof.spot.AppConstants.TUESDAY_STOP_TIME_HOUR;
import static com.petronas.fof.spot.AppConstants.WEDNESDAY_START_TIME_HOUR;
import static com.petronas.fof.spot.AppConstants.WEDNESDAY_STOP_TIME_HOUR;

public class DeviceBootReceiver extends BroadcastReceiver {
    private static final String TAG = "SPOTDeviceBootReceiver";
    SharedPreferences sharedPref;
    private static PowerManager.WakeLock wakeLock;
    private Scheduler scheduler;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Timber.d("android.intent.action.BOOT_COMPLETED received");
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FOF.SPOT:BootWakeLock");
            wakeLock.acquire();

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
            Timber.d("Preferences values:");
            Timber.d("departmentName: "+sharedPref.getString("departmentName",""));
            Timber.d("userName: "+sharedPref.getString("userName",""));
            Timber.d("serverAddress: "+sharedPref.getString("serverAddress",""));
            
            // check serviceState before shutdown
            int serviceState = sharedPref.getInt("serviceState", 1);
            // if service was running >> restart the service
            if (serviceState==1){
                Intent actionIntent = new Intent(context, ScanService.class);
                actionIntent.putExtra("source","deviceBoot");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Timber.d("running startForegroundService");
                    context.startForegroundService(actionIntent);
//                context.startService(actionIntent);
                } else {
                    Timber.d("running legacy startService");
                    context.startService(actionIntent);
                }
                Toast.makeText(context, "Started SPOT app service", Toast.LENGTH_SHORT).show();
            }
            else {
                Timber.d("SPOT service was not running before device reboot. Ignoring.");
            }

            // Rescheduling the alarm on reboot
            if (sharedPref.getBoolean(AppConstants.SUNDAY,false)){
                scheduler.saveAlarmToStartService(1,false,sharedPref.getInt(SUNDAY_START_TIME_HOUR,0));
                scheduler.saveAlarmToStopService(1,false,sharedPref.getInt(SUNDAY_STOP_TIME_HOUR,0));
            }
            if (sharedPref.getBoolean(AppConstants.MONDAY,false)){
                scheduler.saveAlarmToStartService(2,false,sharedPref.getInt(MONDAY_START_TIME_HOUR,0));
                scheduler.saveAlarmToStopService(2,false,sharedPref.getInt(MONDAY_STOP_TIME_HOUR,0));
            }
            if (sharedPref.getBoolean(AppConstants.TUESDAY,false)){
                scheduler.saveAlarmToStartService(3,false,sharedPref.getInt(TUESDAY_START_TIME_HOUR,0));
                scheduler.saveAlarmToStopService(3,false,sharedPref.getInt(TUESDAY_STOP_TIME_HOUR,0));
            }
            if (sharedPref.getBoolean(AppConstants.WEDNESDAY,false)){
                scheduler.saveAlarmToStartService(4,false,sharedPref.getInt(WEDNESDAY_START_TIME_HOUR,0));
                scheduler.saveAlarmToStopService(4,false,sharedPref.getInt(WEDNESDAY_STOP_TIME_HOUR,0));
            }
            if (sharedPref.getBoolean(AppConstants.THURSDAY,false)){
                scheduler.saveAlarmToStartService(5,false,sharedPref.getInt(THURSDAY_START_TIME_HOUR,0));
                scheduler.saveAlarmToStopService(5,false,sharedPref.getInt(THURSDAY_STOP_TIME_HOUR,0));
            }
            if (sharedPref.getBoolean(AppConstants.FRIDAY,false)){
                scheduler.saveAlarmToStartService(6,false,sharedPref.getInt(FRIDAY_START_TIME_HOUR,0));
                scheduler.saveAlarmToStopService(6,false,sharedPref.getInt(FRIDAY_STOP_TIME_HOUR,0));
            }
            if (sharedPref.getBoolean(AppConstants.SATURDAY,false)){
                scheduler.saveAlarmToStartService(7,false,sharedPref.getInt(SATURDAY_START_TIME_HOUR,0));
                scheduler.saveAlarmToStopService(7,false,sharedPref.getInt(SATURDAY_STOP_TIME_HOUR,0));
            }
            Timber.d("Releasing wakelock");
            if (wakeLock != null) wakeLock.release();
            wakeLock = null;
            /* Setting the alarm here */
//            Intent alarmIntent = new Intent(context, ScanService.class);
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
//            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//            int interval = 8000;
//            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);


        }

    }
}
