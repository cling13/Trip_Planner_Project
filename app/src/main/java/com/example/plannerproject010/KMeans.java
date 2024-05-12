package com.example.plannerproject010;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KMeans {
    private int k;
    private List<double[]> data;
    private List<double[]> centroids;
    private int maxIterations = 100;

    public KMeans(int k, List<double[]> data) {
        this.k = k;
        this.data = data;
        this.centroids = new ArrayList<>();
        initializeCentroids();
    }

    private void initializeCentroids() {
        Random random = new Random();
        for (int i = 0; i < k; i++) {
            int idx = random.nextInt(data.size());
            centroids.add(data.get(idx));
        }
    }

    public List<List<double[]>> cluster() {
        List<List<double[]>> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            clusters.add(new ArrayList<>());
        }

        int iter = 0;

        while (iter < maxIterations) {
            clusters.forEach(List::clear);

            for (double[] point : data) {
                int closestClusterIdx = findClosestCluster(point);
                clusters.get(closestClusterIdx).add(point);
            }

            List<double[]> newCentroids = new ArrayList<>();
            for (List<double[]> cluster : clusters) {
                double[] centroid = calculateCentroid(cluster);
                newCentroids.add(centroid);
            }

            if (centroids.equals(newCentroids)) {
                break;
            } else {
                centroids.clear();
                centroids.addAll(newCentroids);
            }

            iter++;
        }

        return clusters;
    }

    private int findClosestCluster(double[] point) {
        double minDist = Double.MAX_VALUE;
        int closestClusterIdx = -1;

        for (int i = 0; i < centroids.size(); i++) {
            double[] centroid = centroids.get(i);
            double dist = euclideanDistance(point, centroid);
            if (dist < minDist) {
                minDist = dist;
                closestClusterIdx = i;
            }
        }

        return closestClusterIdx;
    }

    private double[] calculateCentroid(List<double[]> cluster) {
        double[] centroid = new double[cluster.get(0).length];
        for (int i = 0; i < centroid.length; i++) {
            double sum = 0.0;
            for (double[] point : cluster) {
                sum += point[i];
            }
            centroid[i] = sum / cluster.size();
        }
        return centroid;
    }

    private double euclideanDistance(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }
}