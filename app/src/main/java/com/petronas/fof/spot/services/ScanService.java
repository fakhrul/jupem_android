package com.petronas.fof.spot.services;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.petronas.fof.spot.R;
import com.petronas.fof.spot.activities.MainActivity;
import com.petronas.fof.spot.utilities.DeviceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

import static com.petronas.fof.spot.AppConstants.CHECK_FOREGROUND_SERVICE;
import static com.petronas.fof.spot.AppConstants.SERVER_URL;
import static com.petronas.fof.spot.AppConstants.SHARED_PREF_NAME;
import static com.petronas.fof.spot.AppConstants.START_FOREGROUND_SERVICE;
import static com.petronas.fof.spot.AppConstants.STOP_FOREGROUND_SERVICE;

/**
 * Created by Tareq (tareqaziz2010@gmail.com) on april/2020
 **/

public class ScanService extends Service {
    // logging
    private final String TAG = "SPOTScanService";
    // constants
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    // The desired interval for location updates. Inexact. Updates may be more or less frequent.
    private static final long LOCATION_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    // The fastest rate for active location updates. Updates will never be more frequent than this value.
    private static final long FASTEST_LOCATION_UPDATE_INTERVAL_IN_MILLISECONDS = 3000;
    // data update interval to server
    private static final long UPDATE_DATA_INTERVAL_IN_MILLISECONDS = 30000;

    // flags
    private boolean stopWork = true; // to tell the work thread to stop
    boolean isScanning = false;

    private SharedPreferences sharedPref;
    private final Object lock = new Object();

    // wifi scanning
    private WifiManager wifiManager;

    // GPS scanning
    private FusedLocationProviderClient fusedLocationClient; // provides access to the Fused Location Provider API.
    private LocationRequest locationRequest;  // contains parameters for the location API
    private LocationCallback locationCallback; // Callback for changes in location.
    private Location currentLocation;  // current location
    private Handler serviceHandler;

    // post data request queue
    RequestQueue requestQueue;
    private JSONObject jsonBody = new JSONObject();
    private JSONObject wifiResults = new JSONObject();

    private String departmentName = "";
    private String locationName = "";
    private String userName = "";
    private String serverAddress = "";
    private boolean allowGPS = false;
    private boolean isLearning = false;

    private String api_key = "";
    private String uuid = "";

    Thread scanThread;
    Context context;
    private static PowerManager.WakeLock wakeLock;

    private void getUserDataFromSharedPrefs(){
        userName = sharedPref.getString("userName", "");
        departmentName = sharedPref.getString("departmentName", "");
        //serverAddress = sharedPref.getString("serverAddress", "");
        locationName = sharedPref.getString("locationName", "");
        allowGPS = sharedPref.getBoolean("allowGPS", true);
        isLearning = sharedPref.getBoolean("serviceLearning", false);

        api_key = "e24bc9d153";
        serverAddress = "https://localhost:44336/api/Location";
        uuid = "4c2f20b353554779a1c8f83e4c2e3574";
    }

    @Override
    public void onCreate() {

        Timber.d( "creating new scan service");

        requestQueue = Volley.newRequestQueue(this);

        // Setup Wifi =======================

        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiScanReceiver, intentFilter);
        context = getApplicationContext();

