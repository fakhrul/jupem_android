package com.petronas.fof.spot;

import android.Manifest;
import android.net.Uri;

import java.io.File;

/**
 * Created by Asif Khan
 */

public final class AppConstants {

    public static final String DAY_OF_THE_WEEK_EXTRA = "alarmDay";
    public static final String HOUR_EXTRA = "hour";
    public static final String SUNDAY = "sunday";
    public static final String MONDAY = "monday";
    public static final String TUESDAY = "tuesday";
    public static final String WEDNESDAY = "wednesday";
    public static final String THURSDAY = "thursday";
    public static final String FRIDAY = "friday";
    public static final String SATURDAY = "saturday";
    public static final String MONDAY_START_TIME_INDEX = "MONDAY_START_TIME";
    public static final String MONDAY_STOP_TIME_INDEX = "MONDAY_STOP_TIME";
    public static final String SUNDAY_START_TIME_INDEX = "SUNDAY_START_TIME";
    public static final String SUNDAY_STOP_TIME_INDEX = "SUNDAY_STOP_TIME";
    public static final String TUESDAY_START_TIME_INDEX = "TUESDAY_START_TIME";
    public static final String TUESDAY_STOP_TIME_INDEX = "TUESDAY_STOP_TIME";
    public static final String WEDNESDAY_STOP_TIME_INDEX = "WEDNESDAY_STOP_TIME";
    public static final String WEDNESDAY_START_TIME_INDEX = "WEDNESDAY_START_TIME";
    public static final String THURSDAY_STOP_TIME_INDEX = "THURSDAY_STOP_TIME";
    public static final String THURSDAY_START_TIME_INDEX = "THURSDAY_START_TIME";
    public static final String FRIDAY_STOP_TIME_INDEX = "FRIDAY_STOP_TIME";
    public static final String FRIDAY_START_TIME_INDEX = "FRIDAY_START_TIME";
    public static final String SATURDAY_STOP_TIME_INDEX = "SATURDAY_STOP_TIME";
    public static final String SATURDAY_START_TIME_INDEX = "SATURDAY_START_TIME";
    public static final String SUNDAY_START_TIME_HOUR = "SUNDAY_START_TIME_HOUR";
    public static final String SUNDAY_STOP_TIME_HOUR = "SUNDAY_STOP_TIME_HOUR";
    public static final String MONDAY_START_TIME_HOUR = "MONDAY_START_TIME_HOUR";
    public static final String MONDAY_STOP_TIME_HOUR = "MONDAY_STOP_TIME_HOUR";
    public static final String TUESDAY_START_TIME_HOUR = "TUESDAY_START_TIME_HOUR";
    public static final String TUESDAY_STOP_TIME_HOUR = "TUESDAY_STOP_TIME_HOUR";
    public static final String WEDNESDAY_START_TIME_HOUR = "WEDNESDAY_START_TIME_HOUR";
    public static final String WEDNESDAY_STOP_TIME_HOUR = "WEDNESDAY_STOP_TIME_HOUR";
    public static final String THURSDAY_START_TIME_HOUR = "THURSDAY_START_TIME_HOUR";
    public static final String THURSDAY_STOP_TIME_HOUR = "THURSDAY_STOP_TIME_HOUR";
    public static final String FRIDAY_START_TIME_HOUR = "FRIDAY_START_TIME_HOUR";
    public static final String FRIDAY_STOP_TIME_HOUR = "FRIDAY_STOP_TIME_HOUR";
    public static final String SATURDAY_START_TIME_HOUR = "SATURDAY_START_TIME_HOUR";
    public static final String SATURDAY_STOP_TIME_HOUR = "SATURDAY_STOP_TIME_HOUR";
    public static int   REQUEST_STOP_SERVICE= 6;
    public static int   START_SERVICE_TIME= 8;
    public static int   START_SERVICE_TIME_MIN= 0;
    public static int   ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 991;
    public static int   STOP_SERVICE_TIME= 12;
    public static int   STOP_SERVICE_TIME_MIN= 0;
    public static final String SHARED_PREF_NAME = "FOF.SPOT";
    public static final String START_FOREGROUND_SERVICE = "com.petronas.fof.foregroundservice.start";
    public static final String STOP_FOREGROUND_SERVICE = "com.petronas.fof.foregroundservice.stop";
    public static final String CHECK_FOREGROUND_SERVICE = "com.petronas.fof.foregroundservice.check";
    public static final int PERMISSION_REQUEST_FINE_LOCATION = 3;

    public static final String PROJECT_NAME = "sensor";
    public static final String SERVER = "https://localhost:44336";
    public static final String SERVER_URL = SERVER + "/api/";
    private AppConstants() {
        // This utility class is not publicly instantiable
    }
}
