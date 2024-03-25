package com.example.plannerproject010;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements OnMapReadyCallback,ItemClickListner {

    public static Context context;
    SimpleAdapter placeSearchAdapter;
    MyGoogleMap secondGoogleMap;
    ArrayList<listClass> placeSearchList;
    ArrayList<listClass> intentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        context=this;

        Button placeSearchBtn =(Button) findViewById(R.id.placeSearchBtn);
        EditText placeSearchText =(EditText) findViewById(R.id.placeSearchText);

        //초기 리스트와 어댑터 선언및 설정
        placeSearchList =new ArrayList<>();
        placeSearchAdapter =new SimpleAdapter(placeSearchList,this);
        ItemTouchHelper helper=new ItemTouchHelper(new ItemTouchHelperCallback(placeSearchAdapter));
        intentList = new ArrayList<>();

        RecyclerView recyclerSearchView=findViewById(R.id.placeSearchList);
        recyclerSearchView.setLayoutManager(new LinearLayoutManager(this));
        recyclerSearchView.setAdapter(placeSearchAdapter);
        helper.attachToRecyclerView(recyclerSearchView);

        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        //검색버튼 클릭 이벤트
        placeSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                placeSearchList.clear();
                searchLocation(5,placeSearchText.getText().toString(),placeSearchList,placeSearchAdapter);
            }
        });

        Button finishBtn = (Button) findViewById(R.id.fbtn);
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);

                intent.putExtra("data",intentList);
                setResult(1,intent);
                finish();
            }
        });
    }

    //map초기 설정
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        secondGoogleMap=new MyGoogleMap(googleMap);
        secondGoogleMap.setgMap();
    }

    public void searchLocation(int searchCnt, String locationName,ArrayList<listClass> listClass,SimpleAdapter simpleAdapter) {
        //place 검색 요청 생성
        Places.initialize(getApplicationContext(), "AIzaSyBw-QO4NsKeF4slw6fd2C48YnX03w5IbRA");
        PlacesClient placesClient = Places.createClient(this);
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        //place 검색 요청 설정
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(locationName)
                .setSessionToken(token)
                .build();

        //place 검색 요청 실행
        placesClient.findAutocompletePredictions(request).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {

                if (task.isSuccessful()) {
                    FindAutocompletePredictionsResponse response = task.getResult();
                    if (response != null) {
                        //검색정보 전체 리스트에 저장후 하나씩 반복
                        List<AutocompletePrediction> predictions = response.getAutocompletePredictions();

                        for (int i = 0; i < searchCnt; i++) {

                            AutocompletePrediction prediction = predictions.get(i);
                            String placeId = prediction.getPlaceId();

                            //place로 가져올 정보 선언
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
                                                listClass.add(tmp);
                                                simpleAdapter.notifyDataSetChanged();
                                            }).addOnFailureListener((exception) -> {
                                            });
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    //리스트에서 아이템 클릭시 해당 정보를 메인 액티비티로 전송해주는 부분
    @Override
    public void onItemClick(int position) {
        LatLng latLng=placeSearchList.get(position).getlatLng();
        secondGoogleMap.markClear();
        secondGoogleMap.addMark(latLng,placeSearchList.get(position).getName());
    }

    @Override
    public void onItemBtnClick(int position){
            listClass listPosition = placeSearchList.get(position);

            listClass tmp = new listClass(listPosition.getImage(), listPosition.getName(), listPosition.getAddress(), listPosition.getlatLng(), "주변 검색", listPosition.getId());
            intentList.add(tmp);

        }
}