package com.petronas.fof.spot.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.petronas.fof.spot.BuildConfig;
import com.petronas.fof.spot.R;

import timber.log.Timber;

import static com.petronas.fof.spot.AppConstants.PERMISSION_REQUEST_FINE_LOCATION;

public class BaseActivity extends AppCompatActivity {

    private final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // check/get permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P )
            getPermissions();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            getPermissionsWithBackgroundLocation();

        // whitelist app
        requestPermissionBatteryOptimization();

        super.onCreate(savedInstanceState);
    }

    // acquire run-time permissions
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getPermissionsWithBackgroundLocation() {
        // if permissions were not granted
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED||checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            // show alert dialog to explain objective of app
            AlertDialog.Builder permissionAlertBuilder = new AlertDialog.Builder(this);
            final AlertDialog d = permissionAlertBuilder.setTitle("Location Access required")
                    .setCancelable(true)
                    .setMessage(getResources().getText(R.string.permission_description))
                    .setNegativeButton("Exist", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "Exiting the app. User refused to share location data.", Toast.LENGTH_LONG).show();
                            finish(); //exit App
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                                    (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED||checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                            || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                                Timber.d("Asking for permissions");
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.WAKE_LOCK, Manifest.permission.INTERNET,
                                        Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.CHANGE_WIFI_STATE,
                                        Manifest.permission.ACCESS_WIFI_STATE}, PERMISSION_REQUEST_FINE_LOCATION);

                            }
                        }
                    }).show();
            // make links clickable
            ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

        } else {
            Timber.d("Run-time permissions for ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION are already granted");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getPermissions() {
        // if permissions were not granted
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // show alert dialog to explain objective of app
            AlertDialog.Builder permissionAlertBuilder = new AlertDialog.Builder(this);
            final AlertDialog d = permissionAlertBuilder.setTitle("Location Access required")
                    .setCancelable(true)
                    .setMessage(getResources().getText(R.string.permission_description))
                    .setNegativeButton("Exist", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "Exiting the app. User refused to share location data.", Toast.LENGTH_LONG).show();
                            finish(); //exit App
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                                    (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                            || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                                Timber.d("Asking for permissions");
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WAKE_LOCK, Manifest.permission.INTERNET,
                                        Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.CHANGE_WIFI_STATE,
                                        Manifest.permission.ACCESS_WIFI_STATE}, PERMISSION_REQUEST_FINE_LOCATION);

                            }
                        }
                    }).show();
            // make links clickable
            ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

        } else {
            Timber.d("Run-time permissions for ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION are already granted");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Timber.d("inside onRequestPermissionsResult");
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //success
                Timber.d("LOCATION permission granted.");
            } else {
                Timber.d("LOCATION permission was not granted yet!");
                // no permission granted. exit the app
                finish();
            }
        }
    }

    private void requestPermissionBatteryOptimization() {
        PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
        String packageName = BuildConfig.APPLICATION_ID;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent i = new Intent();
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                i.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                i.setData(Uri.parse("package:" + packageName));
                startActivity(i);
            }
        }
    }

}