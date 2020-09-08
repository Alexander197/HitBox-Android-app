package com.example.boxproject11.MyTraining;

public class TrainingDataItem {
    public int currentImpactForce;
    public int numberOfHits;
    public float averageImpactForce;
    public int strongestHit;
    public int numberOfSeries;
    public float hitsPerSeries;
    public TrainingDataItem(int currentImpactForce, int numberOfHits, float averageImpactForce, int strongestHit, int numberOfSeries, float hitsPerSeries){
        this.currentImpactForce = currentImpactForce;
        this.numberOfHits = numberOfHits;
        this.averageImpactForce = averageImpactForce;
        this.strongestHit = strongestHit;
        this.numberOfSeries = numberOfSeries;
        this.hitsPerSeries = hitsPerSeries;
    }
}
