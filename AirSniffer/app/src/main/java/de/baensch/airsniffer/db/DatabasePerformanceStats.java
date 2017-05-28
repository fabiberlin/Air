package de.baensch.airsniffer.db;

import de.baensch.airsniffer.util.LimitedQueue;

/**
 * Created by fabian on 09.02.17.
 */

public class DatabasePerformanceStats {

    private static final int NUM_RECORDS = 10;

    private static DatabasePerformanceStats instance;

    public LimitedQueue snifferInsertionOverall;
    public LimitedQueue addDevice;
    public LimitedQueue addSafeDevice;
    public LimitedQueue addLocation;
    public LimitedQueue addSafeLocation;
    public LimitedQueue getLocation;
    public LimitedQueue getLocations;
    public LimitedQueue getLocationsByKey;
    public LimitedQueue getDevice;
    public LimitedQueue getDevices;
    public LimitedQueue getDevicesFullyQualified;
    public LimitedQueue getDeviceFullyQualified;


    private DatabasePerformanceStats(){
        snifferInsertionOverall = new LimitedQueue(NUM_RECORDS);
        addDevice = new LimitedQueue(NUM_RECORDS);
        addSafeDevice = new LimitedQueue(NUM_RECORDS);
        addLocation = new LimitedQueue(NUM_RECORDS);
        addSafeLocation = new LimitedQueue(NUM_RECORDS);
        getLocation = new LimitedQueue(NUM_RECORDS);
        getLocations = new LimitedQueue(NUM_RECORDS);
        getLocationsByKey = new LimitedQueue(NUM_RECORDS);
        getDevice = new LimitedQueue(NUM_RECORDS);
        getDevices = new LimitedQueue(NUM_RECORDS);
        getDevicesFullyQualified = new LimitedQueue(NUM_RECORDS);
        getDeviceFullyQualified = new LimitedQueue(NUM_RECORDS);
    }

    public static DatabasePerformanceStats getInstance() {
        if (instance == null) instance = new DatabasePerformanceStats();
        return instance;
    }

    public int getSnifferInsertionTime() {
        return snifferInsertionOverall.mean();
    }
    public int getaddDeviceTime() {
        return addDevice.mean();
    }
    public int getaddSafeDeviceTime() {
        return addSafeDevice.mean();
    }
    public int getaddLocationTime() {
        return addLocation.mean();
    }
    public int getaddSafeLocationTime() {
        return addSafeLocation.mean();
    }
    public int getgetLocationTime() {
        return getLocation.mean();
    }
    public int getgetLocationsTime() {
        return getLocations.mean();
    }
    public int getgetLocationsByKeyTime() {
        return getLocationsByKey.mean();
    }
    public int getgetDeviceTime() {
        return getDevice.mean();
    }
    public int getgetDevicesTime() {
        return getDevices.mean();
    }
    public int getgetDevicesFullyQualifiedTime() {
        return getDevicesFullyQualified.mean();
    }
    public int getgetDeviceFullyQualifiedTime() {
        return getDeviceFullyQualified.mean();
    }

}
