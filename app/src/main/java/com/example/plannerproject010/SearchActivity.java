package com.example.plannerproject010;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements OnMapReadyCallback,ItemClickListner {

    public static Context context;
    SimpleAdapter placeSearchAdapter;
    MyGoogleMap secondGoogleMap;
    ArrayList<listClass> placeSearchList;
    ArrayList<listClass> intentList;
    ArrayList<listClass> templist;
    SimpleAdapter tempadapter;

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
                try {
                    searchLocation(5,placeSearchText.getText().toString(),placeSearchList,placeSearchAdapter);
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }
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

    public void searchLocation(int searchCnt, String locationName,ArrayList<listClass> listClass,SimpleAdapter simpleAdapter) throws PackageManager.NameNotFoundException {
        ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);

        //place 검색 요청 생성
        Places.initialize(getApplicationContext(), appInfo.metaData.getString("com.google.android.geo.API_KEY"));
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
                            secondGoogleMap.placeIdSearch(placeId, placesClient, listClass, simpleAdapter);
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