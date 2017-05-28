package baensch.de.airlocator;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Device {
    private int id = 0;
    private String address = "";
    private String name = "";
    private double longitude = 0;
    private double latitude = 0;
    private long timestamp = 0;
    private String type = "";
    private double radius = 0;
    private boolean uploaded = false;
    private String security = "";
    private boolean checkedLogin = false;
    private String password = "";

    public JSONObject getAsJson(){
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("address", getAddress());
//            jsonObject.put("name", getName());
//            jsonObject.put("timestamp", getTimestamp());
//            jsonObject.put("type", getType());
//            jsonObject.put("security", getSecurity());

        } catch (JSONException e) {
            Log.e("Device", e.toString());
            e.printStackTrace();
        }

        return jsonObject;
    }

    public static final String TYPE_BTEDR = "BtEdr";
    public static final String TYPE_BTLE = "BtLe";
    public static final String TYPE_WIFI = "WiFi";

    public Device(int id, String name, String address, double longitude, double latitude,
                  long timestamp, String type, double radius, boolean uploaded,
                  String security, boolean checkedLogin, String password) {
        this.id = id;
        this.address = address;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.timestamp = timestamp;
        this.type = type;
        this.radius = radius;
        this.uploaded = uploaded;
        this.security = security;
        this.checkedLogin = checkedLogin;
        this.password = password;
    }

    public Device(String name, String address, double longitude, double latitude,
                  long timestamp, String type, double radius, boolean uploaded,
                  String security, boolean checkedLogin, String password) {
        this.timestamp = timestamp;
        this.address = address;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.type = type;
        this.radius = radius;
        this.uploaded = uploaded;
        this.security = security;
        this.checkedLogin = checkedLogin;
        this.password = password;
    }

    public Device() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public void setUploaded(int flag) {
        this.uploaded = (flag == 1)? true : false;
    }

    public void setCheckedLogin(boolean checkedLogin) {
        this.checkedLogin = checkedLogin;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        if (name != null) {
            if (!name.equals("")) {
                return name;
            }else{
                return "N/A";
            }
        }else{
            return "N/A";
        }
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getRadius() {
        return radius;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public String getPassword() {
        return password;
    }

    public String getSecurity() {
        if (security.equals(""))return "N/A";
        return security;
    }

    public boolean isCheckedLogin() {
        return checkedLogin;
    }

    public int isUploade(){
        return (uploaded)? 1 : 0;
    }

    public boolean isAvailableForPasswordCracking(){
        if (type.equals(TYPE_BTEDR) || type.equals(TYPE_BTLE)) return false;
        if (checkedLogin) return false;
        if (name.equals("")) return false;
//        if (!name.equals("SkyNet")) return false;
//        if(MODE)
        return true;
    }

    public boolean isValid(){
        if (longitude == 0 || latitude == 0){
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Device))return false;
        final Device other = (Device) obj;
        return this.getAddress().equals(other.getAddress());
    }

    @Override
    public String toString() {
        return "id: "+id +"\n"+
                "name: "+name +"\n"+
                "address: "+address +"\n"+
                "longitude: "+longitude +"\n"+
                "latitude: "+latitude +"\n"+
                "timestamp: "+timestamp +"\n" +
                "security: "+security +"\n";
    }

    public int getColorValue(){
        if (type == TYPE_BTEDR) return 0xff0000ff;
        if (type == TYPE_BTLE) return 0xff00ff00;
        if (type == TYPE_WIFI) return 0xffff0000;
        return 0xffffffff;
    }
}
