package de.baensch.airsniffer.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.baensch.airsniffer.db.Device;
import de.baensch.airsniffer.db.Location;

/**
 * Created by Fabian on 18.01.2017.
 */

public class LocationProcessor {

    private Device device;
    RichLocation richLocation;

    public LocationProcessor(Device device) {
        this.device = device;
        richLocation = new RichLocation();
        calculateMidpoint();
        calculateRadius();
    }

    public Device getUpdatedDevice(){
        this.device.setLongitude(richLocation.center.longitude);
        this.device.setLatitude(richLocation.center.latitude);
        this.device.setRadius(richLocation.radius);
        return device;
    }

    public RichLocation getRichLocation() {
        return richLocation;
    }

    private void calculateMidpoint (){
        double lo = 0;
        double la = 0;
        int counter = 0;
        for (Location location: device.getLocations()) {
            if (location.getAccuracy() < 40) {
                lo += location.getLongitude();
                la += location.getLatitude();
                counter++;
            }
        }
        lo /= counter;
        la /= counter;
        if (lo == 0 || la == 0){
            richLocation.center = new Point(device.getLongitude(), device.getLatitude());
        }else {
            richLocation.center = new Point(lo, la);
        }
    }

    private void calculateRadius() {

        if (device.getLocations().size() <= 1) {
            richLocation.radius = 20;
            return;
        }

        double maxDistance = Double.MIN_VALUE;
        for (Location l: device.getLocations()) {
            if (l.getAccuracy() <= 40) {
                double distance = distance(
                        l.getLatitude(),
                        l.getLongitude(),
                        richLocation.center.latitude,
                        richLocation.center.longitude);
                if (distance > maxDistance) {
                    maxDistance = distance;
                }
            }
        }
        if (maxDistance < 20) {
            richLocation.radius = 20;
            return;
        }
        richLocation.radius = maxDistance;
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {

        android.location.Location startPoint = new android.location.Location("locationA");
        startPoint.setLatitude(lat1);
        startPoint.setLongitude(lon1);

        android.location.Location endPoint = new android.location.Location("locationA");
        endPoint.setLatitude(lat2);
        endPoint.setLongitude(lon2);

        double distance = startPoint.distanceTo(endPoint);

        return distance;
    }
}
