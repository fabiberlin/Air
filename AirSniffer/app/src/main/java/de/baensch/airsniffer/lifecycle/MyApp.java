package de.baensch.airsniffer.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class MyApp extends Application implements Application.ActivityLifecycleCallbacks {

    boolean d = true;

    private static Context mContext;
    private static Boolean inForeground = false;

    public static Context getAppContext() {
        return mContext;
    }

    public static Boolean appInForeground() {
        return inForeground;
    }

    public void onCreate() {
        super.onCreate();
        if (d) Log.d(this.getClass().getSimpleName(), "onCreate");
        mContext = getApplicationContext();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (d) Log.d(this.getClass().getSimpleName(), "onActivityCreated");
        inForeground = true;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (d) Log.d(this.getClass().getSimpleName(), "onActivityStarted");
        inForeground = true;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (d) Log.d(this.getClass().getSimpleName(), "onActivityResumed");
        inForeground = true;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (d) Log.d(this.getClass().getSimpleName(), "onActivityPaused");
        inForeground = false;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (d) Log.d(this.getClass().getSimpleName(), "onActivityStopped");
        inForeground = false;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        if (d) Log.d(this.getClass().getSimpleName(), "onActivitySaveInstanceState");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (d) Log.d(this.getClass().getSimpleName(), "onActivityDestroyed");
        inForeground = false;
    }

}
