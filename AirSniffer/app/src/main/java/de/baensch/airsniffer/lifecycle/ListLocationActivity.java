package de.baensch.airsniffer.lifecycle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import de.baensch.airsniffer.R;
import de.baensch.airsniffer.db.DatabaseHandler;
import de.baensch.airsniffer.db.Device;
import de.baensch.airsniffer.db.Location;
import de.baensch.airsniffer.gui.LocationAdapter;
import de.baensch.airsniffer.util.Helper;

public class ListLocationActivity extends AppCompatActivity {

    String TAG = getClass().getSimpleName();
    boolean d = true;

    private ListView listViewLocations;
    private List<Location> locationList;
    private LocationAdapter locationAdapter;

    public static final String EXTRA_DEVICE_ID = "device_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_location);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        int id = intent.getIntExtra(EXTRA_DEVICE_ID, 0);

        DatabaseHandler db = new DatabaseHandler(this);

        Device device = db.getFullyQualifiedDevice(id);
        TextView textViewHeader = (TextView)findViewById(R.id.textView_device_detail_header);
        TextView textViewTimestamp = (TextView)findViewById(R.id.textView_device_detail_value_timestamp);
        TextView textViewLongitude = (TextView)findViewById(R.id.textView_device_detail_value_longitudee);
        TextView textViewLatitude = (TextView)findViewById(R.id.textView_device_detail_value_latitude);
        TextView textViewRadius = (TextView)findViewById(R.id.textView_device_detail_value_radius);
        TextView textViewNumLocation = (TextView)findViewById(R.id.textView_device_detail_value_num_Location);
        TextView textViewSecurity = (TextView)findViewById(R.id.textView_device_detail_value_security);


        String name = device.getName();
        String address = device.getAddress();
        String header = "";
        if (name.equals("") || name.equals("null")){
            textViewHeader.setText(address);
        }else{
            textViewHeader.setText(name + "   " + address);
        }

        textViewTimestamp.setText(Helper.getDate(device.getTimestamp()));
        textViewLongitude.setText(String.valueOf(device.getLongitude()));
        textViewLatitude.setText(String.valueOf(device.getLatitude()));
        textViewRadius.setText(String.valueOf(device.getRadius()));
        textViewNumLocation.setText(String.valueOf(device.getLocations().size()));
        textViewSecurity.setText(device.getSecurity());

        locationList = device.getLocations();
        if (d) Log.d(TAG, locationList.toString());

        locationAdapter = new LocationAdapter(this, locationList);
        listViewLocations = (ListView)findViewById(R.id.listView_locations);
        listViewLocations.setAdapter(locationAdapter);

        listViewLocations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Location location = (Location) parent.getItemAtPosition(position);
                if (d) Log.d(TAG, "Clicked on location " + location);
                onListItemClicked(location);
            }
        });
    }

    private void onListItemClicked(Location location) {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.setAction(MapsActivity.ACTION_SHOW_LOCATION);
        intent.putExtra(MapsActivity.EXTRA_LOCATION_ID, location.getId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_show_locations_for_device) {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            intent.setAction(MapsActivity.ACTION_SHOW_ALL_LOCATIONS_OF_DEVICE);
            intent.putExtra(MapsActivity.EXTRA_DEVICE_ID, getIntent().getIntExtra(EXTRA_DEVICE_ID, 0));
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
