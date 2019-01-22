package com.thesis.sad.victimapp.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.thesis.sad.victimapp.R;

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {
    View myView;


    public CustomInfoWindow(Context context)
    {
        myView = LayoutInflater.from(context)
                .inflate(R.layout.custom_victim_info_window,null);

    }


    @Override
    public View getInfoWindow(Marker marker) {
        TextView txtPickUpTitle = myView.findViewById(R.id.txtPickupInfo);
        txtPickUpTitle.setText(marker.getTitle());

        TextView txtPickUpSnippet = myView.findViewById(R.id.txtPickupSnippet);
        txtPickUpSnippet.setText(marker.getSnippet());

        return myView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }


}
