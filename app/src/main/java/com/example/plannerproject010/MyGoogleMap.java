package com.example.plannerproject010;

import static com.example.plannerproject010.MainActivity.context;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

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

    void findNearbyRestaurants(LatLng currentLocation) {
        // Use a list of place fields to define the data types to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS);

        Places.initialize(context, "AIzaSyCQEHHAP6BPVkQoSWMXArg8DtS7zDXDAVA");
        placesClient = Places.createClient(context);

        ArrayList<listClass> list = new ArrayList<>();


        // Create a NearbySearchRequest using currentLocation
        PlacesClient placesClient = Places.createClient(context);
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        // Set up the Places API request for nearby restaurants
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(currentLocation.latitude - 0.05, currentLocation.longitude - 0.05),
                new LatLng(currentLocation.latitude + 0.05, currentLocation.longitude + 0.05)
        );

        // Define a request for nearby places of type 'restaurant'
        FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                .setLocationBias(bounds)
                .setQuery("rest")
                .build();


        // Use the Places Client to find predictions for nearby restaurants
        placesClient.findAutocompletePredictions(predictionsRequest).addOnSuccessListener(response -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                String placeId = prediction.getPlaceId();
                FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeId, placeFields).build();

                placesClient.fetchPlace(placeRequest).addOnSuccessListener(fetchPlaceResponse -> {
                    Place place = fetchPlaceResponse.getPlace();
                    if (true) {
                        LatLng placeLatLng = place.getLatLng();
                        String placeName = place.getName();
                        String placeAddress = place.getAddress();
                        AtomicReference<Bitmap> bitmap = new AtomicReference<>();

                        // Fetch photo metadata if available
                        List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();
                        if (photoMetadataList != null && !photoMetadataList.isEmpty()) {
                            PhotoMetadata photoMetadata = photoMetadataList.get(0);

                            // Fetch the photo
                            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                                    .setMaxWidth(500)  // Optional
                                    .setMaxHeight(500)  // Optional
                                    .build();

                            placesClient.fetchPhoto(photoRequest).addOnSuccessListener(fetchPhotoResponse -> {
                                bitmap.set(fetchPhotoResponse.getBitmap());

                            }).addOnFailureListener(exception -> {
                                // Handle the failure case
                            });
                        }
                        listClass tmp;
                        if (bitmap == null) {
                            tmp = new listClass(null, placeName, placeAddress, placeLatLng, "추가", placeId);
                        } else {
                            tmp = new listClass(bitmap.get(), placeName, placeAddress, placeLatLng, "추가", placeId);
                        }

                        list.add(tmp);

                        Log.d("음식점",list.get(list.size()-1).name);
                    }
                }).addOnFailureListener(exception -> {
                    // Handle the failure case
                });
            }
        }).addOnFailureListener(exception -> {
            Log.d("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",exception.toString());
        });
    }
}
