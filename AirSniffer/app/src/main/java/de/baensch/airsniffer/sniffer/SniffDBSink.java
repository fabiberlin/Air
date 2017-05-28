package de.baensch.airsniffer.sniffer;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Vibrator;
import android.util.Log;

import java.util.concurrent.LinkedBlockingDeque;

import de.baensch.airsniffer.R;
import de.baensch.airsniffer.db.DatabaseHandler;
import de.baensch.airsniffer.db.Device;
import de.baensch.airsniffer.db.Location;
import de.baensch.airsniffer.util.LocationProcessor;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by fabi on 03.03.17.
 */

public class SniffDBSink implements Runnable{

    private boolean d = false;
    private String TAG = this.getClass().getSimpleName();

    private LinkedBlockingDeque<Wrapper> data;
    private DatabaseHandler db;
    private Vibrator v;
    private Context context;

    public SniffDBSink(Context context){
        if (d) Log.d(TAG, "Creating DB Sink");
        this.data = new LinkedBlockingDeque();
        this.context = context;
        db = new DatabaseHandler(context);
        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (d) Log.d(TAG, "Created DB Sink");
    }

    public void add(Device device, Location location){
        if (d) Log.d(TAG, "Adding Device to Sink");
        Wrapper wrapper = new Wrapper(device,location);
        data.offer(wrapper);
    }

    @Override
    public void run() {
        while (true) {
            Wrapper wrapper = null;
            try {
                wrapper = data.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            if (d) Log.d(TAG, "Took new Wrapper from Sink");

            if (wrapper != null) {
                Device device = wrapper.device;
                Location location = wrapper.location;

                long id;

                Device dbDeviceM = db.getDevice(device);
                if (dbDeviceM != null) {
                    if (d) Log.d(TAG, "Device was already in DB");
                    //already in DB
                    id = dbDeviceM.getId();
                    location.setKey((int) id);
                    db.safeAddLocation(location);
                } else {
                    if (d) Log.d(TAG, "Device was not in DB");
                    //not in DB
                    id = db.safeAddDevice(device);
                    location.setKey((int) id);
                    db.safeAddLocation(location);
                    v.vibrate(100);
                }
                if (d) Log.d(TAG, "Update Location Info");
                doNotification(device);
                updateLocationInfo(id);
            }
        }
    }

    private void doNotification(Device device) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.iclauncher)
                .setPriority(Notification.PRIORITY_HIGH)
                .setOngoing(true)
                .setLights(device.getColorValue(), 1000, 50)
                .setContentTitle("AirSniffer")
                .setContentText("Last: " + device.getName());
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }

    private void updateLocationInfo(long id) {
        if (id == -1)return;
        Device device = db.getFullyQualifiedDevice((int)id);
        LocationProcessor locationProcessor = new LocationProcessor(device);
        device = locationProcessor.getUpdatedDevice();
        db.updateDevice(device);

    }

    private class Wrapper{

        public Device device;
        public Location location;

        public Wrapper(Device device, Location location) {
            this.device = device;
            this.location = location;
        }
    }
}
