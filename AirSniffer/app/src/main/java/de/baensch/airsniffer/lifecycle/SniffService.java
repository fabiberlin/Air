package de.baensch.airsniffer.lifecycle;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import de.baensch.airsniffer.sniffer.Sniffer;

public class SniffService extends Service {

    boolean d = true;

    private final IBinder mBinder = new LocalBinder();
    int mStartMode = START_STICKY;
    boolean mAllowRebind = false;

    private Sniffer sniffer = null;

    private Handler backgroundServiceHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //TODO Do the magic for Backgroundservice
        }
    };

    private Handler foregroundServiceHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //TODO Do the magic for GUI
        }
    };

    /**
     * Called when the service is being created.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        if (d) Log.d(this.getClass().getSimpleName(), "onCreate " + this.toString());

        sniffer = Sniffer.getInstance(this);
        registerHandler();
        //do more stuff

    }

    /**
     * The service is starting, due to a call to startService()
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (d) Log.d(this.getClass().getSimpleName(), "onStartCommand");
        registerHandler();
        sniffer.startSniffing();
        return START_STICKY;
    }

    /**
     * Called when all clients have unbound with unbindService()
     */
    @Override
    public boolean onUnbind(Intent intent) {
        if (d) Log.d(this.getClass().getSimpleName(), "onUnbind");
        return mAllowRebind;
    }

    /**
     * Called when a client is binding to the service with bindService()
     */
    @Override
    public void onRebind(Intent intent) {
        Log.d(this.getClass().getSimpleName(), "onRebind");
    }

    /**
     * Called when The service is no longer used and is being destroyed
     */
    @Override
    public void onDestroy() {
        if (d) Log.d(this.getClass().getSimpleName(), "onDestroy");
        sniffer.stopSniffing();
        //Stop stuff in sniffer
    }

    /**
     * A client is binding to the service with bindService()
     */
    @Override
    public IBinder onBind(Intent intent) {
        if (d) Log.d(this.getClass().getSimpleName(), "onBind");
        return mBinder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (d) Log.d(this.getClass().getSimpleName(), "onTaskRemoved");
        super.onTaskRemoved(rootIntent);
    }

    public void registerHandler() {
        if (d) Log.d("HANDLER", MyApp.appInForeground().toString());
        if (d) Log.d("HANDLER", "" + System.identityHashCode(MyApp.class));

        if (MyApp.appInForeground()) {
            sniffer.addHandler(foregroundServiceHandler);
        } else {
            sniffer.addHandler(backgroundServiceHandler);
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public SniffService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SniffService.this;
        }
    }
}
