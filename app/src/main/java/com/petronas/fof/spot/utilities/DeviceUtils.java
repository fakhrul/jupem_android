package com.petronas.fof.spot.utilities;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;

public class DeviceUtils {

    /**
     * Returns the consumer friendly device name
     */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return StringUtils.capitalize(model);
        }
        return StringUtils.capitalize(manufacturer) + " " + model;
    }

    public static boolean isBatteryOptimized(Context context) {
        // returns True if app is whitelisted from battery optimization
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
    }

    public static boolean isInteractive(Context context) {
        // Returns True if device is in interactive state (ready to interact wih user)
        // if false, device is dozing or asleep
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH
                ? powerManager.isInteractive()
                : powerManager.isScreenOn();
    }

    // TODO represent all available mode and add broadcast receiver for ACTION_DEVICE_IDLE_MODE_CHANGED
    public static boolean idleMode(Context context) {
        // Returns true if the device is currently in power save mode.
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && powerManager.isDeviceIdleMode();

    }
}
