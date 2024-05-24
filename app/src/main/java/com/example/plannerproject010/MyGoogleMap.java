package com.example.plannerproject010;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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


    public static void placeIdSearch(String placeId, PlacesClient placesClient, ArrayList<listClass> list, SimpleAdapter simpleAdapter)
    {
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.PHOTO_METADATAS // 사진 메타데이터 필드
        );

        //한개의 place로 정보 받아온뒤 저장
        placesClient.fetchPlace(FetchPlaceRequest.builder(placeId, placeFields).build()).addOnCompleteListener(new OnCompleteListener<FetchPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FetchPlaceResponse> task) {

                if (task.isSuccessful()) {
                    FetchPlaceResponse fetchPlaceResponse = task.getResult();
                    Place place = fetchPlaceResponse.getPlace();

                    LatLng placeLatLng = place.getLatLng();
                    String placeName = place.getName();
                    String placeAddress = place.getAddress();

                    //사진 정보 가져오기
                    List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();

                    if (photoMetadataList != null && !photoMetadataList.isEmpty()) {

                        //사진 메타데이터 가져오기
                        PhotoMetadata photoMetadata = photoMetadataList.get(0);

                        //사진 크기 설정
                        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                                .setMaxWidth(500) // Optional.
                                .setMaxHeight(500) // Optional.
                                .build();

                        //리스트에 여행지 정보 추가
                        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                            Bitmap bitmap = fetchPhotoResponse.getBitmap();
                            listClass tmp = new listClass(bitmap, placeName, placeAddress, placeLatLng, "추가", placeId);
                            Log.d("name",placeName);
                            Log.d("id",placeId);
                            Log.d("lat",Double.toString(placeLatLng.latitude));
                            Log.d("lng",Double.toString(placeLatLng.longitude));
                            list.add(tmp);
                            simpleAdapter.notifyDataSetChanged();
                        }).addOnFailureListener((exception) -> {
                        });
                    }
                }
            }
        });
    }
}
