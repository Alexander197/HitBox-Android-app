package com.example.boxproject11.data;

public class TrainingDbItem {
    public String trainingTitle;
    public String trainingDate;
    public String barChart;
    public int numberOfHits;
    public float averageImpactForce;
    public int strongestHit;
    public int numberOfSeries;
    public float hitsPerSeries;
    public String trainingDuration;
    public TrainingDbItem(String trainingTitle, String trainingDate, String barChart, int numberOfHits, float averageImpactForce, int strongestHit, int numberOfSeries, float hitsPerSeries, String trainingDuration){
        this.trainingTitle = trainingTitle;
        this.trainingDate = trainingDate;
        this.barChart = barChart;
        this.numberOfHits = numberOfHits;
        this.averageImpactForce = averageImpactForce;
        this.strongestHit = strongestHit;
        this.numberOfSeries = numberOfSeries;
        this.hitsPerSeries = hitsPerSeries;
        this.trainingDuration = trainingDuration;
    }
}
