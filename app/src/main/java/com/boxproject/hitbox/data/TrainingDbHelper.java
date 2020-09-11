package com.boxproject.hitbox.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.boxproject.hitbox.MyStatistic.StatisticListItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrainingDbHelper extends SQLiteOpenHelper {
    private final String TAG = "statisticTest";

    private static final String DATABASE_NAME = "training_data_base.db";
    private static final int DATABASE_VERSION = 1;

    public TrainingDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_TABLE = "CREATE TABLE " + TrainingsDbContract.TABLE_NAME + " ("
                + TrainingsDbContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TrainingsDbContract.COLUMN_TITLE + " TEXT NOT NULL, "
                + TrainingsDbContract.COLUMN_DATE + " TEXT NOT NULL, "
                + TrainingsDbContract.COLUMN_BAR_CHART + " TEXT NOT NULL, "
                + TrainingsDbContract.COLUMN_NUMBER_OF_HITS + " INTEGER NOT NULL DEFAULT 0, "
                + TrainingsDbContract.COLUMN_AVERAGE_IMPACT_FORCE + " REAL NOT NULL DEFAULT 0,"
                + TrainingsDbContract.COLUMN_STRONGEST_HIT + " INTEGR NOT NULL DEFAULT 0, "
                + TrainingsDbContract.COLUMN_NUMBER_OF_SERIES + " INTEGER NOT NULL DEFAULT 0, "
                + TrainingsDbContract.COLUMN_HITS_PER_SERIES + " REAL NOT NULL DEFAULT 0, "
                + TrainingsDbContract.TRAINING_DURATION + " TEXT NOT NULL);";
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrainingsDbContract.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
    public long insertTrainingItem(TrainingDbItem trainingDbItem)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrainingsDbContract.COLUMN_TITLE, trainingDbItem.trainingTitle);
        values.put(TrainingsDbContract.COLUMN_DATE, trainingDbItem.trainingDate);
        values.put(TrainingsDbContract.COLUMN_BAR_CHART, trainingDbItem.barChart);
        values.put(TrainingsDbContract.COLUMN_NUMBER_OF_HITS, trainingDbItem.numberOfHits);
        values.put(TrainingsDbContract.COLUMN_AVERAGE_IMPACT_FORCE, trainingDbItem.averageImpactForce);
        values.put(TrainingsDbContract.COLUMN_STRONGEST_HIT, trainingDbItem.strongestHit);
        values.put(TrainingsDbContract.COLUMN_NUMBER_OF_SERIES, trainingDbItem.numberOfSeries);
        values.put(TrainingsDbContract.COLUMN_HITS_PER_SERIES, trainingDbItem.hitsPerSeries);
        values.put(TrainingsDbContract.TRAINING_DURATION, trainingDbItem.trainingDuration);
        return sqLiteDatabase.insert(TrainingsDbContract.TABLE_NAME,null, values);
    }

    public void deleteTrainingItemByIndex(int index)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Log.i(TAG, "Number of rows delete: " + sqLiteDatabase.delete(TrainingsDbContract.TABLE_NAME, "_id = " + getTrainingItemIdByIndex(index), null));
    }
    public void deleteTrainingItemById(int id){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Log.i(TAG, "Number of rows delete: " + sqLiteDatabase.delete(TrainingsDbContract.TABLE_NAME, "_id = " + id, null));
    }

    public void updateTrainingItemByIndex(int index, String column, String newValue){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column, newValue);
        Log.i(TAG, "Number of rows update: " + sqLiteDatabase.update(TrainingsDbContract.TABLE_NAME, contentValues, "_id = " + getTrainingItemIdByIndex(index), null));
    }
    public void updateTrainingItemById(int id, String column, String newValue){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column, newValue);
        Log.i(TAG, "Number of rows update: " + sqLiteDatabase.update(TrainingsDbContract.TABLE_NAME, contentValues, "_id = " + id, null));
    }

    public int getTrainingItemIdByIndex(int index){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String[] projection = {TrainingsDbContract._ID};
        Cursor cursor = sqLiteDatabase.query(TrainingsDbContract.TABLE_NAME, projection, null, null, null, null, null);
        int id;
        try {
            cursor.moveToPosition(index);
            id = cursor.getInt(cursor.getColumnIndex(TrainingsDbContract._ID));
        } finally {
            cursor.close();
        }
        return id;
    }

    public int getLastId(){
        int lastId;
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String[] projection = {
                TrainingsDbContract._ID};
        Cursor cursor = sqLiteDatabase.query(TrainingsDbContract.TABLE_NAME, projection, null, null, null, null, null);
        try{
            if(cursor.moveToLast())
            lastId = cursor.getInt(cursor.getColumnIndex(TrainingsDbContract._ID));
            else lastId = 0;
        }
        finally {
            cursor.close();
        }
        return lastId;
    }

    public TrainingDbItem getTrainingDBItemByIndex(int index) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String[] projection = {
                TrainingsDbContract._ID,
                TrainingsDbContract.COLUMN_TITLE,
                TrainingsDbContract.COLUMN_DATE,
                TrainingsDbContract.COLUMN_BAR_CHART,
                TrainingsDbContract.COLUMN_NUMBER_OF_HITS,
                TrainingsDbContract.COLUMN_AVERAGE_IMPACT_FORCE,
                TrainingsDbContract.COLUMN_STRONGEST_HIT,
                TrainingsDbContract.COLUMN_NUMBER_OF_SERIES,
                TrainingsDbContract.COLUMN_HITS_PER_SERIES,
                TrainingsDbContract.TRAINING_DURATION
        };
        Cursor cursor = sqLiteDatabase.query(TrainingsDbContract.TABLE_NAME, projection, null, null, null, null, null);
        TrainingDbItem trainingDbItem;
        try {
            cursor.moveToPosition(index);
            trainingDbItem = new TrainingDbItem(cursor.getString(cursor.getColumnIndex(TrainingsDbContract.COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndex(TrainingsDbContract.COLUMN_DATE)),
                    cursor.getString(cursor.getColumnIndex(TrainingsDbContract.COLUMN_BAR_CHART)),
                    cursor.getInt(cursor.getColumnIndex(TrainingsDbContract.COLUMN_NUMBER_OF_HITS)),
                    cursor.getFloat(cursor.getColumnIndex(TrainingsDbContract.COLUMN_AVERAGE_IMPACT_FORCE)),
                    cursor.getInt(cursor.getColumnIndex(TrainingsDbContract.COLUMN_STRONGEST_HIT)),
                    cursor.getInt(cursor.getColumnIndex(TrainingsDbContract.COLUMN_NUMBER_OF_SERIES)),
                    cursor.getFloat(cursor.getColumnIndex(TrainingsDbContract.COLUMN_HITS_PER_SERIES)),
                    cursor.getString(cursor.getColumnIndex(TrainingsDbContract.TRAINING_DURATION)));
        } finally {
            cursor.close();
        }
        return trainingDbItem;
    }
    public TrainingDbItem getTrainingDbItemById(int id){
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String[] projection = {
                TrainingsDbContract._ID,
                TrainingsDbContract.COLUMN_TITLE,
                TrainingsDbContract.COLUMN_DATE,
                TrainingsDbContract.COLUMN_BAR_CHART,
                TrainingsDbContract.COLUMN_NUMBER_OF_HITS,
                TrainingsDbContract.COLUMN_AVERAGE_IMPACT_FORCE,
                TrainingsDbContract.COLUMN_STRONGEST_HIT,
                TrainingsDbContract.COLUMN_NUMBER_OF_SERIES,
                TrainingsDbContract.COLUMN_HITS_PER_SERIES,
                TrainingsDbContract.TRAINING_DURATION
        };
        Cursor cursor = sqLiteDatabase.query(TrainingsDbContract.TABLE_NAME, projection, "_id = " + id, null, null, null, null);
        TrainingDbItem trainingDbItem;
        try {
            cursor.moveToNext();
            trainingDbItem = new TrainingDbItem(cursor.getString(cursor.getColumnIndex(TrainingsDbContract.COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndex(TrainingsDbContract.COLUMN_DATE)),
                    cursor.getString(cursor.getColumnIndex(TrainingsDbContract.COLUMN_BAR_CHART)),
                    cursor.getInt(cursor.getColumnIndex(TrainingsDbContract.COLUMN_NUMBER_OF_HITS)),
                    cursor.getFloat(cursor.getColumnIndex(TrainingsDbContract.COLUMN_AVERAGE_IMPACT_FORCE)),
                    cursor.getInt(cursor.getColumnIndex(TrainingsDbContract.COLUMN_STRONGEST_HIT)),
                    cursor.getInt(cursor.getColumnIndex(TrainingsDbContract.COLUMN_NUMBER_OF_SERIES)),
                    cursor.getFloat(cursor.getColumnIndex(TrainingsDbContract.COLUMN_HITS_PER_SERIES)),
                    cursor.getString(cursor.getColumnIndex(TrainingsDbContract.TRAINING_DURATION)));
        } finally {
            cursor.close();
        }
        return trainingDbItem;
    }
    public TrainingDbItem getLastTrainingDbItem()
    {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String[] projection = {
                TrainingsDbContract._ID,
                TrainingsDbContract.COLUMN_TITLE,
                TrainingsDbContract.COLUMN_DATE,
                TrainingsDbContract.COLUMN_BAR_CHART,
                TrainingsDbContract.COLUMN_NUMBER_OF_HITS,
                TrainingsDbContract.COLUMN_AVERAGE_IMPACT_FORCE,
                TrainingsDbContract.COLUMN_STRONGEST_HIT,
                TrainingsDbContract.COLUMN_NUMBER_OF_SERIES,
                TrainingsDbContract.COLUMN_HITS_PER_SERIES,
                TrainingsDbContract.TRAINING_DURATION
        };
        Cursor cursor = sqLiteDatabase.query(TrainingsDbContract.TABLE_NAME, projection, null, null, null, null, null);
        TrainingDbItem trainingDbItem;
        try {
            if(cursor.getCount() > 0){
            cursor.moveToLast();
            trainingDbItem = new TrainingDbItem(cursor.getString(cursor.getColumnIndex(TrainingsDbContract.COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndex(TrainingsDbContract.COLUMN_DATE)),
                    cursor.getString(cursor.getColumnIndex(TrainingsDbContract.COLUMN_BAR_CHART)),
                    cursor.getInt(cursor.getColumnIndex(TrainingsDbContract.COLUMN_NUMBER_OF_HITS)),
                    cursor.getFloat(cursor.getColumnIndex(TrainingsDbContract.COLUMN_AVERAGE_IMPACT_FORCE)),
                    cursor.getInt(cursor.getColumnIndex(TrainingsDbContract.COLUMN_STRONGEST_HIT)),
                    cursor.getInt(cursor.getColumnIndex(TrainingsDbContract.COLUMN_NUMBER_OF_SERIES)),
                    cursor.getFloat(cursor.getColumnIndex(TrainingsDbContract.COLUMN_HITS_PER_SERIES)),
                    cursor.getString(cursor.getColumnIndex(TrainingsDbContract.TRAINING_DURATION)));
            }
            else return null;
        } finally {
            cursor.close();
        }
        return trainingDbItem;
    }
    public float[] getColumnAverage(String[] projection){
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor= sqLiteDatabase.query(TrainingsDbContract.TABLE_NAME, projection, null, null, null, null, null);
        float[] results = new float[projection.length];
        Arrays.fill(results, 0);
        int counter = 0;
        try{
            int[] columns = new int[projection.length];
            for(int i = 0; i < projection.length; i++){
                columns[i] = cursor.getColumnIndex(projection[i]);
            }
            for(; cursor.moveToNext(); counter++){
                for(int i = 0; i < results.length; i++){
                    switch (cursor.getType(columns[i])){
                        case Cursor.FIELD_TYPE_INTEGER:
                            results[i] += cursor.getInt(columns[i]);
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            results[i] += cursor.getFloat(columns[i]);
                            break;
                    }
                }
            }
        }
        finally {
            cursor.close();
        }
        if(counter != 0){
            for(int i = 0; i < results.length; i++)
                results[i] /= (float)counter;
        }
        return results;
    }
    public int dbLength(){
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(TrainingsDbContract.TABLE_NAME, null, null, null, null, null, null);
        int length = 0;
        try {
            length = cursor.getCount();
        }
        finally {
            cursor.close();
        }
        return length;
    }
    public List<StatisticListItem> getTrainingDbList(){
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        List<StatisticListItem> statisticListItems = new ArrayList<>();
        String[] projection = {
                TrainingsDbContract._ID,
                TrainingsDbContract.COLUMN_TITLE,
                TrainingsDbContract.COLUMN_DATE
        };
        Cursor cursor = sqLiteDatabase.query(TrainingsDbContract.TABLE_NAME, projection, null, null, null, null, null);
        try{
            int columnId = cursor.getColumnIndex(TrainingsDbContract._ID);
            int columnTitle = cursor.getColumnIndex(TrainingsDbContract.COLUMN_TITLE);
            int columnDate = cursor.getColumnIndex(TrainingsDbContract.COLUMN_DATE);
            while(cursor.moveToNext()){
                statisticListItems.add(new StatisticListItem(cursor.getString(columnTitle), cursor.getString(columnDate), cursor.getInt(columnId)));
            }
        }
        finally {
            cursor.close();
        }
        return statisticListItems;
    }
    public List<StatisticListItem> searchInDb(String constraint){
        if(constraint == null || constraint.length() == 0){
            return getTrainingDbList();
        }
        else{
            SQLiteDatabase sqLiteDatabase = getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select * from " + TrainingsDbContract.TABLE_NAME + " where " + TrainingsDbContract.COLUMN_TITLE + " like ?", new String[]{"%" + constraint + "%"});
            List<StatisticListItem> statisticListItems = new ArrayList<>();
            try{
                int columnId = cursor.getColumnIndex(TrainingsDbContract._ID);
                int columnTitle = cursor.getColumnIndex(TrainingsDbContract.COLUMN_TITLE);
                int columnDate = cursor.getColumnIndex(TrainingsDbContract.COLUMN_DATE);
                while(cursor.moveToNext()){
                    statisticListItems.add(new StatisticListItem(cursor.getString(columnTitle), cursor.getString(columnDate), cursor.getInt(columnId)));
                }
            }
            finally {
                cursor.close();
            }
            return  statisticListItems;
        }
    }
}
