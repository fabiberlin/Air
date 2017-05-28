package de.baensch.airsniffer.gui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.baensch.airsniffer.R;
import de.baensch.airsniffer.db.Device;
import de.baensch.airsniffer.db.Location;
import de.baensch.airsniffer.util.Helper;

/**
 * Created by Fabian on 18.01.2017.
 */

public class LocationAdapter extends ArrayAdapter<Location> {

    public LocationAdapter(Context context, List<Location> devices) {
        super(context, 0, devices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Location location = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.location_item, parent, false);
//            convertView.setLongClickable(true);
        }

        TextView textView_time = (TextView) convertView.findViewById(R.id.textView_location_time);
        TextView textView_long = (TextView) convertView.findViewById(R.id.textView_location_longitude);
        TextView textView_lati = (TextView) convertView.findViewById(R.id.textView_location_latitude);
        TextView textView_sign = (TextView) convertView.findViewById(R.id.textView_location_signalstrength);
        TextView textView_accu = (TextView) convertView.findViewById(R.id.textView_location_accuracy);

        textView_time.setText(Helper.getDate(location.getTimestamp()));
        textView_long.setText(String.valueOf(location.getLongitude()));
        textView_lati.setText(String.valueOf(location.getLatitude()));
        textView_sign.setText(String.valueOf(location.getSignalStrength())+" dB");
        textView_accu.setText(String.valueOf((int)location.getAccuracy()) + " m");

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView_location_icon);

        if (location.isUploaded()){
            imageView.setImageDrawable(convertView.getResources().getDrawable(R.drawable.cloudfull));
        }else {
            imageView.setImageDrawable(convertView.getResources().getDrawable(R.drawable.db));
        }

        return convertView;
    }
}
