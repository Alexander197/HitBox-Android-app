package com.example.boxproject11.MyStatistic;

public class StatisticListItem {
    private String title;
    private String date;
    private int id;

    public StatisticListItem(String title, String date, int id) {
        this.title = title;
        this.date = date;
        this.id = id;
    }
    public String getTitle()
    {
        return this.title;
    }
    public String getDate()
    {
        return this.date;
    }
    public int getId(){
        return this.id;
    }
}
