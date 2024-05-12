package com.example.plannerproject010;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.widget.Button;
import android.widget.ListView;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ItemClickListner {

    MyGoogleMap mainGoogleMap;
    LocationManager locationManager;
    SimpleAdapter totalPlanAdapter;
    PlanAdapter planAdapter;
    ArrayList<PlanClass> totalPlanList = new ArrayList<>(); //메인액티비티 플랜 저장하는 리스트
    ArrayList<listClass> msgBoxList;
    SQLiteDatabase sqlDB;
    MyDBHelper listDBHelper, mapDBHelper;
    public static Context context;
    Handler handler = new Handler();
    TextView textStartDate, textEndDate;
    DatePickerDialog startDatePicker, endDatePicker;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    int pos;

    ItemClickListner itemClickListner= new ItemClickListner() {
        @Override
        public void onItemClick(int position) {
        }

        @Override
        public void onItemBtnClick(int position) {
            pos=position;
            Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
            mStartForResult.launch(intent);
        }
    };

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
    result -> {
        // getResultCode가 0일 경우 세컨드 액티비티에서 넘어옴
        if (result.getResultCode() == 1) {
            ArrayList<listClass> tmp = new ArrayList<>();
            tmp = (ArrayList<listClass>) result.getData().getSerializableExtra("data");
            for (int i = 0; i < tmp.size(); i++) {
                addList(tmp.get(i));
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatePickerDialog.OnDateSetListener startDateListner =
                (datePicker, i, i1, i2) -> {
                    LocalDate startDate = LocalDate.of(i, i1 + 1, i2);
                    LocalDate endDate = LocalDate.parse(textEndDate.getText().toString(),formatter);

                    String text = startDate.format(formatter);
                    textStartDate.setText(text);

                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(i, i1, i2);

                    endDatePicker.getDatePicker().setMinDate(selectedDate.getTimeInMillis());

                    long numOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate);
                    List<LocalDate> dateBetween = IntStream.iterate(0, j -> j + 1)
                            .limit(numOfDaysBetween+1)
                            .mapToObj(j -> startDate.plusDays(j))
                            .collect(Collectors.toList());

                    totalPlanList.clear();
                    for(LocalDate localDate:dateBetween){

                        String date = localDate.format(formatter);

                        PlanClass tmp = new PlanClass(MainActivity.this,date);
                        totalPlanList.add(tmp);
                        planAdapter.notifyDataSetChanged();
                    }


                };

        DatePickerDialog.OnDateSetListener endDateListner =
                (datePicker, i, i1, i2) -> {
                    LocalDate startDate = LocalDate.parse(textStartDate.getText().toString(),formatter);
                    LocalDate endDate = LocalDate.of(i, i1 + 1, i2);

                    String text = endDate.format(formatter);
                    textEndDate.setText(text);

                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(i, i1, i2);

                    startDatePicker.getDatePicker().setMaxDate(selectedDate.getTimeInMillis());

                    long numOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate);
                    List<LocalDate> dateBetween = IntStream.iterate(0, j -> j + 1)
                            .limit(numOfDaysBetween+1)
                            .mapToObj(j -> startDate.plusDays(j))
                            .collect(Collectors.toList());


                    totalPlanList.clear();
                    for(LocalDate localDate:dateBetween) {

                        String date = localDate.format(formatter);

                        PlanClass tmp = new PlanClass(MainActivity.this, date);
                        totalPlanList.add(tmp);
                        planAdapter.notifyDataSetChanged();
                    }
                };

        Calendar calendar = Calendar.getInstance();
        startDatePicker = new DatePickerDialog(MainActivity.this,startDateListner, calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DATE));
        endDatePicker = new DatePickerDialog(MainActivity.this,endDateListner, calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DATE));

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
        //Button goSecActBtn = (Button) findViewById(R.id.plan_Add_Btn);
        RecyclerView totalPlanListView = findViewById(R.id.planList);
        totalPlanListView.setLayoutManager(new LinearLayoutManager(this));

        //리사이클러뷰 어댑터와 핸들러 연결
        totalPlanListView.setLayoutManager(new LinearLayoutManager(this));
        planAdapter = new PlanAdapter(this, totalPlanList,mapFragment, itemClickListner);
        totalPlanListView.setAdapter(planAdapter);

        //totalPlanAdapter = new SimpleAdapter(totalPlanList, this);
        //ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelperCallback(totalPlanAdapter));
        //helper.attachToRecyclerView(totalPlanListView);

        //세컨드 액티비티로 전환하는 버튼
