package baensch.de.airlocator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class WiFiSniffer {

    private String TAG = getClass().getSimpleName();
    private boolean d = true;

    private Context context;
    private SniffSink sniffSink;
    private Handler handler;
    private Timer scanTimer;

    public WiFiSniffer(Context context, Handler handler) {
        this.context = context;
        this.sniffSink = new SniffSink();
        this.handler = handler;
    }


    public void start(){
        if (d) Log.d(TAG, "start");
        IntentFilter i = new IntentFilter();
        i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(receiver, i);

        String connectivityContext = Context.WIFI_SERVICE;
        final WifiManager wifiManager = (WifiManager) context.getSystemService(connectivityContext);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.startScan();
                }
            }
        };
        scanTimer = new Timer();
        scanTimer.scheduleAtFixedRate(timerTask, 0, 3000);
    }

    public void stop(){
        if (d) Log.d(TAG, "stop");
        context.unregisterReceiver(receiver);
        scanTimer.cancel();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {
            if (d) Log.d(TAG, "Broadcastreceiver received data");
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> scanResults = wifiManager.getScanResults();
            for (ScanResult scanResult : scanResults) {

                String ssid = scanResult.SSID;
                String bssid = scanResult.BSSID.toUpperCase();

                if (d) Log.d(TAG, "ScanResult: " + scanResult.capabilities + " " + bssid + " " + ssid + " " + scanResult);

                Device device = new Device();
                device.setAddress(bssid);
                device.setName(ssid);
                device.setType(Device.TYPE_WIFI);
                device.setSecurity(scanResult.capabilities);
                sniffSink.addDevice(device);

            }
            new UploadDataTask().execute();
        }
    };

    private class UploadDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            JSONObject data = prepareJson();
            if (d) Log.d(TAG, "Json Data: "+data.toString());

            String result = "";
            String inputLine;
            // Headers
            ArrayList<String[]> headers = new ArrayList<>();
            headers.add(new String[]{"Content-Type", "application/json"});
            HttpResultHelper httpResult;
            try {
                httpResult = httpPost(API.URL, API.USER, API.KEY, data.toString(), headers, 120000);
                if (d) Log.d(TAG, "Result Statuscode "+httpResult.getStatusCode());

                if (httpResult.getStatusCode() != 200){
                    if (d) Log.d(TAG, "Sorry Dude, either not found or you forget to send your wifi networks");
                    return null;
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(httpResult.getResponse()));
                while ((inputLine = in.readLine()) != null) {
                    result += inputLine;
                }
                if (d) Log.d(TAG, "Result: "+result);

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            JSONObject jsonObject = null;
            if (d) Log.d(TAG, "Result "+result);

            try {
                jsonObject = new JSONObject(result);
                JSONArray pos = jsonObject.getJSONArray("result");

                double longitude = pos.getDouble(1);
                double latitude = pos.getDouble(0);

                Location location = new Location(longitude, latitude);


                Message msg = handler.obtainMessage(1);
                Bundle bundle =  new Bundle();
                bundle.putDouble("longitude", longitude);
                bundle.putDouble("latitude", latitude);
                msg.setData(bundle);
                handler.sendMessage(msg);


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private JSONObject prepareJson() {
        if (d) Log.d(TAG, "Start Preparing Json Data");

        List<Device> devices = sniffSink.getDevices();
        if (d) Log.d(TAG, "Got Data from DB -> Convert it to Json");

        JSONArray jsonDeviceArray = new JSONArray();
        for (Device device : devices) {
            jsonDeviceArray.put(device.getAsJson());
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("devices", jsonDeviceArray);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        if (d) Log.d(TAG, "Finished Preparing Json Data");
        return jsonObject;
    }

    // see http://notes.iopush.net/android-send-a-https-post-request/
    private HttpResultHelper httpPost(String urlStr, String user, String password, String data, ArrayList<String[]> headers, int timeOut) throws IOException
    {
        // Set url
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // If secure connection
        if (urlStr.startsWith("https")) {
            try {
                SSLContext sc;
                sc = SSLContext.getInstance("TLS");
                sc.init(null, null, new java.security.SecureRandom());
                ((HttpsURLConnection)conn).setSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                Log.d(TAG, "Failed to construct SSL object", e);
            }
        }


        // Use this if you need basic authentication
        if ((user != null) && (password != null)) {
            String userPass = user + ":" + password;
            String basicAuth = "Basic " + Base64.encodeToString(userPass.getBytes(), Base64.DEFAULT);
            conn.setRequestProperty("Authorization", basicAuth);
        }

        // Set Timeout and method
        conn.setReadTimeout(timeOut);
        conn.setConnectTimeout(timeOut);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        if (headers != null) {
            for (int i = 0; i < headers.size(); i++) {
                conn.setRequestProperty(headers.get(i)[0], headers.get(i)[1]);
            }
        }

        if (data != null) {
            conn.setFixedLengthStreamingMode(data.getBytes().length);
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(data);
            writer.flush();
            writer.close();
            os.close();
        }

        InputStream inputStream;
        try
        {
            inputStream = conn.getInputStream();
        }
        catch(IOException exception)
        {
            inputStream = conn.getErrorStream();
        }

        HttpResultHelper result = new HttpResultHelper();
        result.setStatusCode(conn.getResponseCode());
        result.setResponse(inputStream);

        return result;
    }
}
