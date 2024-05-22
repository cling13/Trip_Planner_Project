package com.example.plannerproject010;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TravelPlanner {
    private  final double EARTH_RADIUS_KM = 6371.0;
    private  final double TRAVEL_SPEED = 60.0; // km/h
    List<Destination> destinations = new ArrayList<>();

     class Destination {
        String name;
        double lat;
        double lon;
        double time;

        Destination(String name, double lat, double lon, double time) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
            this.time = time;
        }
    }

    public  double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dlat = Math.toRadians(lat2 - lat1);
        double dlon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dlon / 2) * Math.sin(dlon / 2);
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

    public  double[] calculateCourseTime(List<Destination> course) {
        double totalTime = 0;
        double totalDistance = 0;

        for (int i = 0; i < course.size(); i++) {
            totalTime += course.get(i).time;
            if (i < course.size() - 1) {
                double distance = haversine(course.get(i).lat, course.get(i).lon, course.get(i + 1).lat, course.get(i + 1).lon);
                double travelTime = distance / TRAVEL_SPEED;
                totalTime += travelTime;
                totalDistance += distance;
            }
        }
        return new double[]{totalTime, totalDistance};
    }

    public  List<Destination> findBestCourse(List<Destination> destinations, double totalTravelTime) {
        List<List<Destination>> allCombinations = generateAllCombinations(destinations);
        List<Destination> bestCourse = new ArrayList<>();
        double bestTime = 0;
        double bestDistance = 0;

        for (List<Destination> combo : allCombinations) {
            double[] courseTimeAndDistance = calculateCourseTime(combo);
            double courseTime = courseTimeAndDistance[0];
            double courseDistance = courseTimeAndDistance[1];

            if (courseTime <= totalTravelTime && courseTime > bestTime) {
                bestCourse = combo;
                bestTime = courseTime;
                bestDistance = courseDistance;
            }
        }
        return bestCourse;
    }

    public void start(double totalityTime) {

        double totalTravelTime = totalityTime; // 총 여행 시간 (시간 단위)

        List<Destination> bestCourse = findBestCourse(destinations, totalTravelTime);

        if (!bestCourse.isEmpty()) {
            Log.d("aaaaaaaaaaaaa","추천 여행 코스:");
            bestCourse.forEach(place -> Log.d("aaaaaaaaaaaa",place.name + " (관광 소요 시간: " + place.time + "시간)"));
            double[] bestCourseTimeAndDistance = calculateCourseTime(bestCourse);
            Log.d("총 소요 시간: " ,String.format("%.2f", bestCourseTimeAndDistance[0]) + "시간");
            Log.d("총 이동 거리: ", String.format("%.2f", bestCourseTimeAndDistance[1]) + "km");
        } else {
            Log.d("aaaaaaaaaa","주어진 시간 내에 가능한 여행 코스를 찾을 수 없습니다.");
        }
    }

    public void add(String id, double lat, double lng, double time)
    {
        destinations.add(new Destination(id,lat,lng,time));
    }
}