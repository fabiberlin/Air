package de.baensch.airsniffer.db;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Fabian on 17.01.2017.
 */

public class Location {

    private int id = 0;
    private int key = 0;
    private double longitude = 0;
    private double latitude = 0;
    private double accuracy = 0;
    private long timestamp = 0;
    private int signalStrength = 0;
    private boolean uploaded = false;

    public Location(int id, int key, double longitude, double latitude,
                    double accuracy, long timestamp, int signalStrength, boolean uploaded) {
        this.id = id;
        this.key = key;
        this.longitude = longitude;
        this.latitude = latitude;
        this.accuracy = accuracy;
        this.timestamp = timestamp;
        this.signalStrength = signalStrength;
        this.uploaded = uploaded;
    }

    public Location(int key, double longitude, double latitude, double accuracy,
                    long timestamp, int signalStrength, boolean uploaded) {
        this.key = key;
        this.longitude = longitude;
        this.latitude = latitude;
        this.accuracy = accuracy;
        this.timestamp = timestamp;
        this.signalStrength = signalStrength;
        this.uploaded = uploaded;
    }

    public Location() {
    }

    public int getId() {
        return id;
    }

    public int getKey() {
        return key;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public boolean isValid(){
        return this.latitude != 0 || this.longitude != 0;
    }

    public JSONObject getAsJson(){
        JSONObject jsonObject = new JSONObject();
        JSONArray locJsonObject = new JSONArray();
        try {
            locJsonObject.put(getLongitude());
            locJsonObject.put(getLatitude());
            jsonObject.put("loc", locJsonObject);
            jsonObject.put("accuracy", getAccuracy());
            jsonObject.put("timestamp", getTimestamp());
            jsonObject.put("signalStrength", getSignalStrength());

        } catch (JSONException e) {
            Log.e("Location", e.toString());
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Location))return false;
        final Location other = (Location) obj;

        android.location.Location startPoint = new android.location.Location("locationA");
        startPoint.setLatitude(latitude);
        startPoint.setLongitude(longitude);
        android.location.Location endPoint = new android.location.Location("locationA");
        endPoint.setLatitude(other.getLatitude());
        endPoint.setLongitude(other.getLongitude());
        double distance = startPoint.distanceTo(endPoint);

        return  (this.key == other.getKey() && distance < 2);
    }

    @Override
    public String toString() {
        return "id: "+id +"\n"+
                "key: "+key +"\n"+
                "longitude: "+longitude +"\n"+
                "latitude: "+latitude +"\n"+
                "accuracy: "+accuracy +"\n"+
                "timestamp: "+timestamp +"\n"+
                "signalStrength: "+signalStrength +"\n";
    }
}
