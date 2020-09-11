package com.boxproject.hitbox;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.boxproject.hitbox.MyDevice.MyDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {
    String TAG = "bleServiceTest";

//    public static final String ACTION_DEVICE_FOUNDED =
//            "com.example.bluetooth.le.ACTION_DEVICE_FOUNDED";
    public static final String ACTION_DEVICE_NOT_FOUNDED =
            "com.example.bluetooth.le.ACTION_DEVICE_NOT_FOUNDED";
    public static final String ACTION_STOP_SCANNING =
            "com.example.bluetooth.le.ACTION_STOP_SCANNING";
    public static final String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_CONNECTING =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTING";
    public static final String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public static final String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public static final String BATTERY_LEVEL_UPDATE =
            "com.example.bluetooth.le.BATTERY_LEVEL_UPDATE";
    public static final String RSSI_READ =
            "com.example.bluetooth.le.RSSI_READ";

    public static final String SEARCHING_STATE_KEY = "SEARCH_STATE_KEY";
    public static final String CONNECTION_STATE_KEY = "CONNECTION_STATE_KEY";

    private BluetoothGattCharacteristic TX_CHAR;
    private BluetoothGattCharacteristic RX_CHAR;
    private BluetoothGattCharacteristic BATTERY_LEVEL_CHAR;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothScanner;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothDevice device;

    private int initState = -3;
    private boolean searchingState = false;
    private int connectionState = BluetoothAdapter.STATE_DISCONNECTED;

    private int batteryLevel = -1;

    private final Handler handler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    public int initialize() {
        if(mBluetoothManager == null)
        {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if(mBluetoothManager == null) {
                initState = -3;
                return -3;
            }
        }
        if(mBluetoothAdapter == null)
        {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if(mBluetoothAdapter == null){
                initState = -2;
                return -2;
            }
        }
        if(mBluetoothScanner == null && mBluetoothAdapter.isEnabled())
        {
            mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if(mBluetoothScanner == null) {
                initState = -1;
                return -1;
            }
        }
        else if(!mBluetoothAdapter.isEnabled()) {
            initState = 0;
            return 0;
        }
        initState = 1;
        return 1;
    }

    public int startScanning(int scanPeriod)
    {
        ////////////////////////////////////////////////////////****
        if(!searchingState && mBluetoothAdapter.isEnabled()) {
            initialize();
            if(device != null)checkDeviceConnection();
            if ((connectionState == BluetoothAdapter.STATE_DISCONNECTED) && initState == 1) {
                searchingState = true;
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "start scan");
                        mBluetoothScanner.startScan(mScanCallback);
                    }
                });
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopScanning(false);
                    }
                }, scanPeriod);
                return 1;
            }
        } else if(mBluetoothAdapter.isEnabled()){
            Log.i(TAG, "startScanning: scan is in progress");
        }
        else if (!mBluetoothAdapter.isEnabled()) {
            searchingState = false;
            return 2;
        }
        return 0;
    }

    public interface ScanDeviceListener{
        void scanResult(BluetoothDevice device);
    }
    private ScanDeviceListener scanDeviceListener;
    public void setOnScanDeviceListener (ScanDeviceListener listener)
    {
        this.scanDeviceListener = listener;
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            //Log.i(TAG, "onScanResult: " + result.toString());
            if(scanDeviceListener != null)
                scanDeviceListener.scanResult(result.getDevice());
        }
    };
    private void stopScanning(boolean success)
    {
        if(searchingState && mBluetoothAdapter.isEnabled())
        {
            if(!success)
                broadcastUpdate(ACTION_DEVICE_NOT_FOUNDED);
            else broadcastUpdate(ACTION_STOP_SCANNING);
            searchingState = false;
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "stop scan");
                    mBluetoothScanner.stopScan(mScanCallback);
                }
            });
        }
    }
    public void connectBleDevice(BluetoothDevice device) {
        this.device = device;
        if (device != null && mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT) == BluetoothProfile.STATE_DISCONNECTED && mBluetoothAdapter.isEnabled()) {
            stopScanning(true);
            mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
            connectionState = BluetoothAdapter.STATE_CONNECTING;
            broadcastUpdate(ACTION_GATT_CONNECTING);
        }
    }
    public boolean disconnectBleDevice()
    {
        if (device != null && mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED && mBluetoothAdapter.isEnabled()) {
            mBluetoothGatt.disconnect();
            connectionState = BluetoothAdapter.STATE_DISCONNECTED;
            broadcastUpdate(ACTION_GATT_DISCONNECTED);
            return true;
        }
        return false;
    }
    public int checkDeviceConnection()
    {
        if(mBluetoothAdapter.isEnabled()) {
            if(device != null){
                connectionState = mBluetoothManager.getConnectionState(device, BluetoothGatt.GATT);
                return connectionState;
            }
            else return -1;
        }
        return -2;
    }
    public Bundle getDeviceStates()
    {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SEARCHING_STATE_KEY, searchingState);
        bundle.putInt(CONNECTION_STATE_KEY, connectionState);
        return  bundle;
    }

    public BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.i(TAG, "onConnectionStateChange: " + newState);
            if(newState == BluetoothProfile.STATE_CONNECTED) {
                mBluetoothGatt.discoverServices();
                //Log.i(TAG,"Device connected. Discover services status: " + mBluetoothGatt.discoverServices());
            }
            else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionState = BluetoothAdapter.STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if(status == BluetoothGatt.GATT_SUCCESS)
            {
                Log.i(TAG, "onServicesDiscovered:");
                characteristicNotificationEnable();
                //characteristicNotificationEnable();
                //Log.i(TAG,"RXch: " + RX_CHAR.getUuid().toString());
                //Log.i(TAG,"TXch: " + TX_CHAR.getUuid().toString());
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if(status == BluetoothGatt.GATT_SUCCESS)
            {

            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] receivedData = characteristic.getValue();
            switch (characteristic.getUuid().toString().toUpperCase()){
                case MyDevice.BATTERY_LEVEL_CHAR_UUID:
                    batteryLevel = byteToInt(receivedData[0], receivedData[1]);
                    broadcastUpdate(BATTERY_LEVEL_UPDATE, batteryLevel);
                    Log.i(TAG, "Battery level: " + batteryLevel);
                    break;
                case MyDevice.TRAINING_DATA_CHAR_UUID:
                    if(receivedData.length == 1 && receivedData[0] == 1) {
                        connectionState = BluetoothAdapter.STATE_CONNECTED;
                        broadcastUpdate(ACTION_GATT_CONNECTED);
                    }
                    else if(receivedData.length == 5){
                        int[] ints = {(int)receivedData[0], byteToInt(receivedData[1], receivedData[2]), byteToInt(receivedData[3] ,receivedData[4])};
                        broadcastUpdate(ACTION_DATA_AVAILABLE, ints);
                    }
//                    switch (receivedData.length){
//                        case 5:
//                            if(receivedData[0] == 1)
//                                dataReceiver.onReceivedA(byteToInt(receivedData[1], receivedData[2]), byteToInt(receivedData[3] ,receivedData[4]));
//                            else if(receivedData[0] == 2)
//                                dataReceiver.onReceivedB(byteToInt(receivedData[1], receivedData[2]), byteToInt(receivedData[3] ,receivedData[4]));
//                            Log.i(TAG, "Training data received");
//                            break;
//                        case 1:
//                            break;
//                    }
                    break;
            }
        }
        private int byteToInt(byte lowByte, byte highByte)
        {
            int result=0;
            for(int i=0;i<16;i++)
            {
                if(i<8)
                    result |=lowByte&(1L<<i);
                else
                    result |=(highByte&(1L<<(i-8)))<<8;
            }
            return result;
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i(TAG, "onDescriptorWrite: " + descriptor.getUuid().toString());
            descriptors.remove(0);
            writeDescriptors();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            writeCharData.remove(0);
            writeCharInProgress = false;
            writeCharacteristic();
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.i("infoFragmentTest", "onReadRemoteRssi: " + rssi);
            broadcastUpdate(RSSI_READ, rssi);
        }
    };
    public boolean readRSSI(){
        if(mBluetoothGatt != null)
            return  mBluetoothGatt.readRemoteRssi();
        else return false;
    }

