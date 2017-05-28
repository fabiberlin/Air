package de.baensch.airsniffer.sniffer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import de.baensch.airsniffer.db.Device;
import de.baensch.airsniffer.db.Location;


/**
 * Created by Fabian on 16.01.2017.
 */

public class BtEdrSniffer {

    private boolean d = false;
    private String TAG = this.getClass().getSimpleName();

    public static final int INTERVAL = 20000;
    public static final int START_DELAY = 0; //TODO 20000 --> due to gps

    private BluetoothAdapter bluetoothAdapter;

    private Timer timer;

    public BtEdrSniffer() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void start(){
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        Sniffer.getInstance().getContext().registerReceiver(receiver, filter);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (bluetoothAdapter.isDiscovering()){
                    bluetoothAdapter.cancelDiscovery();
                }
                bluetoothAdapter.startDiscovery();
            }
        }, START_DELAY, INTERVAL);

    }

    public void stop(){
        timer.cancel();
        Sniffer.getInstance().getContext().unregisterReceiver(receiver);
        if (bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            switch (action){
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    if (d) Log.d(TAG, "Discovery Started");
                    break;

                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null) {
                        if (d) Log.d(TAG, "Found a Device " + device.getName() + " "+ device.getBluetoothClass() + " " + device.getAddress());

                        int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);

                        Device dbDevice = new Device();
                        dbDevice.setType(Device.TYPE_BTEDR);
                        dbDevice.setName(device.getName());
                        dbDevice.setAddress(device.getAddress().toUpperCase());
                        Location location = new Location();
                        location.setSignalStrength(rssi);
                        Sniffer.getInstance().addDevice(dbDevice, location);
                    }
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    if (d) Log.d(TAG, "Discovery Finished");
                    break;

                case BluetoothDevice.ACTION_UUID:
                    break;
                default:
                    if (d) Log.d(TAG, "BroadcastReceiver cant handle action. Action was " + action);
                    break;
            }
        }
    };

    public BroadcastReceiver getReceiver() {
        return receiver;
    }
}
