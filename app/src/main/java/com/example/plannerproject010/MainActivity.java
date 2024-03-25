package com.example.plannerproject010;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ItemClickListner {

    MyGoogleMap mainGoogleMap;
    LocationManager locationManager;
    SimpleAdapter totalPlanAdapter;
    PolylineOptions polylineOptions;
    ArrayList<listClass> totalPlanList = new ArrayList<>(); //메인액티비티 플랜 저장하는 리스트
    ArrayList<listClass> msgBoxList;
    //double currentLat,currentLog,currentTime;
    SQLiteDatabase sqlDB;
    MyDBHelper listDBHelper, mapDBHelper;
    public static Context context;
    //ArrayList<LatLng> LatLngList;
    //static boolean sw=false;
    static String date = "2024-03-21";
    Handler handler = new Handler();


    ItemClickListner itemClickListner= new ItemClickListner() {
        @Override
        public void onItemClick(int position) {
        }

        @Override
        public void onItemBtnClick(int position) {
            addDB(msgBoxList.get(position));
            addList(msgBoxList.get(position));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //LatLngList = new ArrayList<LatLng>();
        context=this;
        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);

        ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                result->{
                    Boolean fineLocationGranted=result.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION,false);
                    Boolean coarseLocationGranted=result.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION,false);
                    if(fineLocationGranted!=null&&fineLocationGranted){
                        Toast.makeText(getApplicationContext(), "자세한 위치 권한이 허용됨", Toast.LENGTH_SHORT).show();
                    }
                    else if(coarseLocationGranted!=null && coarseLocationGranted){
                        Toast.makeText(getApplicationContext(), "대략적인 위치 권한이 허용됨", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "위치 권한이 허용되지 않음", Toast.LENGTH_SHORT).show();
                    }
                });

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)!=
                        PackageManager.PERMISSION_GRANTED){
            locationPermissionRequest.launch(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }

        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1,locationListener);


        listDBHelper = new MyDBHelper(this,"plantable",null,1);
        mapDBHelper = new MyDBHelper(this,"movetable",null,1);
        //맵 연결
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mainMapFragment);
        mapFragment.getMapAsync(this);

        //xml 아이디 연결
        Button goSecActBtn = (Button) findViewById(R.id.plan_Add_Btn);
        RecyclerView totalPlanListView = findViewById(R.id.planList);

        //리사이클러뷰 어댑터와 핸들러 연결
        totalPlanListView.setLayoutManager(new LinearLayoutManager(this));
        totalPlanAdapter = new SimpleAdapter(totalPlanList, this);
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelperCallback(totalPlanAdapter));
        totalPlanListView.setAdapter(totalPlanAdapter);
        helper.attachToRecyclerView(totalPlanListView);

        //세컨드 액티비티로 전환하는 버튼
        goSecActBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                mStartForResult.launch(intent);
            }
        });


        TextView dateText = (TextView) findViewById(R.id.dateText);
        dateText.setText(date);

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View datepicker_msg_box = (View) getLayoutInflater().inflate(R.layout.datepicker_msg_box, null);
                AlertDialog.Builder dlg=new AlertDialog.Builder(MainActivity.this);
                DatePicker datePicker = (DatePicker) datepicker_msg_box.findViewById(R.id.datePicker) ;

                dlg.setView(datepicker_msg_box);
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        date = Integer.toString(datePicker.getYear())+"-"+Integer.toString(datePicker.getMonth())+"-"+Integer.toString(datePicker.getDayOfMonth());

                        dateText.setText((CharSequence) date);

                        mainGoogleMap.clear();

                        //읽어오기
                        totalPlanList.clear();
                        sqlDB= listDBHelper.getReadableDatabase();
                        String sql="select * from plantable WHERE date = '"+date+"';";
                        Log.d("sql2",sql);
                        Cursor cursor = sqlDB.rawQuery(sql,null);
                        while(cursor.moveToNext()){
                            totalPlanAdapter.addItemList(cursor.getString(1),MainActivity.this);
                        }
                        totalPlanAdapter.notifyDataSetChanged();
                        sqlDB.close();
                    }
                });
                dlg.show();
            }
        });

        /*
        Button btn=(Button) findViewById(R.id.button2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sqlDB= listDBHelper.getWritableDatabase();
                listDBHelper.onUpgrade(sqlDB,1,1);
                sqlDB.close();
                sqlDB=mapDBHelper.getWritableDatabase();
                mapDBHelper.onUpgrade(sqlDB,1,1);
                sqlDB.close();

                mainGoogleMap.clear();
                totalPlanList.clear();
                totalPlanAdapter.notifyDataSetChanged();
            }
        });

        Button btnStart=(Button) findViewById(R.id.button3);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!sw){
                    sw=true;
                    LatLngList.clear();
                    btnStart.setText("종료");
                }
                else {
                    sw = false;
                    LatLngList.clear();
                    mainGoogleMap.clear();
                    btnStart.setText("시작");
                }
            }
        });

        Button btnview=(Button) findViewById(R.id.button4);
        btnview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sqlDB= mapDBHelper.getReadableDatabase();
                String sql="select * from movetable WHERE date = '"+date+"';";
                Cursor cursor = sqlDB.rawQuery(sql,null);
                mainGoogleMap.clear();
                LatLngList.clear();
                while(cursor.moveToNext()){
                    LatLng latLng=new LatLng(cursor.getDouble(1),cursor.getDouble(2));
                    Log.d("latlng",Double.toString(latLng.latitude));
                    Log.d("latlng",Double.toString(latLng.longitude));
                    polylineOptions = new PolylineOptions();
                    polylineOptions.color(Color.RED);
                    polylineOptions.width(5);
                    LatLngList.add(latLng);
                    polylineOptions.addAll(LatLngList);
                    mainGoogleMap.addPolyline(polylineOptions);
                    mainGoogleMap.moveCamera(latLng);
                }

                totalPlanAdapter.notifyDataSetChanged();
                sqlDB.close();
            }
        });

        Button btnWeb=(Button) findViewById(R.id.button5);
        btnWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String urlStr="https://cling13.iwinv.net/json.php";
                Log.d("url",urlStr);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        requestUrl2(urlStr);
                    }
                }).start();
            }
        });

         */

    }

    //맵 초기설정
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mainGoogleMap = new MyGoogleMap(googleMap);
        mainGoogleMap.setgMap();
    }

    //세컨드 액티비티에서 플랜 정보 받아와서 리스트에 추가해 주는 부분
    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {

                // getResultCode가 0일 경우 세컨드 액티비티에서 넘어옴
                if (result.getResultCode() == 1) {
                    ArrayList<listClass> tmp = new ArrayList<>();
                    tmp = (ArrayList<listClass>) result.getData().getSerializableExtra("data");
                    for(int i=0 ; i<tmp.size(); i++) {
                        addDB(tmp.get(i));
                        addList(tmp.get(i));
                    }
                }
            });

    @Override
    public void onItemClick(int position) {
        mainGoogleMap.moveCamera(totalPlanList.get(position).getlatLng());
    }

    @Override
    public void onItemBtnClick(int position) {
        String msg = totalPlanList.get(position).getName() + " 주변의 여행지 3곳만 알려줘 추가 설명 없이 이름만 말해줘";

        View search_msg_box = (View) getLayoutInflater().inflate(R.layout.search_msg_box, null);
        msgBoxList = new ArrayList<>();
        SimpleAdapter msgBoxAdapter = new SimpleAdapter(msgBoxList, itemClickListner);
        RecyclerView msgBoxListView = (RecyclerView) search_msg_box.findViewById(R.id.msgBoxList);
        msgBoxListView.setLayoutManager(new LinearLayoutManager(this));
        msgBoxListView.setAdapter(msgBoxAdapter);

        //gpt에서 추천 여행지 받아오기
        GptCallback gptCallback = new GptCallback();
        CompletableFuture<String> apiResponseFuture = gptCallback.callAPI(msg);

        // CompletableFuture의 thenAcceptAsync 메서드를 사용하여 API 응답을 처리
        apiResponseFuture.thenAcceptAsync(response -> {
            // API 호출 성공 시 처리 (response 변수에 API 응답이 들어 있습니다)
            Log.d("API Response", response);

            runOnUiThread(() -> {
                try {
                    JSONObject responseObject = new JSONObject(response);
                    JSONArray choicesArray = responseObject.getJSONArray("choices");

                    // choices 배열의 첫 번째 객체 가져오기
                    JSONObject firstChoice = choicesArray.getJSONObject(0);

                    // 첫 번째 객체에서 "text" 속성 가져오기
                    String gptReturnText = firstChoice.getString("text");

                    int[] idx = new int[3];
                    String[] resText = new String[3];
                    idx[0] = gptReturnText.indexOf("1.");
                    idx[1] = gptReturnText.indexOf("2.");
                    idx[2] = gptReturnText.indexOf("3.");
                    resText[0] = gptReturnText.substring(idx[0] + 3, idx[1]).replace(System.getProperty("line.separator"), "");
                    resText[1] = gptReturnText.substring(idx[1] + 3, idx[2]).replace(System.getProperty("line.separator"), "");
                    resText[2] = gptReturnText.substring(idx[2] + 3).replace(System.getProperty("line.separator"), "");

                    AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                    dlg.setTitle("주변 추천 여행지");
                    dlg.setView(search_msg_box);
                    msgBoxList.clear();
                    for (String gptmsg : resText) {
                        ((SearchActivity) SearchActivity.context).searchLocation(1, gptmsg, msgBoxList, msgBoxAdapter);
                    }
                    dlg.setPositiveButton("닫기",null);
                    dlg.show();

                } catch (Exception e) {
                    Log.d("error", e.toString());
                }
                ;
            });
        }).exceptionally(e -> {
            // API 호출 실패 시 처리
            Log.e("API Error", "Error occurred: " + e.getMessage());
            return null;
        });
    }

    void addList(listClass tmp)
    {
        tmp.setBtnName("주변 검색");
        totalPlanList.add(tmp);
        totalPlanAdapter.notifyDataSetChanged();
        mainGoogleMap.addMark(tmp.getlatLng(), tmp.getName());
    }
    void addDB(listClass tmp)
    {
        sqlDB= listDBHelper.getReadableDatabase();
        String sql="INSERT INTO plantable VALUES ('"+date+"', '"+tmp.getId()+"');";
        Log.d("sql",sql);
        sqlDB.execSQL(sql);

        final String urlStr="https://cling13.iwinv.net/insert_ok_place.php?" +
                "date="+date+
                "&placeId="+tmp.getId();
        Log.d("url",urlStr);

        new Thread(new Runnable() {
            @Override
            public void run() {
                requestUrl2(urlStr);
            }
        }).start();
    }

    /*
    final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            String provider = location.getProvider();
            currentLat=location.getLatitude();
            currentLog=location.getLongitude();
            currentTime=System.currentTimeMillis();

            LatLng latLng=new LatLng(currentLat,currentLog);


            if(sw) {
                mainGoogleMap.moveCamera(latLng);
                polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.RED);
                polylineOptions.width(5);
                LatLngList.add(latLng);
                polylineOptions.addAll(LatLngList);
                mainGoogleMap.addPolyline(polylineOptions);

                sqlDB= mapDBHelper.getWritableDatabase();
                String sql="INSERT INTO movetable VALUES ('"+date+"', '"+currentLat+"', '"+currentLog+"');";
                sqlDB.execSQL(sql);
                sqlDB.close();

                final String urlStr="https://cling13.iwinv.net/insert_ok_latlng.php?" +
                        "date="+date+
                        "&lat="+currentLat+
                        "&lng="+currentLog;
                Log.d("url",urlStr);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        requestUrl2(urlStr);
                    }
                }).start();
            }
        }
    };

     */

    public void requestUrl2(String urlStr) {
        StringBuilder output = new StringBuilder();

        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection != null) {
                connection.setConnectTimeout(10000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                String line = null;
                while (true) {
                    line = reader.readLine();
                    if (line == null) break;
                    output.append(line + "\n");
                }
                Log.d("output",output.toString());
                reader.close();
                connection.disconnect();
            }
        } catch (Exception e) {
        }
        println(output.toString());
    }


    public void println(final String data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                jsonParsing(data);
            }
        });
    }

    private void jsonParsing(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray phoneBookArray = jsonObject.getJSONArray("placetable");

            sqlDB= listDBHelper.getWritableDatabase();
            listDBHelper.onUpgrade(sqlDB,1,1);

            for (int i = 0; i < phoneBookArray.length(); i++) {
                JSONObject phoneBookObject = phoneBookArray.getJSONObject(i);
                String date = phoneBookObject.getString("date");
                String id = phoneBookObject.getString("placeId");
                Log.d("date",date);
                String sql = "insert into plantable values ('"
                        + date +"', '"+ id +"')";
                sqlDB.execSQL(sql);
            }
            sqlDB.close();

            phoneBookArray = jsonObject.getJSONArray("LatLngtable");

            sqlDB= mapDBHelper.getWritableDatabase();
            mapDBHelper.onUpgrade(sqlDB,1,1);

            for (int i = 0; i < phoneBookArray.length(); i++) {
                JSONObject phoneBookObject = phoneBookArray.getJSONObject(i);
                String date = phoneBookObject.getString("date");
                String lat = phoneBookObject.getString("lat");
                String lng = phoneBookObject.getString("lng");
                String sql = "insert into plantable values ('"
                        + date +"', '"+ lat +"','"+lng+"')";
                Log.d("SQL", sql);
                sqlDB.execSQL(sql);
            }
            sqlDB.close();

            totalPlanAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

