package de.baensch.airsniffer.lifecycle;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import de.baensch.airsniffer.R;
import de.baensch.airsniffer.db.DatabasePerformanceStats;

public class DBStats extends AppCompatActivity {

    private static final int INTERVAL = 2000;
    private String TAG = getClass().getSimpleName();

    private TextView snifferInsertionOverall;
    private TextView addDevice;
    private TextView addSafeDevice;
    private TextView addLocation;
    private TextView addSafeLocation;
    private TextView getLocation;
    private TextView getLocations;
    private TextView getLocationsByKey;
    private TextView getDevice;
    private TextView getDevices;
    private TextView getDevicesFullyQualified;
    private TextView getDeviceFullyQualified;

    private Timer timer;

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(TAG, "handle Message");
            snifferInsertionOverall.setText(DatabasePerformanceStats.getInstance().getSnifferInsertionTime() +" ms");
            addDevice.setText(DatabasePerformanceStats.getInstance().getaddDeviceTime() +" ms");
            addSafeDevice.setText(DatabasePerformanceStats.getInstance().getaddSafeDeviceTime() +" ms");
            addLocation.setText(DatabasePerformanceStats.getInstance().getaddLocationTime() +" ms");
            addSafeLocation.setText(DatabasePerformanceStats.getInstance().getaddSafeLocationTime() +" ms");
            getLocation.setText(DatabasePerformanceStats.getInstance().getgetLocationTime() +" ms");
            getLocations.setText(DatabasePerformanceStats.getInstance().getgetLocationsTime() +" ms");
            getLocationsByKey.setText(DatabasePerformanceStats.getInstance().getgetLocationsByKeyTime() +" ms");
            getDevice.setText(DatabasePerformanceStats.getInstance().getgetDeviceTime() +" ms");
            getDevices.setText(DatabasePerformanceStats.getInstance().getgetDevicesTime() +" ms");
            getDevicesFullyQualified.setText(DatabasePerformanceStats.getInstance().getgetDevicesFullyQualifiedTime() +" ms");
            getDeviceFullyQualified.setText(DatabasePerformanceStats.getInstance().getgetDeviceFullyQualifiedTime() +" ms");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbstats);

        snifferInsertionOverall = (TextView) findViewById(R.id.txt_snifferInsertionOverall);
        addDevice = (TextView) findViewById(R.id.txt_addDevice);
        addSafeDevice = (TextView) findViewById(R.id.txt_addSafeDevice);
        addLocation = (TextView) findViewById(R.id.txt_addLocation);
        addSafeLocation = (TextView) findViewById(R.id.txt_addSafeLocation);
        getLocation = (TextView) findViewById(R.id.txt_getLocation);
        getLocations = (TextView) findViewById(R.id.txt_getLocations);
        getLocationsByKey = (TextView) findViewById(R.id.txt_getLocationsByKey);
        getDevice = (TextView) findViewById(R.id.txt_getDevice);
        getDevices = (TextView) findViewById(R.id.txt_getDevices);
        getDevicesFullyQualified = (TextView) findViewById(R.id.txt_getDevicesFullyQualified);
        getDeviceFullyQualified = (TextView) findViewById(R.id.txt_getDeviceFullyQualified);

        timer = new Timer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mHandler.obtainMessage(1).sendToTarget();
            }
        }, 0, INTERVAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
    }
}
