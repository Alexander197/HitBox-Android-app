package com.boxproject.hitbox.MyTraining;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.boxproject.hitbox.BagSetting.Bag;
import com.boxproject.hitbox.BagSetting.BagSetting;
import com.boxproject.hitbox.BluetoothLeService;
import com.boxproject.hitbox.R;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;


public class TrainingService extends BluetoothLeService {

    public static final String TIMER_TICK =
            "com.example.bluetooth.le.TIMER_TICK";

    String TAG = "trainingServiceTest";
    private int currentImpactForce = 0;
    private int numberOfHits = 0;
    private float averageImpactForce = 0;
    private int strongestHit = 0;
    private int numberOfSeries = 0;
    private float hitsPerSeries = 0;

    private int impactForceSum = 0;
    private int hitsPerSeriesSum = 0;
    private int[] hitsArray = new int[Bag.BAR_CHART_LENGTH];

    public boolean trainingState = false;

    private long time = 0;
    private String strTime = "00:00:00";
    private Timer timer;
    private TimerTask timerTask;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        registerReceiver(mBroadcastReceiver, mIntentFilter());
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        unregisterReceiver(mBroadcastReceiver);
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new TrainingService.LocalBinder();

    public class LocalBinder extends Binder {
        public TrainingService getService() {
            return TrainingService.this;
        }
    }

    public void startTraining(BagSetting bagSetting) {
        stopTraining();
        trainingState = true;
        sendData(getTrainingSettings((byte)1, bagSetting.bagWeight, bagSetting.threshold));
        startTimer();
    }
    public void stopTraining(){
        trainingState = false;
        if(checkDeviceConnection() == BluetoothAdapter.STATE_CONNECTED){
            stopTimer();
            sendData(getTrainingSettings((byte)0));
            currentImpactForce = 0;
            numberOfHits = 0;
            averageImpactForce = 0;
            strongestHit = 0;
            numberOfSeries = 0;
            hitsPerSeries = 0;
            impactForceSum = 0;
            hitsPerSeriesSum = 0;
            Arrays.fill(hitsArray, 0);
        }
    }
    public void stopTrainingForResult(){
        if(stopTrainingListener != null)
            stopTrainingListener.onStopTraining(getTrainingDataItem(), getBarChartString(hitsArray, '-'), strTime);
        stopTraining();
    }
    private StopTrainingListener stopTrainingListener;
    public interface StopTrainingListener{
        void onStopTraining(TrainingDataItem trainingDataItem, String barChart, String duration);
    }
    public void setOnStopTrainingListener(StopTrainingListener stopTrainingListener){
        this.stopTrainingListener = stopTrainingListener;
    }

    private void startTimer(){
        time = 0;
        if(timer != null)
            timer.cancel();
        timer = new Timer();
        timerTask = new MyTimerTask();
        timer.schedule(timerTask, 1000, 1000);
    }
    private void stopTimer(){
        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            time += 1000;
            strTime = getString(R.string.timer_values,time / 3600000, time / 60000 % 60, time / 1000 % 60);
            broadcastUpdate(TIMER_TICK, strTime);
        }
    }


    private static IntentFilter mIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        return  intentFilter;
    }
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action == ACTION_DATA_AVAILABLE){
                int[] ints = intent.getIntArrayExtra(EXTRA_DATA);
                if(ints[0] == 1){
                    currentImpactForce = ints[1];
                numberOfHits ++;
                impactForceSum += ints[1];
                averageImpactForce = (float)impactForceSum / (float)numberOfHits;
                if(strongestHit < ints[1])
                    strongestHit = ints[1];
                for(int i = 0; i < Bag.BAR_CHART_LENGTH; i++){
                    if(ints[1] >= Bag.MIN_THRESHOLD + i * Bag.BAR_CHART_STEP && ints[1] < Bag.MIN_THRESHOLD + (i + 1) * Bag.BAR_CHART_STEP) {
                        hitsArray[i] ++;
                        break;
                    }
                    else if(ints[1] >= Bag.MIN_THRESHOLD + Bag.BAR_CHART_STEP * Bag.BAR_CHART_LENGTH){
                        hitsArray[hitsArray.length - 1] ++;
                        break;
                    }
                }
                    Log.i(TAG, "Bar chart: " + getBarChartString(hitsArray, '-'));
                } else if (ints[0] == 2) {
                    numberOfSeries ++;
                    hitsPerSeriesSum += ints[1];
                    hitsPerSeries = (float)hitsPerSeriesSum / (float)numberOfSeries;
                }
                Log.i(TAG, "Data received in training service" );
                if(trainingDataListener != null)
                    trainingDataListener.onTrainingDataReceived(getTrainingDataItem());
            }
        }
    };

    private TrainingDataListener trainingDataListener;
    public interface TrainingDataListener{
        void onTrainingDataReceived(TrainingDataItem trainingDataItem);
    }
    public void setOnTrainingDataListener(TrainingDataListener trainingDataListener){
        this.trainingDataListener = trainingDataListener;
    }

    public TrainingDataItem getTrainingDataItem(){
        return new TrainingDataItem(currentImpactForce, numberOfHits, averageImpactForce, strongestHit, numberOfSeries,  hitsPerSeries);
    }

    private String getBarChartString(int[] hitsArray, char key){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < hitsArray.length; i++){
            if(i < hitsArray.length - 1){
                builder.append(hitsArray[i]);
                builder.append(key);
            }
            else builder.append(hitsArray[i]);
        }
        return builder.toString();
    }

    private void broadcastUpdate(final String action, final String stringValue) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, stringValue);
        sendBroadcast(intent);
    }

    private byte[] getTrainingSettings(byte extra, int weight, int threshold){
        byte[] bytes = new byte[5];
        bytes[0] = extra;
        System.arraycopy(getByte(weight),0, bytes, 1, 2);
        System.arraycopy(getByte(threshold),0, bytes, 3, 2);
        return bytes;
    }
    private byte[] getTrainingSettings(byte extra){
        byte[] bytes = new byte[1];
        bytes[0] = extra;
        return bytes;
    }
    private byte[] getByte(int integer){
        byte[] bytes = new byte[2];
        bytes[0] = (byte)(integer & 0xFF);
        bytes[1] = (byte)((integer >> 8) & 0xFF);
        return bytes;
    }

}
