package com.boxproject.hitbox.data;

import android.provider.BaseColumns;

public class TrainingsDbContract implements BaseColumns {
    public static final String TABLE_NAME = "training_db_table";

    public static final String _ID = BaseColumns._ID;
    public static final String COLUMN_TITLE = "training_title";
    public static final String COLUMN_DATE = "training_date";
    public static final String COLUMN_BAR_CHART = "training_bar_chart";
    public static final String COLUMN_NUMBER_OF_HITS = "number_of_hits";
    public static final String COLUMN_AVERAGE_IMPACT_FORCE = "impact_force";
    public static final String COLUMN_STRONGEST_HIT = "strongest_hit";
    public static final String COLUMN_NUMBER_OF_SERIES = "series_of_hit";
    public static final String COLUMN_HITS_PER_SERIES = "hits_per_series";
    public static final String TRAINING_DURATION = "training_duration";
}
