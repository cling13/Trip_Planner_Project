package com.example.plannerproject010;

import static com.example.plannerproject010.MainActivity.context;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MyGoogleMap {
    GoogleMap gMap;
    PolylineOptions polylineOptions;
    PlacesClient placesClient;

    MyGoogleMap(GoogleMap gMap) {
        this.gMap = gMap;
    }


    void setgMap() {
        LatLng defaultLocation = new LatLng(35.9645, 126.9801);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 16));

    }

    void addMark(LatLng latLng, String title) {
        gMap.addMarker(new MarkerOptions().position(latLng).title(title)/*.icon(BitmapDescriptorFactory.fromResource(R.drawable.makrer1))*/);
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16)); //마커로 카메라 이동
    }

    void markClear() {
        gMap.clear();
    }

    public void moveCamera(LatLng LatLng) {
        CameraUpdate newLatLng = CameraUpdateFactory.newLatLng(LatLng);
        gMap.moveCamera(newLatLng);
    }

    public void addPolyline(ArrayList<LatLng> latLngs) {
        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLUE);
        polylineOptions.width(5);
        polylineOptions.addAll(latLngs);
        gMap.addPolyline(polylineOptions);
    }

    public void clear() {
        gMap.clear();
    }

    
}
