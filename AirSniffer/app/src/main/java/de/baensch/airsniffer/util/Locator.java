package de.baensch.airsniffer.util;

import android.location.Location;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import de.baensch.airsniffer.sniffer.Sniffer;

/**
 * Created by Fabian on 16.01.2017.
 */

public class Locator {

    private static Locator instance = null;

    public static final int INTERVAL = 1000; //1 sec

    private Location lastLocation = null;
    private Timer timer = null;
    private boolean isRunning = false;

    public static Locator getInstance() {
        if (instance == null){
            instance = new Locator();
        }
        return instance;
    }

    private Locator(){
        lastLocation = new Location("Locator init");
    }

    public void start(){
        isRunning = true;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
                    @Override
                    public void gotLocation(Location location){
                        if (isRunning) {
//                            Log.d("Locator", "got Location: " + location.getLongitude() + " " + location.getLatitude());
                            lastLocation = location;
                        }
                    }
                };
                MyLocation myLocation = new MyLocation();
                myLocation.getLocation(Sniffer.getInstance().getContext(), locationResult);
            }
        }, 0, INTERVAL);
    }

    public void stop(){
        isRunning = false;
        timer.cancel();
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