//        goSecActBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
//                mStartForResult.launch(intent);
//            }
//        });

        Button btnMakePlan = (Button) findViewById(R.id.btnMakePlan);
        btnMakePlan.setOnClickListener(view -> {
            List<double[]> latLngs = new ArrayList<>();
            for(PlanClass plan: totalPlanList){
                ArrayList<listClass> planlist = plan.getPlanList();
                for(listClass item: planlist){
                    latLngs.add(new double[]{item.lat,item.lng});
                }
            }
            int k=2;
            KMeans kMeans = new KMeans(k,latLngs);
            List<List<double[]>> clusters = kMeans.cluster();

            for(int i=0; i<clusters.size(); i++)
            {
                List<double[]> cluster = clusters.get(i);
                Log.d("cluter" + (i+1),"Size: "+cluster.size());
                for(double[] point : cluster){
                    Log.d("Cluster"+(i+1),"Point: "+point[0]+","+point[1]);
                }
            }

        });

        textStartDate = (TextView) findViewById(R.id.textStartDate);
        textEndDate = (TextView) findViewById(R.id.textEndDate);

        LocalDate localDate = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE));
        String sDate = localDate.format(formatter);
        textStartDate.setText(sDate);
        textEndDate.setText(sDate);

        textStartDate.setOnClickListener(v-> {
            startDatePicker.show();
        });

        textEndDate.setOnClickListener(v -> {
            endDatePicker.show();
        });

    }

    //맵 초기설정
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mainGoogleMap = new MyGoogleMap(googleMap);
        mainGoogleMap.setgMap();
//
//        sqlDB= listDBHelper.getReadableDatabase();
//        String sql="select * from plantable WHERE date = '"+ localDate.toString() +"';";
//        Log.d("sql2",sql);
//        Cursor cursor = sqlDB.rawQuery(sql,null);
//         while(cursor.moveToNext()) {
//            totalPlanAdapter.addItemList(cursor.getString(1), MainActivity.this);
//        }
//        totalPlanAdapter.notifyDataSetChanged();
//        sqlDB.close();
    }

    //세컨드 액티비티에서 플랜 정보 받아와서 리스트에 추가해 주는 부분


    @Override
    public void onItemClick(int position) {
        //mainGoogleMap.moveCamera(totalPlanList.get(position).getlatLng());
    }

    @Override
    public void onItemBtnClick(int position) {

    }

    void addList(listClass tmp)
    {
        //latLngs.clear();
        //mainGoogleMap.clear();

        planAdapter.getItem(pos).planList.add(tmp);
        planAdapter.totalPlanAdapter.notifyDataSetChanged();
        //planAdapter.addList(tmp);
        planAdapter.notifyDataSetChanged();

        //for(listClass t : totalPlanList)
        //{
        //    latLngs.add(t.getlatLng());
        //    mainGoogleMap.addMark(t.getlatLng(),t.getName());
        //}
        //mainGoogleMap.addPolyline(latLngs);
    }
    void addDB(listClass tmp)
    {
//        sqlDB= listDBHelper.getReadableDatabase();
//        String sql="INSERT INTO plantable VALUES ('"+ localDate.toString() +"', '"+tmp.getId()+"');";
//        Log.d("sql",sql);
//        sqlDB.execSQL(sql);
//
//        final String urlStr="https://cling13.iwinv.net/insert_ok_place.php?" +
//                "date="+ localDate.toString() +
//                "&placeId="+tmp.getId();
//        Log.d("url",urlStr);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                requestUrl2(urlStr);
//            }
//        }).start();
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

    void setDate(TextView date) //날짜 설정
    {
//        mainGoogleMap.clear();
//
//        //읽어오기
//        totalPlanList.clear();
//        sqlDB= listDBHelper.getReadableDatabase();
//
//        String sql="select * from plantable WHERE date = '"+ localDate.toString() +"';";
//
//        Cursor cursor = sqlDB.rawQuery(sql,null);
//        while(cursor.moveToNext()) {
//            totalPlanAdapter.addItemList(cursor.getString(1), MainActivity.this);
//        }
//
//        totalPlanAdapter.notifyDataSetChanged();
//        sqlDB.close();
    }
}

