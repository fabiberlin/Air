package de.baensch.airsniffer.gui;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.baensch.airsniffer.R;
import de.baensch.airsniffer.db.Device;

/**
 * Created by Fabian on 16.01.2017.
 */

public class DeviceCursorAdapter extends CursorAdapter {

    public DeviceCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.device_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView txt_name = (TextView) view.findViewById(R.id.textView_device_name);
        TextView txt_address = (TextView) view.findViewById(R.id.textView_device_address);
        TextView txt_latitude = (TextView) view.findViewById(R.id.textView_device_lat);
        TextView txt_longitude = (TextView) view.findViewById(R.id.textView_device_long);
        TextView txt_timestamp = (TextView) view.findViewById(R.id.textView_device_timestamp);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView_device_icon);


        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
        String longitude = cursor.getString(cursor.getColumnIndexOrThrow("longitude"));
        String latitude = cursor.getString(cursor.getColumnIndexOrThrow("latitude"));
        String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
        String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));


        // Populate fields with extracted properties
        txt_name.setText(name);
        txt_address.setText(address);
        txt_longitude.setText(longitude);
        txt_latitude.setText(latitude);

        long timestampLong = Long.parseLong(timestamp);
        txt_timestamp.setText(getDate(timestampLong ));

        switch (type){
            case Device.TYPE_BTEDR:
                imageView.setImageDrawable(view.getResources().getDrawable(R.drawable.bluetoothedr));
                break;
            case Device.TYPE_BTLE:
                imageView.setImageDrawable(view.getResources().getDrawable(R.drawable.bluetoothle));
                break;
            case Device.TYPE_WIFI:
                imageView.setImageDrawable(view.getResources().getDrawable(R.drawable.wifi));
                break;
        }

    }

    private String getDate(long timeStamp){

        try{
            DateFormat sdf = new SimpleDateFormat("dd.MM.yyyy - HH:mm");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }
}