//    private DataReceiver dataReceiver;
//    public interface DataReceiver{
//        void onReceivedA(int impactForce, int hits);
//        void onReceivedB(int seriesHits, int seriesNumber);
//    }
//    public void setOnDataReceiver(DataReceiver dataReceiver){
//        this.dataReceiver = dataReceiver;
//    }

    public int getBatteryLevel(){
        return this.batteryLevel;
    }

    private List<byte[]> writeCharData = new ArrayList<>();
    private boolean addDataToWrite(byte[] bytes){
        if(checkDeviceConnection() == BluetoothAdapter.STATE_CONNECTED) {
            return writeCharData.add(bytes);
        }
        else return false;
    }
    private boolean writeCharInProgress = false;
    private boolean writeCharacteristic(){
        if(!writeCharInProgress) {
            if (!writeCharData.isEmpty() && TX_CHAR != null && mBluetoothGatt != null) {
                TX_CHAR.setValue(writeCharData.get(0));
                writeCharInProgress = true;
                return mBluetoothGatt.writeCharacteristic(TX_CHAR);
            } else if (writeCharData.isEmpty()) {
                Log.i(TAG, "All characteristic data transmitted");
                return false;
            }
        }
        return false;
    }
    public boolean sendData(byte[] bytes){
        if(!addDataToWrite(bytes)) return false;
        return writeCharacteristic();
    }

    List<BluetoothGattDescriptor> descriptors = new ArrayList<>();
    private void writeDescriptors(){
        if(!descriptors.isEmpty() && mBluetoothGatt != null){
            descriptors.get(0).setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            Log.i(TAG, "Descriptor write: " + mBluetoothGatt.writeDescriptor(descriptors.get(0)));
        }
        else if (mBluetoothGatt != null){
//            byte[] bytes = new byte[1];
//            bytes[0] = 2;
//            sendData(bytes);
            Log.i(TAG, "All Descriptors write");
        }
    }
    private void characteristicNotificationEnable()
    {
        BluetoothGattService boxProjectService = mBluetoothGatt.getService(UUID.fromString(MyDevice.BOXPROJECT_SERVICE_UUID));
        if(boxProjectService!=null)
        {
            for(BluetoothGattCharacteristic bgc: boxProjectService.getCharacteristics())
                Log.i(TAG, "BOX_PROJECT_CHARACTERISTIC: " + bgc.getUuid().toString());
            BATTERY_LEVEL_CHAR = boxProjectService.getCharacteristic(UUID.fromString(MyDevice.BATTERY_LEVEL_CHAR_UUID));
            RX_CHAR = boxProjectService.getCharacteristic(UUID.fromString(MyDevice.TRAINING_DATA_CHAR_UUID));
            TX_CHAR = boxProjectService.getCharacteristic(UUID.fromString(MyDevice.TRAINING_SETTING_CHAR_UUID));

            Log.i(TAG, "BoxProject characteristic notification: : " + mBluetoothGatt.setCharacteristicNotification(RX_CHAR,true));
            Log.i(TAG, "Battery characteristic notification: " + mBluetoothGatt.setCharacteristicNotification(BATTERY_LEVEL_CHAR,true));

            descriptors.addAll(RX_CHAR.getDescriptors());
            descriptors.addAll(BATTERY_LEVEL_CHAR.getDescriptors());

            writeDescriptors();
        }
    }
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    private void broadcastUpdate(final String action, final int intValue) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, intValue);
        sendBroadcast(intent);
    }
    private  void broadcastUpdate(final String action, final int[] intArray){
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, intArray);
        sendBroadcast(intent);
    }
}