        // Setup GPS =========================

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };
        createLocationRequest();
        getLastLocation();

        // Setup Handler Thread for intervals

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        serviceHandler = new Handler(handlerThread.getLooper());

        Timber.d( "setting up scanning thread");
        scanThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                while (true) {
                    if (stopWork) {
                        Timber.d( "Stopped scanning thread");
                        return;
                    }

                    Timber.d( "Scanning thread - Scanning now..");

                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ScanService:WakeLock");
                    wakeLock.acquire(5 * 60 * 1000L /*5 minutes*/);

                    if (!wifiManager.isWifiEnabled()) wifiManager.setWifiEnabled(true);

                    LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Timber.d( "Gps not enabled");
                        // TODO what to do if GPS is not enabled
                    } else {
                        Timber.d( "Gps already enabled");
                        requestLocationUpdates();
                    }

                    doScan();
                    wakeLock.release();

                    try {
                        Thread.sleep(UPDATE_DATA_INTERVAL_IN_MILLISECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });

        Timber.d( "State of ScanThread: " + scanThread.isAlive());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (sharedPref == null) {
            try {
                sharedPref = EncryptedSharedPreferences.create(
                        SHARED_PREF_NAME,
                        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                        this,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } catch (GeneralSecurityException | IOException ignored) {
            }
        }

        // handle when starting service with scheduler
        if (intent == null || intent.hasExtra("source") || intent.getAction().equals(START_FOREGROUND_SERVICE)) {

            if(intent == null) {
                Timber.d( "onStartCommand of spot received a null intent.");
            } else {
                if (intent.hasExtra("source") && Objects.requireNonNull(intent.getStringExtra("source")).equalsIgnoreCase("scheduler")) {
                    Timber.d( "onStartCommand of spot received from scheduler.");
                } else {
                    Timber.d( "onStartCommand of spot received a null intent.");
                }

                if (intent.getAction().equals(START_FOREGROUND_SERVICE)) {
                    Timber.d( "Received Start Foreground Intent ");
                }
            }

            getUserDataFromSharedPrefs();
            startForeground(1, createNotification(isLearning));

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("serviceState", 1);
            editor.putBoolean("serviceLearning", isLearning);
            editor.commit();

            updateUserState("started");
            stopWork = false;
            requestLocationUpdates();

            if (!scanThread.isAlive()) {
                scanThread.start();
            }

            return START_STICKY;
        }

        else if (intent.getAction().equals(STOP_FOREGROUND_SERVICE)) {
            Timber.d( "Received Stop Foreground Intent");
            try {
                unregisterReceiver(wifiScanReceiver);
            } catch (Exception e) {
                Timber.d( e.toString());
            }

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("serviceState", 0);
            editor.putBoolean("serviceLearning", isLearning);
            editor.commit();

            updateUserState("stopped");
            removeLocationUpdates();
            serviceHandler.removeCallbacksAndMessages(null);
            stopWork = true;
            stopForeground(true);
            stopSelf();

            return START_NOT_STICKY;
        }

        // Check if service is running and restart if necessary
        else if (intent.getAction().equals(CHECK_FOREGROUND_SERVICE)) {
            Timber.d( "=====Checking service state==== ");
            // Several tests to check if service is running
            boolean serviceRunning = isServiceAlive(ScanService.class);
            Timber.d( "Service running: " + serviceRunning);
            boolean scanThreadRunning = scanThread.isAlive();
            Timber.d( "ScanThread running: " + scanThreadRunning);
            Timber.d( "stopWork? " + stopWork);
            Timber.d( "=================== ");

            if (!stopWork || serviceRunning || scanThreadRunning) {
                // service is still running, do nothing
                Timber.d( "Service is running... exiting onStartCommand without action");
            } else {
                // service might have been killed/stopped, restart scanning
                Timber.d( "Restarting Service....");

                getUserDataFromSharedPrefs();
                startForeground(1, createNotification(isLearning));

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("serviceState", 1);
                editor.putBoolean("serviceLearning", isLearning);
                editor.commit();

                stopWork = false;
                requestLocationUpdates(); // start location updates
                if (!scanThread.isAlive()) {
                    scanThread.start();
                }
            }
            return START_STICKY;
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        Timber.d( "onBind ScanService.");
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    @Override
    public void onDestroy() {
        Timber.d( "onDestroy ScanService");
        stopForeground(true);
        try {
            unregisterReceiver(wifiScanReceiver);
        } catch (Exception e) {
            Timber.d( e.toString());
        }
        removeLocationUpdates();
        serviceHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            // This condition is not necessary if you listen to only one action
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                Timber.d( "timer off, trying to send data");
                List<ScanResult> wifiScanList = wifiManager.getScanResults();
                // TODO filter the wifi_rssi data to contain only certain APs
                for (int i = 0; i < wifiScanList.size(); i++) {
                    String name = wifiScanList.get(i).SSID.toLowerCase() + " " + wifiScanList.get(i).BSSID.toLowerCase();
                    int rssi = wifiScanList.get(i).level;
                    Timber.d( "wifi: " + name + " => " + rssi + "dBm");
                    try {
                        wifiResults.put(name, rssi);
                    } catch (Exception e) {
                        Timber.d( e.toString());
                    }
                }
                sendData();
                synchronized (lock) {
                    isScanning = false;
                }
            }
        }
    };

    private void doScan() {
        synchronized (lock) {
            if (isScanning) {
                return;
            }
            isScanning = true;
        }
        wifiResults = new JSONObject();
        if (wifiManager.startScan()) {
            Timber.d( "started wifi scan");
        } else {
            Timber.d( "started wifi scan false?");
        }
        Timber.d( "started discovery");
    }

    public void sendData() {
        try {
            String URL = "https://geo.jupem.gov.my/api/location.php";

            jsonBody.put("api_key", api_key);
            String uuid = sharedPref.getString("userUUID", "");
            jsonBody.put("uuid", uuid);

            if (allowGPS) {
                JSONObject gps = new JSONObject();
                if (currentLocation != null) {
                    jsonBody.put("latitude", currentLocation.getLatitude());
                    jsonBody.put("longitude", currentLocation.getLongitude());
                }
            }
            final String requestBody = jsonBody.toString();
            removeLocationUpdates();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Timber.d( "Request response: " + response);
                    String res = response;
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Timber.d( "Response Error: " + error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = new String(response.data);
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {

        }
    }

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_LOCATION_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Makes a request for location updates. merely log any {@link SecurityException}.
     */
    public void requestLocationUpdates() {
        Timber.d("Requesting location updates");
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.myLooper());
        } catch (SecurityException e) {
            Timber.d( "Lost location permission. Could not request updates. " + e);
        }
    }

    /**
     * Removes location updates. merely log the {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        Timber.d("Removing location updates");
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        } catch (SecurityException e) {
            Timber.d( "Lost location permission. Could not remove updates. " + e);
        }
    }

    /**
     * @return the latest location
     */
    private void getLastLocation() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                currentLocation = task.getResult();
                            } else {
                                Timber.d( "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Timber.d( "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {
        Timber.d("New location: " + location);
        currentLocation = location;
    }


    private int getBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        if (batteryStatus != null) {
            return batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        }
        return 0;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Staff Locator Foreground Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

    }

    private Notification createNotification(Boolean isLearning) {
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("serviceState", 1);
        resultIntent.putExtra("serviceLearning", isLearning);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("SPOT Service")
                    .setContentText("Tracing for " + departmentName + "/" + userName)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentIntent(resultPendingIntent)
                    .build();
            return notification;
        } else {
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("SPOT Service")
                    .setContentText("Tracing for " + departmentName + "/" + userName)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentIntent(resultPendingIntent)
                    .build();
            return notification;
        }
    }

    private boolean isServiceAlive(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void updateUserState(String state) {
        JSONObject jsonData = new JSONObject();
        //String URL = SERVER_URL + "_userstate";
        String URL = "https://geo.jupem.gov.my/api/patient.php";
        try {
            jsonData.put("api_key", api_key);
            String my_ic = sharedPref.getString("userID", "");
            jsonData.put("my_ic", my_ic);
            String my_phone = sharedPref.getString("phoneNo", "");
            jsonData.put("my_phone",my_phone );
            sendToServer(jsonData, URL);
        } catch (JSONException ignored) {
        }
    }

    // send data to server
    private void sendToServer(JSONObject data, String URL) {
        final String mRequestBody = data.toString();
        Timber.d( mRequestBody);
        Timber.d( "Server: " + URL);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Timber.d( "Request response: " + response);

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    String uuid = (String) jsonObject.getString("uuid");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("userUUID", uuid);
                    editor.commit();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String res = response;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Timber.d( "Response Error: " + error.toString());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return mRequestBody.getBytes("utf-8");


                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = new String(response.data);
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        requestQueue.add(stringRequest);
    }
}