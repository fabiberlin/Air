package de.baensch.airsniffer.lifecycle;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import de.baensch.airsniffer.R;
import de.baensch.airsniffer.db.DatabaseHandler;
import de.baensch.airsniffer.db.Device;
import de.baensch.airsniffer.db.Location;
import de.baensch.airsniffer.gui.DeviceAdapter;
import de.baensch.airsniffer.util.API;
import de.baensch.airsniffer.util.HttpResultHelper;

public class MainActivity extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    boolean d = true;

    private ListView listViewDevices;
    private List<Device> deviceArrayList;
    private DeviceAdapter deviceAdapter;

    private Intent sniffServiceIntent = null;
    private boolean sniffServiceRunning = false;
    private SniffService mService = null;
    private boolean mBound = false;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        long time = System.currentTimeMillis();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DatabaseHandler handler = new DatabaseHandler(this);

        deviceArrayList = handler.getAllDevices();
        List<Location> locations = handler.getAllLocations();

        List<Device> devices = handler.getAllDevices();
//        if (d) Log.d(TAG, deviceArrayList.toString());
        deviceAdapter = new DeviceAdapter(this, deviceArrayList);
        listViewDevices = (ListView) findViewById(R.id.listView_devices);
        listViewDevices.setAdapter(deviceAdapter);
        listViewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device device = (Device) parent.getItemAtPosition(position);
                if (d) Log.d(TAG, "Clicked on device " + device);
                onListItemClicked(device);
            }
        });

        sniffServiceRunning = isMyServiceRunning(SniffService.class);
        if (sniffServiceRunning) {
            sniffServiceIntent = new Intent(getApplicationContext(), SniffService.class);
            startService(sniffServiceIntent);
        }


        EditText editTextSearch = (EditText) findViewById(R.id.editText_search);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                MainActivity.this.deviceAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });

        Log.d(TAG, "on create startup time " + (System.currentTimeMillis() - time) + " ms");
        long duration = System.currentTimeMillis() - time;
        TextView textViewInfo = (TextView) findViewById(R.id.textview_info);
        textViewInfo.setText("Found " + deviceArrayList.size() + " Devices with " + locations.size() + " Locations. (" + duration + "ms)");


        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (Build.VERSION.SDK_INT >= 18 && !wifiManager.isScanAlwaysAvailable()) {
            Log.d(TAG, "Askink for ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE");
            startActivityForResult(new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE), 100);
        }


//        for (Device device : devices) {
//            if (d) Log.d(TAG, device.toString());
//        }

        // request location permission
        ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION},
                1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
        }
    }

    private void startSniffingService() {
        if (d) Log.d(this.getClass().getSimpleName(), "start service in activity");
        sniffServiceIntent = new Intent(getApplicationContext(), SniffService.class);
        sniffServiceIntent.putExtra("KEY1", "Value to be used by the service");
        startService(sniffServiceIntent);
        sniffServiceRunning = true;
    }

    private void stopSniffingService() {
        if (d) Log.d(this.getClass().getSimpleName(), "stop service in activity");
        stopService(sniffServiceIntent);
        sniffServiceRunning = false;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (d) Log.d(this.getClass().getSimpleName(), "onServiceConnected");
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            SniffService.LocalBinder binder = (SniffService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            if (d) Log.d(this.getClass().getSimpleName(), "service loaded " + mService.toString());
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            if (d) Log.d(this.getClass().getSimpleName(), "onServiceDisconnected");
            mBound = false;
        }
    };

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem checkable = menu.findItem(R.id.menu_service_checkbox);
        checkable.setChecked(sniffServiceRunning);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_show_all_devices_on_map) {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            intent.setAction(MapsActivity.ACTION_SHOW_ALL_DEVICES);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_lock_screen){
            Intent intent = new Intent(getApplicationContext(), LockScreenActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.menu_item_refresh) {
            updateListview();
        }
        if (id == R.id.menu_item_upload) {
            uploadData();
        }
        if (id == R.id.menu_service_checkbox) {
            if (sniffServiceRunning) {
                stopSniffingService();
                item.setChecked(false);

            } else {
                startSniffingService();
                item.setChecked(true);
            }
        }
        if (id == R.id.menu_dbStats){
            Intent dbstats = new Intent(getApplicationContext(), DBStats.class);
            startActivity(dbstats);
        }
        if (id == R.id.menu_dump_data) {
            if (d) Log.d(TAG, "On Menu Item - Dump data clicked");
            showDumpDataConfirmationAlert();
            return true;
        }
        if (id == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateListview() {
        mergeListviwdevicesWithDB();
        deviceAdapter.notifyDataSetChanged();
    }

    private void mergeListviwdevicesWithDB() {
        DatabaseHandler handler = new DatabaseHandler(this);
        ArrayList<Device> dbDevices = handler.getAllDevices();
        for (Device dbDevice : dbDevices) {
            if (!deviceArrayList.contains(dbDevice)) {
                deviceArrayList.add(dbDevice);
            }
        }
    }

    private void showDumpDataConfirmationAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Are you sure?")
                .setTitle("Dump Database");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dumpData();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void dumpData() {
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        db.dumbData();
        deviceArrayList.clear();
        updateListview();

    }

    private void onListItemClicked(Device device) {
        Intent intent = new Intent(getApplicationContext(), ListLocationActivity.class);
        intent.putExtra(ListLocationActivity.EXTRA_DEVICE_ID, device.getId());
        startActivity(intent);
    }

    private void uploadData() {
        progress = new ProgressDialog(MainActivity.this);
        UploadDataTask prepareDataTask = (UploadDataTask) new UploadDataTask().execute();
    }

    private class UploadDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            showProgress(true);
            updateProgressMessage("Prepare Data",false);

            JSONObject data = prepareJson();

            updateProgressMessage("Send Data",false);

            String result = "";
            String inputLine;
            // Headers
            ArrayList<String[]> headers = new ArrayList<>();
            headers.add(new String[]{"Content-Type", "application/json"});
            HttpResultHelper httpResult = null;
            try {
                httpResult = httpPost(API.URL, API.USER, API.KEY, data.toString(), headers, 120000);
            } catch (IOException e) {
                e.printStackTrace();
                updateProgressMessage("Something went wrong during upload", true);
                return null;
            }
            BufferedReader in = null;
            try {
                 in = new BufferedReader(new InputStreamReader(httpResult.getResponse()));
            }catch (Exception e){
                e.printStackTrace();
                updateProgressMessage("Something went wrong during upload", true);
                return null;
            }
            try {
                while ((inputLine = in.readLine()) != null) {
                    result += inputLine;
                }
            } catch (IOException e) {
                e.printStackTrace();
                updateProgressMessage("Something went wrong during upload", true);
                return null;
            }


            updateProgressMessage("Upload Data was successful",true);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            showProgress(false);
            super.onPostExecute(aVoid);
        }
    }

    private JSONObject prepareJson() {
        if (d) Log.d(TAG, "Start Preparing Json Data");
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        List<Device> devices = db.getFullyQualifiedDevices();
        if (d) Log.d(TAG, "Got Data from DB -> Convert it to Json");

        JSONArray jsonDeviceArray = new JSONArray();
        for (Device device : devices) {
            if (device.isValid()) {
                jsonDeviceArray.put(device.getAsJson());
            }
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

        InputStream inputStream = null;
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

    void updateProgressMessage(final String message, final boolean asToast){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setMessage(message);
                if (asToast){
                    Toast.makeText(MainActivity.this , message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void showProgress(final boolean show){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show){
                    progress.show();
                }else{
                    progress.dismiss();
                }
            }
        });
    }
}
