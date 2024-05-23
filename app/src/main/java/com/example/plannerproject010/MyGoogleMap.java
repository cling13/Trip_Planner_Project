package com.example.plannerproject010;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    public static double[] getTransitRoute(final String origin, final String destination) {
        final double[][] result = {new double[2]};
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    result[0] = fetchTransitRoute(origin, destination);
                    Log.d(TAG, "Transit time: " + result[0][0]);
                } catch (IOException e) {
                    Log.e(TAG, "IOException while fetching transit route", e);
                }
            }
        });
        thread.start();
        try{
            thread.join();
        }catch (InterruptedException e){
            Log.e(TAG,e.toString());
        }
        return result[0];
    }

    public static double[] fetchTransitRoute(String origin, String destination) throws IOException {

        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=place_id:" + origin + "&destinations=place_id:" + destination+ "&region=KR&key=AIzaSyCQEHHAP6BPVkQoSWMXArg8DtS7zDXDAVA";
        Log.d("urlurlurlurlurlurlurlulrulrulrulr",url.toString());
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String result = response.body().string();
                JSONObject jsonObject = new JSONObject(result);
                JSONArray rows = jsonObject.getJSONArray("rows");
                if (rows.length() > 0) {
                    JSONObject row = rows.getJSONObject(0);
                    JSONArray elements = row.getJSONArray("elements");
                    JSONObject element = elements.getJSONObject(0);
                    JSONObject duration = element.optJSONObject("duration");
                    double[] durationText = new double[2];
                    durationText[0]= duration.getDouble("value") / 3600 * 1.5;
                    JSONObject distance = element.optJSONObject("distance");
                    durationText[1]= distance.getDouble("value") / 1000;

                    return durationText;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException while fetching transit route", e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        double[] d={99,99};
        return d;
    }

}
