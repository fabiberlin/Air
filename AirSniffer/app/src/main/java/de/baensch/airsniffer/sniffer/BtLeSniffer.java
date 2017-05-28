package de.baensch.airsniffer.sniffer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.baensch.airsniffer.db.Device;
import de.baensch.airsniffer.db.Location;

/**
 * Created by Fabian on 16.01.2017.
 */

public class BtLeSniffer {

    String TAG = getClass().getSimpleName();
    boolean d = false;

    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothLeScanner mBluetoothLeScanner = null;
    private ScanCallback mScanCallback = null;

    public BtLeSniffer() {
    }

    public void startScan() {
        if (d) Log.d(TAG, "start scanning");

        this.mBluetoothManager = (BluetoothManager) Sniffer.getInstance().getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        this.mBluetoothAdapter = mBluetoothManager.getAdapter();
        this.mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        this.mScanCallback = new ScanCallback();

        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scanSettingsBuilder
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
        } else {
            scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        }

        ScanSettings scanSettings = scanSettingsBuilder.build();
        List<ScanFilter> scanFilters = new ArrayList<>();
        mBluetoothLeScanner.stopScan(mScanCallback);
        mBluetoothLeScanner.startScan(scanFilters, scanSettings, mScanCallback);
    }

    public void stopScan() {
        if (d) Log.d(TAG, "stop scanninng");
        mBluetoothLeScanner.flushPendingScanResults(mScanCallback);
        mBluetoothLeScanner.stopScan(mScanCallback);
    }

    public class ScanCallback extends android.bluetooth.le.ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            int rssi = result.getRssi();
            if (d) Log.d(TAG, "found a device "+device.getName()+" "+device.getAddress()+" "+device.getBluetoothClass());
            Device dbDevice = new Device();
            dbDevice.setType(Device.TYPE_BTLE);
            dbDevice.setName(device.getName());
            dbDevice.setAddress(device.getAddress().toUpperCase());
            Location location = new Location();
            location.setSignalStrength(rssi);
            Sniffer.getInstance().addDevice(dbDevice, location);
        }
    }
}
