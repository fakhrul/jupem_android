package com.petronas.fof.spot.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.petronas.fof.spot.AppConstants;
import com.petronas.fof.spot.adapters.CustomArrayAdapter;
import com.petronas.fof.spot.R;
import com.petronas.fof.spot.utilities.Scheduler;
import com.petronas.fof.spot.broadcastreceivers.ScheduleReciever;
import com.petronas.fof.spot.broadcastreceivers.ScheduleStopServiceReciever;
import com.petronas.fof.spot.models.TimeBeans;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

import static com.petronas.fof.spot.AppConstants.SHARED_PREF_NAME;


public class SchedulerActivity extends AppCompatActivity {

    private final String TAG = "SchedularActivity";
    private SharedPreferences sharedPref;
    private Switch sunday,monday,tuesday,wednesday,thursday,friday,saturday;
    private Scheduler scheduler;
    private Spinner startSunday,stopSunday,startMonday,stopMonday,startTuesday,stopTuesday,startWednesday,stopWednesday,startThursday,stopThursday,
    startFriday,stopFriday,startSaturday,stopSaturday;
    private  List<TimeBeans> timeslotList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedular);
        Objects.requireNonNull(getSupportActionBar()).setTitle("SPOT | Scheduler");

        try {
            sharedPref = EncryptedSharedPreferences.create(
                    SHARED_PREF_NAME,
                    MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException ignored) { }

        setupSpinners();
        scheduler =new Scheduler(SchedulerActivity.this);
        sunday=findViewById(R.id.sundaySwitch);
        monday=findViewById(R.id.mondaySwitch);
        tuesday=findViewById(R.id.tuesdaySwitch);
        wednesday=findViewById(R.id.wednesdaySwitch);
        thursday=findViewById(R.id.thursdaySwitch);
        friday=findViewById(R.id.fridaySwitch);
        saturday=findViewById(R.id.saturdaySwitch);

        // set saved values
        sunday.setChecked(sharedPref.getBoolean(AppConstants.SUNDAY,false));
        monday.setChecked(sharedPref.getBoolean(AppConstants.MONDAY,false));
        tuesday.setChecked(sharedPref.getBoolean(AppConstants.TUESDAY,false));
        wednesday.setChecked(sharedPref.getBoolean(AppConstants.WEDNESDAY,false));
        thursday.setChecked(sharedPref.getBoolean(AppConstants.THURSDAY,false));
        friday.setChecked(sharedPref.getBoolean(AppConstants.FRIDAY,false));
        saturday.setChecked(sharedPref.getBoolean(AppConstants.SATURDAY,false));


        final SharedPreferences.Editor editor = sharedPref.edit();
        sunday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    disableSpinner(startSunday,stopSunday);
                    editor.putInt(AppConstants.SUNDAY_START_TIME_INDEX,startSunday.getSelectedItemPosition());
                    editor.putInt(AppConstants.SUNDAY_STOP_TIME_INDEX,stopSunday.getSelectedItemPosition());
                    editor.putInt(AppConstants.SUNDAY_START_TIME_HOUR,timeslotList.get(startSunday.getSelectedItemPosition()).getHours());
                    editor.putInt(AppConstants.SUNDAY_STOP_TIME_HOUR,timeslotList.get(stopSunday.getSelectedItemPosition()).getHours());
                    editor.putBoolean(AppConstants.SUNDAY,true);
                    editor.commit();
                    scheduler.saveAlarmToStartService(1,false, timeslotList.get(startSunday.getSelectedItemPosition()).getHours());
                    scheduler.saveAlarmToStopService(1,false, timeslotList.get(stopSunday.getSelectedItemPosition()).getHours());
                }
                else {
                    editor.putBoolean(AppConstants.SUNDAY,false);
                    editor.commit();
                    cancelScheduleTask(1);
                    enableSpinner(startSunday,stopSunday);
                }
            }
        });
        monday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){

                    disableSpinner(startMonday,stopMonday);
                    editor.putInt(AppConstants.MONDAY_START_TIME_INDEX,startMonday.getSelectedItemPosition());
                    editor.putInt(AppConstants.MONDAY_STOP_TIME_INDEX,stopMonday.getSelectedItemPosition());
                    editor.putInt(AppConstants.MONDAY_START_TIME_HOUR,timeslotList.get(startMonday.getSelectedItemPosition()).getHours());
                    editor.putInt(AppConstants.MONDAY_STOP_TIME_HOUR,timeslotList.get(stopMonday.getSelectedItemPosition()).getHours());
                    editor.putBoolean(AppConstants.MONDAY,true);
                    editor.commit();
                    scheduler.saveAlarmToStartService(2,false,timeslotList.get(startMonday.getSelectedItemPosition()).getHours());
                    scheduler.saveAlarmToStopService(2,false,timeslotList.get(stopMonday.getSelectedItemPosition()).getHours());

                }
                else {
                    editor.putBoolean(AppConstants.MONDAY,false);
                    editor.commit();
                    enableSpinner(startMonday,stopMonday);
                    cancelScheduleTask(2);
                }
            }
        });
        tuesday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    disableSpinner(startTuesday,stopTuesday);
                    editor.putInt(AppConstants.TUESDAY_START_TIME_INDEX,startTuesday.getSelectedItemPosition());
                    editor.putInt(AppConstants.TUESDAY_STOP_TIME_INDEX,stopTuesday.getSelectedItemPosition());
                    editor.putInt(AppConstants.TUESDAY_START_TIME_HOUR,timeslotList.get(startTuesday.getSelectedItemPosition()).getHours());
                    editor.putInt(AppConstants.TUESDAY_STOP_TIME_HOUR,timeslotList.get(stopTuesday.getSelectedItemPosition()).getHours());
                    editor.putBoolean(AppConstants.TUESDAY,true);
                    editor.commit();
                    scheduler.saveAlarmToStartService(3,false,timeslotList.get(startTuesday.getSelectedItemPosition()).getHours());
                    scheduler.saveAlarmToStopService(3,false,timeslotList.get(stopTuesday.getSelectedItemPosition()).getHours());
                }
                else {
                    editor.putBoolean(AppConstants.TUESDAY,false);
                    editor.commit();
                    enableSpinner(startTuesday,stopTuesday);
                    cancelScheduleTask(3);
                }
            }
        });
        wednesday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    disableSpinner(startWednesday,stopWednesday);
                    editor.putInt(AppConstants.WEDNESDAY_START_TIME_INDEX,startWednesday.getSelectedItemPosition());
                    editor.putInt(AppConstants.WEDNESDAY_STOP_TIME_INDEX,stopWednesday.getSelectedItemPosition());
                    editor.putInt(AppConstants.WEDNESDAY_START_TIME_HOUR,timeslotList.get(startWednesday.getSelectedItemPosition()).getHours());
                    editor.putInt(AppConstants.WEDNESDAY_STOP_TIME_HOUR,timeslotList.get(stopWednesday.getSelectedItemPosition()).getHours());
                    editor.putBoolean(AppConstants.WEDNESDAY,true);
                    editor.commit();
                    scheduler.saveAlarmToStartService(4,false,timeslotList.get(startWednesday.getSelectedItemPosition()).getHours());
                    scheduler.saveAlarmToStopService(4,false,timeslotList.get(stopWednesday.getSelectedItemPosition()).getHours());
                }
                else {
                    editor.putBoolean(AppConstants.WEDNESDAY,false);
                    editor.commit();
                    enableSpinner(startWednesday,stopWednesday);
                    cancelScheduleTask(4);
                }
            }
        });
        thursday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    disableSpinner(startThursday,stopThursday);
                    editor.putInt(AppConstants.THURSDAY_START_TIME_INDEX,startThursday.getSelectedItemPosition());
                    editor.putInt(AppConstants.THURSDAY_STOP_TIME_INDEX,stopThursday.getSelectedItemPosition());
                    editor.putInt(AppConstants.THURSDAY_START_TIME_HOUR,timeslotList.get(startThursday.getSelectedItemPosition()).getHours());
                    editor.putInt(AppConstants.THURSDAY_STOP_TIME_HOUR,timeslotList.get(stopThursday.getSelectedItemPosition()).getHours());
                    editor.putBoolean(AppConstants.THURSDAY,true);
                    editor.commit();
                    scheduler.saveAlarmToStartService(5,false,timeslotList.get(startThursday.getSelectedItemPosition()).getHours());
                    scheduler.saveAlarmToStopService(5,false,timeslotList.get(stopThursday.getSelectedItemPosition()).getHours());
                }
                else {
                    editor.putBoolean(AppConstants.THURSDAY,false);
                    editor.commit();
                    enableSpinner(startThursday,stopThursday);
                    cancelScheduleTask(5);
                }
            }
        });
        friday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    disableSpinner(startFriday,stopFriday);
                    editor.putInt(AppConstants.FRIDAY_START_TIME_INDEX,startFriday.getSelectedItemPosition());
                    editor.putInt(AppConstants.FRIDAY_STOP_TIME_INDEX,stopFriday.getSelectedItemPosition());
                    editor.putInt(AppConstants.FRIDAY_START_TIME_HOUR,timeslotList.get(startFriday.getSelectedItemPosition()).getHours());
                    editor.putInt(AppConstants.FRIDAY_STOP_TIME_HOUR,timeslotList.get(stopFriday.getSelectedItemPosition()).getHours());
                    editor.putBoolean(AppConstants.FRIDAY,true);
                    editor.commit();
                    scheduler.saveAlarmToStartService(6,false,timeslotList.get(startFriday.getSelectedItemPosition()).getHours());
                    scheduler.saveAlarmToStopService(6,false,timeslotList.get(stopFriday.getSelectedItemPosition()).getHours());
                }
                else {
                    enableSpinner(startFriday,stopFriday);
                    editor.putBoolean(AppConstants.FRIDAY,false);
                    editor.commit();
                    cancelScheduleTask(6);
                }
            }
        });
        saturday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    disableSpinner(startSaturday,stopSaturday);
                    editor.putInt(AppConstants.SATURDAY_START_TIME_INDEX,startSaturday.getSelectedItemPosition());
                    editor.putInt(AppConstants.SATURDAY_STOP_TIME_INDEX,stopSaturday.getSelectedItemPosition());
                    editor.putInt(AppConstants.SATURDAY_START_TIME_HOUR,timeslotList.get(startSaturday.getSelectedItemPosition()).getHours());
                    editor.putInt(AppConstants.SATURDAY_STOP_TIME_HOUR,timeslotList.get(stopSaturday.getSelectedItemPosition()).getHours());
                    editor.putBoolean(AppConstants.SATURDAY,true);
                    editor.commit();
                    scheduler.saveAlarmToStartService(7,false,timeslotList.get(startSaturday.getSelectedItemPosition()).getHours());
                    scheduler.saveAlarmToStopService(7,false,timeslotList.get(stopSaturday.getSelectedItemPosition()).getHours());
                }
                else {
                    editor.putBoolean(AppConstants.SATURDAY,false);
                    editor.commit();
                    enableSpinner(startSaturday,stopSaturday);
                    cancelScheduleTask(7);
                }
            }
        });

    }

    private void enableSpinner(Spinner value1,Spinner value2){
        value1.setEnabled(true);
        value2.setEnabled(true);
    }
    private void disableSpinner(Spinner value1,Spinner value2){
        value1.setEnabled(false);
        value2.setEnabled(false);
    }
    private void setupSpinners() {

        timeslotList=getSpinnerData();
        ArrayAdapter spinnerAdapter = new CustomArrayAdapter(this, R.layout.item_spinner, getSpinnerData());

        startSunday =  findViewById(R.id.startSunday);
        stopSunday =  findViewById(R.id.stopSunday);
        startMonday =  findViewById(R.id.startMonday);
        stopMonday = findViewById(R.id.stopMonday);
        startTuesday = findViewById(R.id.startTuesday);
        stopTuesday = findViewById(R.id.stopTuesday);
        startWednesday = findViewById(R.id.startWednesday);
        stopWednesday = findViewById(R.id.stopWednesday);
        startThursday = findViewById(R.id.startThursday);
        stopThursday = findViewById(R.id.stopThursday);
        startFriday = findViewById(R.id.startFriday);
        stopFriday = findViewById(R.id.stopFriday);
        startSaturday = findViewById(R.id.startSaturday);
        stopSaturday = findViewById(R.id.stopSaturday);


        for (Spinner spinner: Arrays.asList(startSunday,stopSunday,startMonday,stopMonday,startTuesday,stopTuesday,startWednesday,stopWednesday,startThursday,stopThursday,
                startFriday,stopFriday,startSaturday,stopSaturday)) {
            spinner.setAdapter(spinnerAdapter);
        }

        if (sharedPref.getBoolean(AppConstants.SUNDAY,false)){
            startSunday.setSelection(sharedPref.getInt(AppConstants.SUNDAY_START_TIME_INDEX,0));
            stopSunday.setSelection(sharedPref.getInt(AppConstants.SUNDAY_STOP_TIME_INDEX,0));
            disableSpinner(startSunday,stopSunday);
        }
        if (sharedPref.getBoolean(AppConstants.MONDAY,false)){
            startMonday.setSelection(sharedPref.getInt(AppConstants.MONDAY_START_TIME_INDEX,0));
            stopMonday.setSelection(sharedPref.getInt(AppConstants.MONDAY_STOP_TIME_INDEX,0));
            disableSpinner(startMonday,stopMonday);
        }
        if (sharedPref.getBoolean(AppConstants.TUESDAY,false)){
            startTuesday.setSelection(sharedPref.getInt(AppConstants.TUESDAY_START_TIME_INDEX,0));
            stopTuesday.setSelection(sharedPref.getInt(AppConstants.TUESDAY_STOP_TIME_INDEX,0));
            disableSpinner(startTuesday,stopTuesday);
        }
        if (sharedPref.getBoolean(AppConstants.WEDNESDAY,false)){
            startWednesday.setSelection(sharedPref.getInt(AppConstants.WEDNESDAY_START_TIME_INDEX,0));
            stopWednesday.setSelection(sharedPref.getInt(AppConstants.WEDNESDAY_STOP_TIME_INDEX,0));
            disableSpinner(startWednesday,stopWednesday);
        }
        if (sharedPref.getBoolean(AppConstants.THURSDAY,false)){
            startThursday.setSelection(sharedPref.getInt(AppConstants.THURSDAY_START_TIME_INDEX,0));
            stopThursday.setSelection(sharedPref.getInt(AppConstants.THURSDAY_STOP_TIME_INDEX,0));
            disableSpinner(startThursday,stopThursday);
        }
        if (sharedPref.getBoolean(AppConstants.FRIDAY,false)){
            startFriday.setSelection(sharedPref.getInt(AppConstants.FRIDAY_START_TIME_INDEX,0));
            stopFriday.setSelection(sharedPref.getInt(AppConstants.FRIDAY_STOP_TIME_INDEX,0));
            disableSpinner(startFriday,stopFriday);
        }
        if (sharedPref.getBoolean(AppConstants.SATURDAY,false)){
            startSaturday.setSelection(sharedPref.getInt(AppConstants.SATURDAY_START_TIME_INDEX,0));
            stopSaturday.setSelection(sharedPref.getInt(AppConstants.SATURDAY_STOP_TIME_INDEX,0));
            disableSpinner(startSaturday,stopSaturday);
        }

        startSunday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeBeans time = (TimeBeans) parent.getSelectedItem();
                if (!isTimeSlotValid(time.getHours(),timeslotList.get(stopSunday.getSelectedItemPosition()).getHours()))
                    startSunday.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        stopSunday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeBeans time = (TimeBeans) parent.getSelectedItem();
                if (!isTimeSlotValid(timeslotList.get(startSunday.getSelectedItemPosition()).getHours(),time.getHours()))
                    stopSunday.setSelection(timeslotList.size()-1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        startMonday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeBeans time = (TimeBeans) parent.getSelectedItem();
                if (!isTimeSlotValid(time.getHours(),timeslotList.get(stopMonday.getSelectedItemPosition()).getHours()))
                    startMonday.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        stopMonday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeBeans time = (TimeBeans) parent.getSelectedItem();
                if (!isTimeSlotValid(timeslotList.get(startMonday.getSelectedItemPosition()).getHours(),time.getHours()))
                    stopMonday.setSelection(timeslotList.size()-1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        startTuesday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeBeans time = (TimeBeans) parent.getSelectedItem();
                if (!isTimeSlotValid(time.getHours(),timeslotList.get(stopTuesday.getSelectedItemPosition()).getHours()))
                    startTuesday.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        stopTuesday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeBeans time = (TimeBeans) parent.getSelectedItem();
                if (!isTimeSlotValid(timeslotList.get(startTuesday.getSelectedItemPosition()).getHours(),time.getHours()))
                    stopTuesday.setSelection(timeslotList.size()-1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        startWednesday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeBeans time = (TimeBeans) parent.getSelectedItem();
                if (!isTimeSlotValid(time.getHours(),timeslotList.get(stopWednesday.getSelectedItemPosition()).getHours()))
                    startWednesday.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        stopWednesday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeBeans time = (TimeBeans) parent.getSelectedItem();
                if (!isTimeSlotValid(timeslotList.get(startWednesday.getSelectedItemPosition()).getHours(),time.getHours()))
                    stopWednesday.setSelection(timeslotList.size()-1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        startThursday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeBeans time = (TimeBeans) parent.getSelectedItem();
                if (!isTimeSlotValid(time.getHours(),timeslotList.get(stopThursday.getSelectedItemPosition()).getHours()))
                    startThursday.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        stopThursday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeBeans time = (TimeBeans) parent.getSelectedItem();
                if (!isTimeSlotValid(timeslotList.get(startThursday.getSelectedItemPosition()).getHours(),time.getHours()))
                    stopThursday.setSelection(timeslotList.size()-1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        startFriday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeBeans time = (TimeBeans) parent.getSelectedItem();
                if (!isTimeSlotValid(time.getHours(),timeslotList.get(stopFriday.getSelectedItemPosition()).getHours()))
                    startFriday.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        stopFriday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeBeans time = (TimeBeans) parent.getSelectedItem();
                if (!isTimeSlotValid(timeslotList.get(startFriday.getSelectedItemPosition()).getHours(),time.getHours()))
                    stopFriday.setSelection(timeslotList.size()-1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        startSaturday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeBeans time = (TimeBeans) parent.getSelectedItem();
                if (!isTimeSlotValid(time.getHours(),timeslotList.get(stopSaturday.getSelectedItemPosition()).getHours()))
                    startSaturday.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        stopSaturday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeBeans time = (TimeBeans) parent.getSelectedItem();
                if (!isTimeSlotValid(timeslotList.get(startSaturday.getSelectedItemPosition()).getHours(),time.getHours()))
                    stopSaturday.setSelection(timeslotList.size()-1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    private void cancelScheduleTask(int requestCodePendingIntent){
        //cancel start service alarm at 08:00 am
        Intent intent = new Intent(this, ScheduleReciever.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent  pendingIntent = PendingIntent.getBroadcast(this, requestCodePendingIntent, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
        // cancel stop service alarm at 06:00 pm
        Intent intent2 = new Intent(this, ScheduleStopServiceReciever.class);
        PendingIntent  pendingIntent2 = PendingIntent.getBroadcast(this, requestCodePendingIntent+AppConstants.REQUEST_STOP_SERVICE, intent2, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent2);
        pendingIntent2.cancel();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
       super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Timber.d("MainActivity onDestroy()");
        super.onDestroy();
    }
    private List<TimeBeans> getSpinnerData(){
        List<TimeBeans> listSpinner=new ArrayList<>();
        TimeBeans tb0=new TimeBeans();
        tb0.setValue("07:00 AM");
        tb0.setHours(7);
        listSpinner.add(tb0);
        TimeBeans tb1=new TimeBeans();
        tb1.setValue("08:00 AM");
        tb1.setHours(8);
        listSpinner.add(tb1);
        TimeBeans tb2=new TimeBeans();
        tb2.setValue("09:00 AM");
        tb2.setHours(9);
        listSpinner.add(tb2);
        TimeBeans tb3=new TimeBeans();
        tb3.setValue("10:00 AM");
        tb3.setHours(10);
        listSpinner.add(tb3);
        TimeBeans tb4=new TimeBeans();
        tb4.setValue("11:00 AM");
        tb4.setHours(11);
        listSpinner.add(tb4);
        TimeBeans tb5=new TimeBeans();
        tb5.setValue("12:00 PM");
        tb5.setHours(12);
        listSpinner.add(tb5);
        TimeBeans tb6=new TimeBeans();
        tb6.setValue("01:00 PM");
        tb6.setHours(13);
        listSpinner.add(tb6);
        TimeBeans tb7=new TimeBeans();
        tb7.setValue("02:00 PM");
        tb7.setHours(14);
        listSpinner.add(tb7);
        TimeBeans tb8=new TimeBeans();
        tb8.setValue("03:00 PM");
        tb8.setHours(15);
        listSpinner.add(tb8);
        TimeBeans tb9=new TimeBeans();
        tb9.setValue("04:00 PM");
        tb9.setHours(16);
        listSpinner.add(tb9);
        TimeBeans tb10=new TimeBeans();
        tb10.setValue("05:00 PM");
        tb10.setHours(17);
        listSpinner.add(tb10);
        TimeBeans tb11=new TimeBeans();
        tb11.setValue("06:00 PM");
        tb11.setHours(18);
        listSpinner.add(tb11);
        TimeBeans tb12=new TimeBeans();
        tb12.setValue("07:00 PM");
        tb12.setHours(19);
        listSpinner.add(tb12);

        return listSpinner;
    }

    private boolean isTimeSlotValid(int start,int stop){
        if (start>=stop)
            return false;
        return true;
    }
}
