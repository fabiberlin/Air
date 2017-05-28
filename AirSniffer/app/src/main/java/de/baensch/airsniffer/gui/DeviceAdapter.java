package de.baensch.airsniffer.gui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.baensch.airsniffer.R;
import de.baensch.airsniffer.db.Device;
import de.baensch.airsniffer.util.Helper;

/**
 * Created by Fabian on 15.01.2017.
 */

public class DeviceAdapter extends ArrayAdapter<Device> implements Filterable {

    boolean d = true;
    String TAG = getClass().getSimpleName();

    private List<Device>originalData = null;
    private List<Device>filteredData = null;
    private LayoutInflater mInflater;
    private ItemFilter mFilter = new ItemFilter();

    public DeviceAdapter(Context context, List<Device> devices) {
        super(context, 0, devices);
        this.filteredData = devices ;
        this.originalData = devices ;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        if (filteredData == null) return 0;
        return filteredData.size();
    }

    public Device getItem(int position) {
        return filteredData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Device device = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_item, parent, false);
//            convertView.setLongClickable(true);
        }
        // accessing the gui elements
        TextView txt_name = (TextView) convertView.findViewById(R.id.textView_device_name);
        TextView txt_address = (TextView) convertView.findViewById(R.id.textView_device_address);
        TextView txt_latitude = (TextView) convertView.findViewById(R.id.textView_device_lat);
        TextView txt_longitude = (TextView) convertView.findViewById(R.id.textView_device_long);
        TextView txt_timestamp = (TextView) convertView.findViewById(R.id.textView_device_timestamp);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView_device_icon);
        TextView txt_num_locations = (TextView) convertView.findViewById(R.id.textView_device_number_locations);
        TextView txt_radius = (TextView) convertView.findViewById(R.id.textView_device_radius);
        TextView txt_security = (TextView) convertView.findViewById(R.id.textView_device_security);

        //Setting the gui elements to the properties of the messageModel
        txt_name.setText(device.getName());
        txt_address.setText(device.getAddress());
        txt_latitude.setText(String.valueOf(device.getLatitude()));
        txt_longitude.setText(String.valueOf(device.getLongitude()));
        txt_radius.setText(String.valueOf((int)device.getRadius()) +" m");
//        txt_num_locations.setText(String.valueOf(device.getLocations().size()));

        txt_timestamp.setText(Helper.getDate(device.getTimestamp()));

        switch (device.getType()){
            case Device.TYPE_BTEDR:
                imageView.setImageDrawable(convertView.getResources().getDrawable(R.drawable.bluetoothedr));
                txt_security.setText(device.getSecurity());
//                txt_security.setVisibility(View.GONE);
                break;
            case Device.TYPE_BTLE:
                imageView.setImageDrawable(convertView.getResources().getDrawable(R.drawable.bluetoothle));
                txt_security.setText(device.getSecurity());
//                txt_security.setVisibility(View.GONE);
                break;
            case Device.TYPE_WIFI:
                imageView.setImageDrawable(convertView.getResources().getDrawable(R.drawable.wifi));
//                txt_security.setVisibility(View.VISIBLE);
                txt_security.setText(device.getSecurity());
                break;
        }



        return convertView;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<Device> list = originalData;

            int count = list.size();
            final ArrayList<Device> nlist = new ArrayList<Device>(count);

            String filterableString;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i).getName();
                if (filterableString != null) {
                    if (filterableString.toLowerCase().contains(filterString)) {
                        nlist.add(list.get(i));
                    }
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
            filteredData = (ArrayList<Device>) results.values;
            notifyDataSetChanged();
        }
    }


}
