package com.petronas.fof.spot.utilities;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.petronas.fof.spot.AppConstants;
import com.petronas.fof.spot.broadcastreceivers.ScheduleReciever;
import com.petronas.fof.spot.broadcastreceivers.ScheduleStopServiceReciever;

import java.util.Calendar;

import timber.log.Timber;

public class Scheduler {
    private final Context context;
    public Scheduler(Context context){
        this.context=context;
    }

    public void saveAlarmToStartService(int dayOfTheWeek, boolean isEditingAlarm, int hours){

        PendingIntent pendingIntent;
        Intent intent = new Intent(context, ScheduleReciever.class);
        intent.putExtra(AppConstants.DAY_OF_THE_WEEK_EXTRA, String.valueOf(dayOfTheWeek));
        intent.putExtra(AppConstants.HOUR_EXTRA, String.valueOf(hours));

        Calendar calender= Calendar.getInstance();
        calender.set(Calendar.DAY_OF_WEEK, dayOfTheWeek);
        calender.set(Calendar.HOUR_OF_DAY, hours);
        calender.set(Calendar.MINUTE, AppConstants.START_SERVICE_TIME_MIN);
        calender.set(Calendar.SECOND, 0);
        calender.set(Calendar.MILLISECOND, 0);

        Calendar now = Calendar.getInstance();
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (calender.before(now)) {
            calender.add(Calendar.DATE, 7);
            Timber.d("service start alarm scheduled for next week at %s", calender.getTime());
        }
        else {
            Timber.d("service start alarm scheduled for this week at  %s", calender.getTime());
        }

        if (isEditingAlarm){
            pendingIntent = PendingIntent.getBroadcast(context, dayOfTheWeek, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            pendingIntent = PendingIntent.getBroadcast(context, dayOfTheWeek, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        }
        else
            pendingIntent = PendingIntent.getBroadcast(context, dayOfTheWeek, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if(Build.VERSION.SDK_INT < 23){
            if(Build.VERSION.SDK_INT >= 19){
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntent);
            }
            else{
                alarmManager.set(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntent);
            }
        }
        else{
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntent);
        }
    }

    public void repeatAlarmToStartService(int weekNo,int hours){
        PendingIntent pendingIntent;
        Intent intent = new Intent(context, ScheduleReciever.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        intent.putExtra(AppConstants.DAY_OF_THE_WEEK_EXTRA, String.valueOf(weekNo));
        intent.putExtra(AppConstants.HOUR_EXTRA, String.valueOf(hours));

        Calendar calender= Calendar.getInstance();
        calender.set(Calendar.DAY_OF_WEEK, weekNo);  //here pass week number
        calender.set(Calendar.HOUR_OF_DAY, hours);  //pass hour which you have select
        calender.set(Calendar.MINUTE, AppConstants.START_SERVICE_TIME_MIN);  //pass min which you have select
        calender.set(Calendar.SECOND, 0);
        calender.set(Calendar.MILLISECOND, 0);
        calender.add(Calendar.DATE, 7);

        Timber.d("service start alarm repeated for next week at %s", calender.getTime());

            pendingIntent = PendingIntent.getBroadcast(context, weekNo, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            pendingIntent = PendingIntent.getBroadcast(context, weekNo, intent,PendingIntent.FLAG_UPDATE_CURRENT);

        if(Build.VERSION.SDK_INT < 23){
            if(Build.VERSION.SDK_INT >= 19){
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntent);
            }
            else{
                alarmManager.set(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntent);
            }
        }
        else{
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntent);
        }
    }

    public void saveAlarmToStopService(int dayOfTheWeek, boolean isEditingAlarm, int hours) {
        PendingIntent pendingIntent;
        Intent intent = new Intent(context, ScheduleStopServiceReciever.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        intent.putExtra(AppConstants.DAY_OF_THE_WEEK_EXTRA, String.valueOf(dayOfTheWeek));
        intent.putExtra(AppConstants.HOUR_EXTRA, String.valueOf(hours));
        int requestCodePendingIntent=dayOfTheWeek+AppConstants.REQUEST_STOP_SERVICE;
        Calendar calender= Calendar.getInstance();

        calender.set(Calendar.DAY_OF_WEEK, dayOfTheWeek);  //here pass week number
        calender.set(Calendar.HOUR_OF_DAY, hours);  //pass hour which you have select
        calender.set(Calendar.MINUTE, AppConstants.STOP_SERVICE_TIME_MIN);  //pass min which you have select
        calender.set(Calendar.SECOND, 0);
        calender.set(Calendar.MILLISECOND, 0);

        Calendar now = Calendar.getInstance();
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        if (calender.before(now)) {    //this condition is used for future reminder that means your reminder not fire for past time
            calender.add(Calendar.DATE, 7);
            Timber.d( "service start alarm stopped for next week at " + calender.getTimeInMillis());
        }
        else {
            Timber.d("service start alarm stopped for current week at " + calender.getTimeInMillis());
        }
        if (isEditingAlarm){
            pendingIntent = PendingIntent.getBroadcast(context, requestCodePendingIntent, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            pendingIntent = PendingIntent.getBroadcast(context, requestCodePendingIntent, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        }
        else
            pendingIntent = PendingIntent.getBroadcast(context, requestCodePendingIntent, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if(Build.VERSION.SDK_INT < 23){
            if(Build.VERSION.SDK_INT >= 19){
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntent);
            }
            else{
                alarmManager.set(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntent);
            }
        }
        else{
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntent);
        }
    }
    @SuppressLint("ObsoleteSdkInt")
    public void repeatAlarmToStopService(int weekNo, int hours) {
        PendingIntent pendingIntent;
        Intent intent = new Intent(context, ScheduleStopServiceReciever.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        intent.putExtra(AppConstants.DAY_OF_THE_WEEK_EXTRA, String.valueOf(weekNo));
        intent.putExtra(AppConstants.HOUR_EXTRA, String.valueOf(hours));
        int requestCodePendingIntent=weekNo+AppConstants.REQUEST_STOP_SERVICE;
        Calendar calender= Calendar.getInstance();

        calender.set(Calendar.DAY_OF_WEEK, weekNo);  //here pass week number
        calender.set(Calendar.HOUR_OF_DAY, hours);  //pass hour which you have select
        calender.set(Calendar.MINUTE, AppConstants.STOP_SERVICE_TIME_MIN);  //pass min which you have select
        calender.set(Calendar.SECOND, 0);
        calender.set(Calendar.MILLISECOND, 0);
        calender.add(Calendar.DATE, 7);
        Timber.d("service start alarm stopped for next week at " + calender.getTimeInMillis());
            pendingIntent = PendingIntent.getBroadcast(context, requestCodePendingIntent, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            pendingIntent = PendingIntent.getBroadcast(context, requestCodePendingIntent, intent,PendingIntent.FLAG_UPDATE_CURRENT);

        if(Build.VERSION.SDK_INT < 23){
            if(Build.VERSION.SDK_INT >= 19){
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntent);
            }
            else{
                alarmManager.set(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntent);
            }
        }
        else{
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntent);
        }
    }
}
