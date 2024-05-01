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
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ItemClickListner {

    MyGoogleMap mainGoogleMap;
    LocationManager locationManager;
    SimpleAdapter totalPlanAdapter;
    ArrayList<listClass> totalPlanList = new ArrayList<>(); //메인액티비티 플랜 저장하는 리스트
    ArrayList<listClass> msgBoxList;
    SQLiteDatabase sqlDB;
    MyDBHelper listDBHelper, mapDBHelper;
    ArrayList<LatLng> latLngs = new ArrayList<>();
    public static Context context;
    static LocalDate localDate;
    Handler handler = new Handler();
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    TextView textStartDate, textEndDate;


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

        Date d = new Date();
        format.format(d);
        localDate=d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        context=this;
        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);

        ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                result->{
                    Boolean fineLocationGranted=result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION,false);
                    Boolean coarseLocationGranted=result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION,false);
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
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)!=
                        PackageManager.PERMISSION_GRANTED){
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }

        View ivMapTransparent = (View) findViewById(R.id.ivMapTransparent);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);

        ivMapTransparent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        return false;
                    case MotionEvent.ACTION_UP:
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        return false;
                    default:
                        return true;
                }
            }
        });

        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1,locationListener);


        listDBHelper = new MyDBHelper(this,"plantable",null,1);

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


        textStartDate = (TextView) findViewById(R.id.textStartDate);
        textEndDate = (TextView) findViewById(R.id.textEndDate);
        textStartDate.setText(localDate.toString());
        textEndDate.setText(localDate.toString());

        textStartDate.setOnClickListener(v-> {
            setDate(textStartDate);
        });

        textEndDate.setOnClickListener(v -> {
            setDate(textEndDate);
        });

    }

    //맵 초기설정
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mainGoogleMap = new MyGoogleMap(googleMap);
        mainGoogleMap.setgMap();

        sqlDB= listDBHelper.getReadableDatabase();
        String sql="select * from plantable WHERE date = '"+ localDate.toString() +"';";
        Log.d("sql2",sql);
        Cursor cursor = sqlDB.rawQuery(sql,null);
        while(cursor.moveToNext()) {
            totalPlanAdapter.addItemList(cursor.getString(1), MainActivity.this);
        }
        totalPlanAdapter.notifyDataSetChanged();
        sqlDB.close();
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

    }

    void addList(listClass tmp)
    {
        latLngs.clear();
        //mainGoogleMap.clear();

        tmp.setBtnName("주변 검색");
        totalPlanList.add(tmp);
        totalPlanAdapter.notifyDataSetChanged();

        for(listClass t : totalPlanList)
        {
            latLngs.add(t.getlatLng());
            mainGoogleMap.addMark(t.getlatLng(),t.getName());
        }
        mainGoogleMap.addPolyline(latLngs);
    }
    void addDB(listClass tmp)
    {
        sqlDB= listDBHelper.getReadableDatabase();
        String sql="INSERT INTO plantable VALUES ('"+ localDate.toString() +"', '"+tmp.getId()+"');";
        Log.d("sql",sql);
        sqlDB.execSQL(sql);

        final String urlStr="https://cling13.iwinv.net/insert_ok_place.php?" +
                "date="+ localDate.toString() +
                "&placeId="+tmp.getId();
        Log.d("url",urlStr);

        new Thread(new Runnable() {
            @Override
            public void run() {
                requestUrl2(urlStr);
            }
        }).start();
    }


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

    void setDate(TextView textView) //날짜 설정
    {
        View datepicker_msg_box = (View) getLayoutInflater().inflate(R.layout.datepicker_msg_box, null);
        AlertDialog.Builder dlg=new AlertDialog.Builder(MainActivity.this);
        DatePicker datePicker = (DatePicker) datepicker_msg_box.findViewById(R.id.datePicker) ;

        dlg.setView(datepicker_msg_box);
        dlg.setPositiveButton("확인", (dialog, which) -> {
            localDate = LocalDate.of(datePicker.getYear(),datePicker.getMonth()+1,datePicker.getDayOfMonth());

            textView.setText(localDate.toString());
            try {
                if(format.parse((String) textEndDate.getText()).compareTo(format.parse((String) textStartDate.getText()))<0)
                    textStartDate=textEndDate;
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            mainGoogleMap.clear();

            //읽어오기
            totalPlanList.clear();
            sqlDB= listDBHelper.getReadableDatabase();

            String sql="select * from plantable WHERE date = '"+ localDate.toString() +"';";

            Cursor cursor = sqlDB.rawQuery(sql,null);
            while(cursor.moveToNext()) {
                totalPlanAdapter.addItemList(cursor.getString(1), MainActivity.this);
            }

            totalPlanAdapter.notifyDataSetChanged();
            sqlDB.close();
        });
        dlg.show();
    }
}

