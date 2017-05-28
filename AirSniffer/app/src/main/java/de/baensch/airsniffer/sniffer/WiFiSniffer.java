package de.baensch.airsniffer.sniffer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.baensch.airsniffer.db.Device;
import de.baensch.airsniffer.db.Location;

/**
 * Created by Fabian on 16.01.2017.
 */

public class WiFiSniffer {

    String TAG = getClass().getSimpleName();
    boolean d = true;

    private Timer scanTimer;

    public WiFiSniffer() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                String connectivityContext = Context.WIFI_SERVICE;
                final WifiManager wifiManager = (WifiManager) Sniffer.getInstance().getContext().getSystemService(connectivityContext);
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.startScan();
                }
            }
        };
        scanTimer = new Timer();
        scanTimer.schedule(timerTask, 2000);
    }

    public void start(){
        if (d) Log.d(TAG, "start");
        IntentFilter i = new IntentFilter();
        i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        Sniffer.getInstance().getContext().registerReceiver(receiver, i);

        String connectivityContext = Context.WIFI_SERVICE;
        final WifiManager wifiManager = (WifiManager) Sniffer.getInstance().getContext().getSystemService(connectivityContext);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.startScan();
                }
            }
        };
        scanTimer = new Timer();
        scanTimer.scheduleAtFixedRate(timerTask, 0, 5000);

    }

    public void stop(){
        if (d) Log.d(TAG, "stop");
        Sniffer.getInstance().getContext().unregisterReceiver(receiver);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {
            if (d) Log.d(TAG, "Broadcastreceiver received data");
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> scanResults = wifiManager.getScanResults();
            for (ScanResult scanResult : scanResults) {

                String ssid = scanResult.SSID;
                String bssid = scanResult.BSSID.toUpperCase();

                if (d) Log.d(TAG, "ScanResult: " + scanResult.capabilities + " " + bssid + " " + ssid + " " + scanResult);

                Device dbDevice = new Device();
                dbDevice.setAddress(bssid);
                dbDevice.setName(ssid);
                dbDevice.setType(Device.TYPE_WIFI);
                dbDevice.setSecurity(scanResult.capabilities);
                Location location = new Location();
                location.setSignalStrength(scanResult.level);
                Sniffer.getInstance().addDevice(dbDevice, location);
            }
        }
    };
}
