package de.baensch.airsniffer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Fabian on 15.01.2017.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    String TAG = this.getClass().getSimpleName();
    boolean d = false;

    private static final int DATABASE_VERSION = 15;

    private static final String DATABASE_NAME = "devicesManager";

    private static final String TABLE_DEVICES = "devices";
    private static final String TABLE_LOCATIONS = "locations";

    private static final String KEY_DEVICE_ID = "_id";
    private static final String KEY_DEVICE_NAME = "name";
    private static final String KEY_DEVICE_ADDRESS = "address";
    private static final String KEY_DEVICE_LONGITUDE = "longitude";
    private static final String KEY_DEVICE_LATITUDE = "latitude";
    private static final String KEY_DEVICE_TIMESTAMP = "timestamp";
    private static final String KEY_DEVICE_TYPE = "type";
    private static final String KEY_DEVICE_RADIUS = "radius";
    private static final String KEY_DEVICE_UPLOADED = "uploaded";
    private static final String KEY_DEVICE_SECURITY = "security";
    private static final String KEY_DEVICE_LOGIN_CHECKED = "login_checked";
    private static final String KEY_DEVICE_PASSWORD = "password";

    private static final String KEY_LOCATION_ID = "_id";
    private static final String KEY_LOCATION_KEY = "key";
    private static final String KEY_LOCATION_LONGITUDE = "longitude";
    private static final String KEY_LOCATION_LATITUDE = "latitude";
    private static final String KEY_LOCATION_ACCURACY = "accuracy";
    private static final String KEY_LOCATION_TIMESTAMP = "timestamp";
    private static final String KEY_LOCATION_SIGNALSTRENGTH = "signalstrength";
    private static final String KEY_LOCATION_UPLOADED = "uploaded";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (d) Log.d(TAG, "onCreate");
        String CREATE_DEVICES_TABLE = "CREATE TABLE " + TABLE_DEVICES + "("
                + KEY_DEVICE_ID + " INTEGER PRIMARY KEY, "
                + KEY_DEVICE_NAME + " TEXT, "
                + KEY_DEVICE_ADDRESS + " TEXT, "
                + KEY_DEVICE_LONGITUDE + " DOUBLE, "
                + KEY_DEVICE_LATITUDE + " DOUBLE, "
                + KEY_DEVICE_TIMESTAMP + " BIGINT, "
                + KEY_DEVICE_TYPE + " TEXT, "
                + KEY_DEVICE_RADIUS + " DOUBLE, "
                + KEY_DEVICE_UPLOADED + " TINYINT, "
                + KEY_DEVICE_SECURITY + " TEXT, "
                + KEY_DEVICE_LOGIN_CHECKED + " TINYINT, "
                + KEY_DEVICE_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_DEVICES_TABLE);

        String CREATE_LOCATIONS_TABLE = "CREATE TABLE " + TABLE_LOCATIONS + "("
                + KEY_LOCATION_ID + " INTEGER PRIMARY KEY, "
                + KEY_LOCATION_KEY + " INTEGER, "
                + KEY_LOCATION_LONGITUDE + " DOUBLE, "
                + KEY_LOCATION_LATITUDE + " DOUBLE, "
                + KEY_LOCATION_ACCURACY + " DOUBLE, "
                + KEY_LOCATION_TIMESTAMP + " BIGINT, "
                + KEY_LOCATION_SIGNALSTRENGTH + " INTEGER, "
                + KEY_LOCATION_UPLOADED + " TINYINT" + ")";
        db.execSQL(CREATE_LOCATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (d) Log.d(TAG, "onUpgrade");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        onCreate(db);
    }

    public void dumbData (){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        onCreate(db);
        db.close();

    }

    public long safeAddDevice(Device device){
        long start = System.currentTimeMillis();
        if (d) Log.d(TAG, "safeAddDevice");
        List<Device> devices = getAllDevices();
        if (!devices.contains(device)){
            return addDevice(device);
        }
        DatabasePerformanceStats.getInstance().addSafeDevice.add((int) (System.currentTimeMillis()-start));
        return -1;
    }

    public long safeAddLocation(Location location){
        long start = System.currentTimeMillis();
        if (d) Log.d(TAG, "safeAddLocation");
        List<Location> locations = getAllLocationsByKey(location.getKey());
        if (!locations.contains(location)){
            return addLocation(location);
        }
        DatabasePerformanceStats.getInstance().addSafeLocation.add((int) (System.currentTimeMillis()-start));
        return -1;
    }


    public long addDevice(Device device) {
        long start = System.currentTimeMillis();
        if (d) Log.d(TAG, "addDevice");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DEVICE_NAME, device.getName());
        values.put(KEY_DEVICE_ADDRESS, device.getAddress());
        values.put(KEY_DEVICE_LONGITUDE, device.getLongitude());
        values.put(KEY_DEVICE_LATITUDE, device.getLatitude());
        values.put(KEY_DEVICE_TIMESTAMP, device.getTimestamp());
        values.put(KEY_DEVICE_TYPE, device.getType());
        values.put(KEY_DEVICE_RADIUS, device.getRadius());
        values.put(KEY_DEVICE_UPLOADED, booleanToInt(device.isUploaded()));
        values.put(KEY_DEVICE_SECURITY, device.getSecurity());
        values.put(KEY_DEVICE_LOGIN_CHECKED, booleanToInt(device.isCheckedLogin()));
        values.put(KEY_DEVICE_PASSWORD, device.getPassword());
        long id = db.insert(TABLE_DEVICES, null, values);
        db.close();
        DatabasePerformanceStats.getInstance().addDevice.add((int) (System.currentTimeMillis()-start));
        return id;
    }

    public long addLocation(Location location){
        long start = System.currentTimeMillis();
        if (d) Log.d(TAG, "addLocation");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LOCATION_KEY, location.getKey());
        values.put(KEY_LOCATION_LONGITUDE, location.getLongitude());
        values.put(KEY_LOCATION_LATITUDE, location.getLatitude());
        values.put(KEY_LOCATION_ACCURACY, location.getAccuracy());
        values.put(KEY_LOCATION_TIMESTAMP, location.getTimestamp());
        values.put(KEY_LOCATION_SIGNALSTRENGTH, location.getSignalStrength());
        values.put(KEY_LOCATION_UPLOADED, booleanToInt(location.isUploaded()));
        long id = db.insert(TABLE_LOCATIONS, null, values);
        db.close();
        DatabasePerformanceStats.getInstance().addLocation.add((int) (System.currentTimeMillis()-start));
        return id;
    }

    public Device getDevice(int id) {
        long start = System.currentTimeMillis();
        if (d) Log.d(TAG, "getDevice");
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_DEVICES, new String[] {
                KEY_DEVICE_ID, KEY_DEVICE_NAME, KEY_DEVICE_ADDRESS, KEY_DEVICE_LONGITUDE,
                KEY_DEVICE_LATITUDE, KEY_DEVICE_TIMESTAMP, KEY_DEVICE_TYPE, KEY_DEVICE_RADIUS,
                KEY_DEVICE_UPLOADED, KEY_DEVICE_SECURITY, KEY_DEVICE_LOGIN_CHECKED, KEY_DEVICE_PASSWORD
        }, KEY_DEVICE_ID + "=?", new String[] {
                String.valueOf(id)
        }, null, null, null, null);

        if (cursor.getCount() == 0) return null;

        if (cursor != null)
            cursor.moveToFirst();

        Device device = new Device(
                cursor.getInt(0),//TODO
                cursor.getString(1),
                cursor.getString(2),
                cursor.getDouble(3),
                cursor.getDouble(4),
                cursor.getLong(5),
                cursor.getString(6),
                cursor.getDouble(7),
                intToBoolean(cursor.getInt(8)),
                cursor.getString(9),
                intToBoolean(cursor.getInt(10)),
                cursor.getString(11));
        db.close();
        DatabasePerformanceStats.getInstance().getDevice.add((int) (System.currentTimeMillis()-start));
        return device;
    }

    public Device getDevice(Device device) {
        long start = System.currentTimeMillis();
        if (d) Log.d(TAG, "getDevice");
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_DEVICES, new String[] {
                KEY_DEVICE_ID, KEY_DEVICE_NAME, KEY_DEVICE_ADDRESS, KEY_DEVICE_LONGITUDE,
                KEY_DEVICE_LATITUDE, KEY_DEVICE_TIMESTAMP, KEY_DEVICE_TYPE, KEY_DEVICE_RADIUS,
                KEY_DEVICE_UPLOADED, KEY_DEVICE_SECURITY, KEY_DEVICE_LOGIN_CHECKED, KEY_DEVICE_PASSWORD
        }, KEY_DEVICE_ADDRESS + "=?", new String[] {
                String.valueOf(device.getAddress())
        }, null, null, null, null);

        if (cursor.getCount() == 0) return null;

        if (cursor != null) {
            cursor.moveToFirst();
        }


        if (d) Log.d(TAG, "Count" + cursor.getCount());

        Device dbdevice = new Device(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getDouble(3),
                cursor.getDouble(4),
                cursor.getLong(5),
                cursor.getString(6),
                cursor.getDouble(7),
                intToBoolean(cursor.getInt(8)),
                cursor.getString(9),
                intToBoolean(cursor.getInt(10)),
                cursor.getString(11));
        db.close();
        DatabasePerformanceStats.getInstance().getDevice.add((int) (System.currentTimeMillis()-start));
        return dbdevice;
    }

    public Device getFullyQualifiedDevice(int id){
        long start = System.currentTimeMillis();
        if (d) Log.d(TAG, "getFullyQualifiedDevice");

        Device device = getDevice(id);
        device.setLocations(getAllLocationsByKey(device.getId()));
        DatabasePerformanceStats.getInstance().getDeviceFullyQualified.add((int) (System.currentTimeMillis()-start));
        return device;
    }

    public List<Device> getFullyQualifiedDevices(){
        long start = System.currentTimeMillis();
        if (d) Log.d(TAG, "getFullyQualifiedDevices");

        List<Device> devices = getAllDevices();
        for (Device device: devices ) {
            device.setLocations(getAllLocationsByKey(device.getId()));
        }
        DatabasePerformanceStats.getInstance().getDevicesFullyQualified.add((int) (System.currentTimeMillis()-start));
        return devices;

//        long start = System.currentTimeMillis();
//        if (d) Log.d(TAG, "getFullyQualifiedDevices");
//
//        List<Device> devices = getAllDevices();
//        Device[] devicesArray = new Device[devices.size()*2];
//        for (Device device : devices) {
//            devicesArray[device.getId()] = device;
//        }
//        List<Location> locations = getAllLocations();
//        for (Location location : locations) {
//            devicesArray[location.getKey()].addLocation(location);
//        }
//
//        devices = Arrays.asList(devicesArray);
//
//        DatabasePerformanceStats.getInstance().getDevicesFullyQualified.add((int) (System.currentTimeMillis()-start));
//        return devices;
    }

    public Location getLocationByID (int id) {
        if (d) Log.d(TAG, "getLocationByID");
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_LOCATIONS, new String[] {
                KEY_LOCATION_ID, KEY_LOCATION_KEY, KEY_LOCATION_LONGITUDE, KEY_LOCATION_LATITUDE,
                KEY_LOCATION_ACCURACY, KEY_LOCATION_TIMESTAMP, KEY_LOCATION_SIGNALSTRENGTH, KEY_LOCATION_UPLOADED
        }, KEY_LOCATION_ID + "=?", new String[] {
                String.valueOf(id)
        }, null, null, null, null);

        if (cursor.getCount() == 0) return null;

        if (cursor != null)
            cursor.moveToFirst();

        Location location = new Location(
                cursor.getInt(0),
                cursor.getInt(1),
                cursor.getDouble(2),
                cursor.getDouble(3),
                cursor.getDouble(4),
                cursor.getLong(5),
                cursor.getInt(6),
                intToBoolean(cursor.getInt(7)));
        db.close();
        return location;
    }


    public ArrayList<Device> getAllDevices() {
        long start = System.currentTimeMillis();
        if (d) Log.d(TAG, "getAllDevices");

        ArrayList<Device> devicesList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_DEVICES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() == 0) return devicesList;

        if (cursor.moveToFirst()) {
            do {
                Device device = new Device();
                device.setId(cursor.getInt(0));
                device.setName(cursor.getString(1));
                device.setAddress(cursor.getString(2));
                device.setLongitude(cursor.getDouble(3));
                device.setLatitude(cursor.getDouble(4));
                device.setTimestamp(cursor.getLong(5));
                device.setType(cursor.getString(6));
                device.setRadius(cursor.getDouble(7));
                device.setUploaded(intToBoolean(cursor.getInt(8)));
                device.setSecurity(cursor.getString(9));
                device.setCheckedLogin(intToBoolean(10));
                device.setPassword(cursor.getString(11));
                devicesList.add(device);
            } while (cursor.moveToNext());
        }
        db.close();
        DatabasePerformanceStats.getInstance().getDevices.add((int) (System.currentTimeMillis()-start));
        return devicesList;
    }

    public List<Location> getAllLocations() {
        long start = System.currentTimeMillis();
        if (d) Log.d(TAG, "getAllLocations");

        List<Location> locations = new ArrayList<Location>();
        String selectQuery = "SELECT  * FROM " + TABLE_LOCATIONS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() == 0) return locations;

        if (cursor.moveToFirst()) {
            do {
                Location location = new Location(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getDouble(2),
                        cursor.getDouble(3),
                        cursor.getDouble(4),
                        cursor.getLong(5),
                        cursor.getInt(6),
                        intToBoolean(cursor.getInt(7)));
                locations.add(location);
            } while (cursor.moveToNext());
        }
        db.close();
        DatabasePerformanceStats.getInstance().getLocations.add((int) (System.currentTimeMillis()-start));
        return locations;
    }

    public List<Location> getAllLocationsByKey(int key) {
        long start = System.currentTimeMillis();
        if (d) Log.d(TAG, "getAllLocationsByKey");

        List<Location> locations = new ArrayList<Location>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_LOCATIONS, new String[] {
                KEY_LOCATION_ID, KEY_LOCATION_KEY, KEY_LOCATION_LONGITUDE, KEY_LOCATION_LATITUDE,
                KEY_LOCATION_ACCURACY, KEY_LOCATION_TIMESTAMP, KEY_LOCATION_SIGNALSTRENGTH, KEY_LOCATION_UPLOADED
        }, KEY_LOCATION_KEY+"=?", new String[] { String.valueOf(key) }, null, null, null);

        if (cursor.getCount() == 0) return locations;

        if (cursor.moveToFirst()) {
            do {
                Location location = new Location(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getDouble(2),
                        cursor.getDouble(3),
                        cursor.getDouble(4),
                        cursor.getLong(5),
                        cursor.getInt(6),
                        intToBoolean(cursor.getInt(7)));
                locations.add(location);
            } while (cursor.moveToNext());
        }
        db.close();
        DatabasePerformanceStats.getInstance().getLocationsByKey.add((int) (System.currentTimeMillis()-start));
        return locations;
    }

    public int updateDevice(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DEVICE_NAME, device.getName());
        values.put(KEY_DEVICE_ADDRESS, device.getAddress());
        values.put(KEY_DEVICE_LONGITUDE, device.getLongitude());
        values.put(KEY_DEVICE_LATITUDE, device.getLatitude());
        values.put(KEY_DEVICE_TIMESTAMP, device.getTimestamp());
        values.put(KEY_DEVICE_TYPE, device.getType());
        values.put(KEY_DEVICE_RADIUS, device.getRadius());
        values.put(KEY_DEVICE_UPLOADED, booleanToInt(device.isUploaded()));
        values.put(KEY_DEVICE_SECURITY, device.getSecurity());
        values.put(KEY_DEVICE_LOGIN_CHECKED, booleanToInt(device.isCheckedLogin()));
        values.put(KEY_DEVICE_PASSWORD, device.getPassword());

        int id = db.update(TABLE_DEVICES, values, KEY_DEVICE_ID + "=?",
                new String[] {
                        String.valueOf(device.getId())
                });
        db.close();
        return id;

    }

    public void deleteDevice(Device device) {
        if (d) Log.d(TAG, "device");
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DEVICES, KEY_DEVICE_ID + "=?",
                new String[] {
                        String.valueOf(device.getId())
                });
        db.close();
    }

    public void deleteLocation(Location location) {
        if (d) Log.d(TAG, "deleteLocation");
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCATIONS, KEY_LOCATION_ID + "=?",
                new String[] {
                        String.valueOf(location.getId())
                });
        db.close();
    }

    public int getDevicesCount() {
        if (d) Log.d(TAG, "getDevicesCount");
        String countQuery = "SELECT * FROM " + TABLE_DEVICES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
//        cursor.close();

        db.close();
        return cursor.getCount();
    }

    private boolean intToBoolean (int flag){
        return (flag == 1)? true : false;
    }

    private int booleanToInt (boolean value){
        return (value)? 1 : 0;
    }

    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);
        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);
            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {
                alc.set(0,c);
                c.moveToFirst();
                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }
}
