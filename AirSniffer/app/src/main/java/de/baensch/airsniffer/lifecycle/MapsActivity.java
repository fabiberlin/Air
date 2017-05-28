package de.baensch.airsniffer.lifecycle;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import de.baensch.airsniffer.R;
import de.baensch.airsniffer.db.DatabaseHandler;
import de.baensch.airsniffer.db.Device;
import de.baensch.airsniffer.db.Location;
import de.baensch.airsniffer.util.Helper;
import de.baensch.airsniffer.util.LocationProcessor;
import de.baensch.airsniffer.util.Point;

import static android.R.attr.id;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    String TAG = getClass().getSimpleName();
    boolean d = true;

    public static final String EXTRA_DEVICE_ID = "device_id";
    public static final String EXTRA_LOCATION_ID = "location_id";

    public static final String ACTION_SHOW_DEVICE = "device";
    public static final String ACTION_SHOW_ALL_DEVICES = "all_devices";
    public static final String ACTION_SHOW_ALL_LOCATIONS_OF_DEVICE = "all_locations_of_device";
    public static final String ACTION_SHOW_LOCATION = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (d) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (d) Log.d(TAG, "onMapReady");
        mMap = googleMap;
//
//        try {
//            // Customise the styling of the base map using a JSON object defined
//            // in a raw resource file.
//            boolean success = googleMap.setMapStyle(
//                    MapStyleOptions.loadRawResourceStyle(
//                            this, R.raw.templatemaps));
//
//            if (!success) {
//                Log.e(TAG, "Style parsing failed.");
//            }
//        } catch (Resources.NotFoundException e) {
//            Log.e(TAG, "Can't find style. Error: ", e);
//        }


        DatabaseHandler db = new DatabaseHandler(this);
        Intent intent = getIntent();

        switch (intent.getAction()){
            case ACTION_SHOW_DEVICE:

                break;

            case ACTION_SHOW_ALL_DEVICES:
                List<Device> devices = db.getAllDevices();
                if (devices.size() >= 1){
                    LatLng pos = null;
                    for (Device d : devices) {
                        pos = new LatLng(d.getLatitude(), d.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(pos).title(d.getName()).alpha(0.23f)
                                .snippet(d.getAddress()));
                    }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 16));
            }
                break;

            case ACTION_SHOW_ALL_LOCATIONS_OF_DEVICE:
                int deviceId = intent.getIntExtra(EXTRA_DEVICE_ID, 0);
                Device device = db.getFullyQualifiedDevice(deviceId);
                List<Location> locations = device.getLocations();
                LatLng locationPos = null;
                for (Location l: locations) {
                    locationPos = new LatLng(l.getLatitude(), l.getLongitude());
                    mMap.addMarker(new MarkerOptions()
                            .position(locationPos)
                            .title(Helper.getDate(l.getTimestamp()))
                            .snippet("Accuracy: "+String.format("%1$,.2f", l.getAccuracy()) + " | " +
                                    "Signal: "+l.getSignalStrength()+"dB")
                            .alpha(1f));
                }

                locationPos = new LatLng(device.getLatitude(), device.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(locationPos)
                        .title(device.getName() + " " + device.getAddress())
                        .alpha(1f)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationPos, 18));

//                if (d) Log.d(TAG, "Draw Circle with radius: "+radius + " m");

                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(device.getLatitude(), device.getLongitude()))
                        .radius(device.getRadius())
                        .strokeColor(Color.argb(130, 20, 20, 200))
                        .fillColor(Color.argb(100, 20, 20, 200)));


                break;

            case ACTION_SHOW_LOCATION:
                int locationIId = intent.getIntExtra(EXTRA_LOCATION_ID, 0);
                Location location = db.getLocationByID(locationIId);
                LatLng locationPosi = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(locationPosi)
                        .visible(true)
                        .title(Helper.getDate(location.getTimestamp()))
                        .snippet("Accuracy: "+String.format("%1$,.2f", location.getAccuracy()) + " | " +
                                "Signal: "+location.getSignalStrength()+"dB")
                        .alpha(1f));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationPosi, 19));

                break;
        }
    }
}
