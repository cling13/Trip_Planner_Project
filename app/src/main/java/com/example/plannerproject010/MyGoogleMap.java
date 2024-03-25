package com.example.plannerproject010;

import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MyGoogleMap {
    GoogleMap gMap;

    PolylineOptions polylineOptions;

    MyGoogleMap(GoogleMap gMap){
        this.gMap=gMap;
        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLUE);
        polylineOptions.width(5);
    }


    void setgMap()
    {
        LatLng defaultLocation = new LatLng(35.9450, 126.6828);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation,16));

    }

    void addMark(LatLng latLng, String title)
    {
        gMap.addMarker(new MarkerOptions().position(latLng).title(title).icon(BitmapDescriptorFactory.fromResource(R.drawable.makrer1)));
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,16)); //마커로 카메라 이동
    }

    void markClear()
    {
        gMap.clear();
    }

    public void moveCamera(LatLng LatLng) {
        CameraUpdate newLatLng = CameraUpdateFactory.newLatLng(LatLng);
        gMap.moveCamera(newLatLng);
    }

    public void addPolyline(ArrayList<LatLng> latLngs) {
        polylineOptions.addAll(latLngs);
        gMap.addPolyline(polylineOptions);
    }

    public void clear() {
        gMap.clear();
    }
}
