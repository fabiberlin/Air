package de.baensch.airsniffer.sniffer;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.baensch.airsniffer.db.DatabaseHandler;
import de.baensch.airsniffer.db.Device;
import de.baensch.airsniffer.util.Locator;
import de.baensch.airsniffer.wificrack.WifiCracker;

/**
 * Created by Fabian on 15.01.2017.
 */

public class Sniffer {

    private String TAG = this.getClass().getSimpleName();
    private boolean d = true;

    private static int START_DELAY = 25000; //25sec

    private static Sniffer instance = null;
    private Context context;
    private List<Handler> handlers;
    private Locator locator;
    private BtEdrSniffer btEdrSniffer;
    private BtLeSniffer btLeSniffer;
    private WiFiSniffer wiFiSniffer;
    private WifiCracker wifiCracker;
    private Timer timer;
    private SniffDBSink sniffDBSink;
    private DatabaseHandler db;
    private Thread sinkThread;

    private boolean isRunning = false;

    private Sniffer(Context context){
        this.context = context;
        handlers =  new ArrayList<>();
        locator = Locator.getInstance();
        btEdrSniffer = new BtEdrSniffer();
        btLeSniffer = new BtLeSniffer();
        wiFiSniffer = new WiFiSniffer();
//        wifiCracker = new WifiCracker(context);
        db = new DatabaseHandler(context);
        sniffDBSink = new SniffDBSink(context);
    }

    public static Sniffer getInstance(Context context) {
        if (instance == null){
            instance = new Sniffer(context);
        }
        return instance;
    }

    public static Sniffer getInstance() {
        return instance;
    }

    public void addHandler(Handler serviceHandler) {
        if (!handlers.contains(serviceHandler)){
            handlers.add(serviceHandler);
        }
    }

    public Context getContext() {
        return context;
    }

    public void startSniffing(){
        locator.start();
        sinkThread = new Thread(sniffDBSink);
        sinkThread.start();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // this code will be executed after 25 seconds
                isRunning = true;
                btEdrSniffer.start();
                btLeSniffer.startScan();
                wiFiSniffer.start();
                timer = null;
            }
        }, START_DELAY);
    }

    public void stopSniffing(){
        locator.stop();
        sinkThread.interrupt();

        if (timer!=null){
            //timer is ticking
            timer.cancel();
            timer = null;
        }
        if (isRunning) {
            btEdrSniffer.stop();
            btLeSniffer.stopScan();
            wiFiSniffer.stop();
//            wifiCracker.stop();
            isRunning = false;
        }
    }


    void addDevice(Device device, de.baensch.airsniffer.db.Location location){

        long time = System.currentTimeMillis();
        Location currentLocation = Locator.getInstance().getLastLocation();
        if (currentLocation.getLatitude() == 0 || currentLocation.getLongitude() == 0 || currentLocation.getAccuracy() > 50){
            return;
        }

        location.setTimestamp(System.currentTimeMillis());
        location.setLongitude(currentLocation.getLongitude());
        location.setLatitude(currentLocation.getLatitude());
        location.setAccuracy(currentLocation.getAccuracy());

        device.setTimestamp(System.currentTimeMillis());
        device.setLongitude(currentLocation.getLongitude());
        device.setLatitude(currentLocation.getLatitude());

        sniffDBSink.add(device, location);

        time = System.currentTimeMillis() - time;
        if (d) Log.d(TAG, "Adding a Device took "+time+"ms");

    }


}
