package com.petronas.fof.spot;

import android.app.Application;
import timber.log.Timber;

import static timber.log.Timber.DebugTree;

public class SpotApplication extends Application {

    @Override public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        }
    }
}
