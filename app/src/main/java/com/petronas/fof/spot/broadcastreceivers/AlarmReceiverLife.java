package com.petronas.fof.spot.broadcastreceivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import com.petronas.fof.spot.services.ScanService;

import timber.log.Timber;

import static com.petronas.fof.spot.AppConstants.CHECK_FOREGROUND_SERVICE;


public class AlarmReceiverLife extends BroadcastReceiver {
    private static PowerManager.WakeLock wakeLock;

    private static final String TAG = "SPOTAlarmReceiverLife";
    static Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d( "Recurring alarm");

        // get data
        String departmentName = intent.getStringExtra("departmentName");
        String deviceName = intent.getStringExtra("userName");
        String locationName = intent.getStringExtra("locationName");
        String serverAddress = intent.getStringExtra("serverAddress");
        boolean allowGPS = intent.getBooleanExtra("allowGPS",true);
        boolean serviceLearning = intent.getBooleanExtra("serviceLearning",false);
        Timber.d("familyName: "+ departmentName);

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FOF.SPOT:AlarmWakeLock");
        wakeLock.acquire();
        Intent scanService = new Intent(context, ScanService.class);
        scanService.putExtra("departmentName",departmentName);
        scanService.putExtra("userName",deviceName);
        scanService.putExtra("locationName",locationName);
        scanService.putExtra("serverAddress",serverAddress);
        scanService.putExtra("allowGPS",allowGPS);
        scanService.putExtra("serviceLearning",serviceLearning);
        scanService.setAction(CHECK_FOREGROUND_SERVICE);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Timber.d("running startForegroundService");
                context.startForegroundService(scanService);
            } else {
                Timber.d("running legacy startService");
                context.startService(scanService);
            }            Timber.d("Inside alarmReceiver, acquired wakelock!");
        } catch (Exception e) {
            Timber.d(e.toString());
        }
        Timber.d("Releasing wakelock");
        if (wakeLock != null) wakeLock.release();
        wakeLock = null;
    }


}
