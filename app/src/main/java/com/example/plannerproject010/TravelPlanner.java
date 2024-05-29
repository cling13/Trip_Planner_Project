package com.example.plannerproject010;

import static com.example.plannerproject010.MainActivity.context;

import android.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

public class TravelPlanner {
    List<Destination> destinations = new ArrayList<>();

     class Destination {
        String name;
        String type;
        double lat;
        double lon;
        double time;

        Destination(String name, double lat, double lon, double time, String type) {
            this.name = name;
            this.type = type;
            this.lat = lat;
            this.lon = lon;
            this.time = time;
        }
    }

    public  double[] haversine(String val1, String val2) {
        double[] dou = MyGoogleMap.getTransitRoute(val1,val2);
        Log.d("doubledoubledoubledoubledouble",Double.toString(dou[0])+Double.toString(dou[1]));
        return dou;
    }

    public double haversine2(double lat1, double lng1, double lat2, double lng2)
    {
        double EARTH_RADIUS_KM=6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public  List<List<Destination>> generateAllCombinations(List<Destination> destinations) {
        List<List<Destination>> allCombinations = new ArrayList<>();
        int n = destinations.size();
        for (int r = 1; r <= n; r++) {
            combinations(destinations, new ArrayList<>(), allCombinations, 0, r);
        }
        return allCombinations;
    }

    private  void combinations(List<Destination> destinations, List<Destination> temp, List<List<Destination>> allCombinations, int start, int r) {
        if (temp.size() == r) {
            allCombinations.add(new ArrayList<>(temp));
            return;
        }
        for (int i = start; i < destinations.size(); i++) {
            temp.add(destinations.get(i));
            combinations(destinations, temp, allCombinations, i + 1, r);
            temp.remove(temp.size() - 1);
        }
    }

    public  double[] calculateCourseTime(List<Destination> course, boolean lunch, boolean dinner, double startTime, int type){
        double totalTime = startTime;
        double totalDistance = 0;

        for (int i = 0; i < course.size(); i++) {
            Destination current = course.get(i);
            totalTime += course.get(i).time;

            if(current.type == "restaurant")
            {
                if(totalTime >= 11 && totalTime <= 13){
                    lunch = true;
                }
                else{
                    lunch = false;
                }
                if(totalTime >=18 && totalTime<=20){
                    dinner = true;
                }
                else{
                    dinner = false;
                }
            }

            if (i < course.size() - 1) {
                if(type == 1) {
                    double[] distance = haversine(course.get(i).name, course.get(i + 1).name);
                    totalTime += distance[0];
                    totalDistance += distance[1];
                    lunch = true;
                    dinner = true;
                }
                else if(type == 2) {
                    double distance = haversine2(course.get(i).lat, course.get(i).lon, course.get(i + 1).lat, course.get(i + 1).lon);
                    distance /= 60;
                    totalTime += distance;
                }
            }
        }

        if(!lunch || !dinner)
        {
            return new double[]{Double.MAX_VALUE,totalDistance};
        }

        return new double[]{totalTime - startTime, totalDistance};
    }

    public  List<Destination> findBestCourse(List<Destination> destinations, double totalTravelTime) {
        List<List<Destination>> allCombinations = generateAllCombinations(destinations);
        List<Destination> bestCourse = new ArrayList<>();
        double bestTime = 0;
        boolean lunch = true;
        boolean dinner = true;

        if(8<13)
            lunch = false;
        if(8+totalTravelTime > 18)
            dinner = false;

        for (List<Destination> combo : allCombinations) {
            double[] courseTimeAndDistance = calculateCourseTime(combo,lunch,dinner,8.0,2);
            double courseTime = courseTimeAndDistance[0];

            if (courseTime <= totalTravelTime && courseTime > bestTime) {
                bestCourse = combo;
                bestTime = courseTime;
            }
        }

        return bestCourse;
    }

    public void start(double totalityTime) throws IOException {

        double totalTravelTime = totalityTime; // 총 여행 시간 (시간 단위)
        ArrayList<listClass> tmp=new ArrayList<>();
        Places.initialize(context.getApplicationContext(), "AIzaSyCQEHHAP6BPVkQoSWMXArg8DtS7zDXDAVA");
        PlacesClient placesClient = Places.createClient(context);
        View v = View.inflate(context,R.layout.make_plan,null);

        RecyclerView makePlanRecyclerView = (RecyclerView) v.findViewById(R.id.list);
        TextView timeText = (TextView) v.findViewById(R.id.totaltime);
        TextView disText = (TextView) v.findViewById(R.id.totaldis);
        List<Destination> bestCourse = findBestCourse(destinations, totalTravelTime);

        makePlanRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        SimpleAdapter simpleAdapter = new SimpleAdapter(tmp,null);
        makePlanRecyclerView.setAdapter(simpleAdapter);

        if (!bestCourse.isEmpty()) {
            Log.d("aaaaaaaaaaaaa","추천 여행 코스:");
            bestCourse.forEach(place -> Log.d("aaaaaaaaaaaa",place.name + " (관광 소요 시간: " + place.time + "시간)"));

            bestCourse.forEach(place -> MyGoogleMap.placeIdSearch(place.name,placesClient,tmp,simpleAdapter));

            double[] bestCourseTimeAndDistance = calculateCourseTime(bestCourse,true,true,8.0,1);
            timeText.setText("총 여행 시간"+bestCourseTimeAndDistance[0]+"시간");
            disText.setText("이동 거리"+bestCourseTimeAndDistance[1]+"km");

            Log.d("총 소요 시간: " ,String.format("%.2f", bestCourseTimeAndDistance[0]) + "시간");
            Log.d("총 이동 거리: ", String.format("%.2f", bestCourseTimeAndDistance[1]) + "km");
        } else {
            Log.d("aaaaaaaaaa","주어진 시간 내에 가능한 여행 코스를 찾을 수 없습니다.");
        }

        AlertDialog.Builder dlg = new AlertDialog.Builder(context);
        dlg.setView(v);
        dlg.show();
    }

    public void add(String id, double lat, double lng, double time, String type)
    {
        destinations.add(new Destination(id,lat,lng,time,type));
    }
}