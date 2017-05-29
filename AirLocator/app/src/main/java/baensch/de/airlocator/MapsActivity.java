package baensch.de.airlocator;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private String TAG = getClass().getSimpleName();
    private boolean d = true;
    private static final int TAG_CODE_PERMISSION_LOCATION = 2;
    private static final int REQUEST_SCAN_ALWAYS_AVAILABLE = 234;

    private GoogleMap mMap;
    private WiFiSniffer wiFiSniffer;
    private Marker myPositionMarker;
    private LatLng myOldPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (d) Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        // request location permission
        ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION},
                TAG_CODE_PERMISSION_LOCATION);


        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (Build.VERSION.SDK_INT >= 18 && !wifiManager.isScanAlwaysAvailable()) {
            Log.d(TAG, "Askink for ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE");
            startActivityForResult(new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE), REQUEST_SCAN_ALWAYS_AVAILABLE);
        }

        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (d) Log.d(TAG, "onMapReady");

        mMap = googleMap;
        wiFiSniffer = new WiFiSniffer(MapsActivity.this, guiHandler);
        wiFiSniffer.start();
    }

    @Override
    protected void onStop() {
        if (d) Log.d(TAG, "onStop");
        if (wiFiSniffer!=null) wiFiSniffer.stop();
        super.onStop();
    }


    private Handler guiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case WiFiSniffer.CODE_OK:
                    if (d) Log.d(TAG, "Callback - positionUpdated");
                    double longitude = msg.getData().getDouble(WiFiSniffer.EXTRA_LONGITUDE);
                    double latitude = msg.getData().getDouble(WiFiSniffer.EXTRA_LATITUDE);
                    long time = msg.getData().getLong(WiFiSniffer.EXTRA_TIME);
                    int numNetworks = msg.getData().getInt(WiFiSniffer.EXTRA_NUM_OF_NETWORKS);
                    if (d) Log.d(TAG, "Callback - " + longitude + " " + latitude);

                    Toast.makeText(getApplicationContext(), "DB Response Time: " + time + " ms\n# Networks: "+numNetworks, Toast.LENGTH_SHORT).show();

                    LatLng myPosition = new LatLng(longitude, latitude);

                    if (myOldPosition != null){
                        //draw line
                        mMap.addPolyline((new PolylineOptions())
                                .add(myOldPosition, myPosition).width(10).color(Color.BLUE)
                                .visible(true));
                    }
                    myOldPosition = myPosition;

                    // remove old maker
                    if (myPositionMarker != null) {
                        myPositionMarker.remove();
                    }

                    myPositionMarker = mMap.addMarker(new MarkerOptions().position(myPosition).title("Your Position"));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(myPosition)      // Sets the center of the map to Mountain View
                            .zoom(17)                   // Sets the zoom
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    break;

                case WiFiSniffer.CODE_FAIL:
                    Toast.makeText(getApplicationContext(), "Error - "+msg.getData().getString(WiFiSniffer.EXTRA_FAILMESSAGE), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
