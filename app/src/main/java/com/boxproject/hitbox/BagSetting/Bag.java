package com.boxproject.hitbox.BagSetting;

import android.content.Context;
import android.content.SharedPreferences;

public class Bag {
    Context context;
    public Bag (Context c)
    {
        context = c;
        if(context != null)
            sharedPreference = context.getSharedPreferences(BAG_PREFERENCES, Context.MODE_PRIVATE);
    }
    private String KEY_BAG_WEIGHT = "BAG_WEIGHT";
    private String KEY_THRESHOLD = "THRESHOLD";
    private String BAG_PREFERENCES = "BAG_PREFERENCES";

    public static int MAX_BAG_WEIGHT = 100;
    public static int MIN_BAG_WEIGHT = 30;
    public static int START_BAG_WEIGHT= 50;

    public static int MAX_THRESHOLD = 400;
    public static int MIN_THRESHOLD = 150;
    public static int START_THRESHOLD = 160;

    public static int BAR_CHART_LENGTH = 30;
    public static int BAR_CHART_STEP = 25;

    private SharedPreferences sharedPreference;

    public void setBagSettings(int bagWeight, int threshold)
    {
        sharedPreference = context.getSharedPreferences(BAG_PREFERENCES, Context.MODE_PRIVATE);
        if(sharedPreference != null)
        {
            SharedPreferences.Editor editor = sharedPreference.edit();
            editor.putInt(KEY_BAG_WEIGHT, bagWeight);
            editor.putInt(KEY_THRESHOLD, threshold);
            editor.apply();
        }
    }
    public BagSetting getBagSettings()
    {
        if(sharedPreference != null)
            return new BagSetting(sharedPreference.getInt(KEY_BAG_WEIGHT, START_BAG_WEIGHT), sharedPreference.getInt(KEY_THRESHOLD, START_THRESHOLD));
        else
            return new BagSetting();
    }
}
