package com.boxproject.hitbox.MyDevice;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;

public class MyDevice {

    private Context context;
    private String KEY_DEVICE_PREFERENCES = "DEVICE_PREFERENCES";
    private String KEY_DEVICE_MAC_ADDRESS = "DEVICE_MAC_ADDRESS";
    private SharedPreferences sharedPreferences;

    public static final int CONNECTED = 2;
    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int SEARCHING = 3;
    public static final int NONE = 4;
    public static final int NOT_FOUNDED = 5;


    public BluetoothDevice myDevice;

    public static final String MY_DEVICE_NAME = "UART Service";
    //public static final String BATTERY_SERVICE_UUID = "1c3db28e-cb5d-11ea-87d0-0242ac130003";
    public static final String BATTERY_LEVEL_CHAR_UUID = "C821907A-CB5D-11EA-87D0-0242AC130003";

    public static final String BOXPROJECT_SERVICE_UUID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
    public static final String TRAINING_DATA_CHAR_UUID = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
    public static final String TRAINING_SETTING_CHAR_UUID = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";

    public MyDevice(Context c){
        context = c;
        sharedPreferences = context.getSharedPreferences(KEY_DEVICE_PREFERENCES,Context.MODE_PRIVATE);
    }

    public boolean macAvailable()
    {
        return sharedPreferences.contains(KEY_DEVICE_MAC_ADDRESS);
    }
    public String getDeviceMacAddress(){
        return sharedPreferences.getString(KEY_DEVICE_MAC_ADDRESS,"");
    }
    public boolean setDeviceMacAddress(String macAddress){
        if(!macAvailable()){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_DEVICE_MAC_ADDRESS, macAddress);
            editor.apply();
            return true;
        }
        else return false;
    }
    public boolean removeDeviceMacAddress()
    {
        if(sharedPreferences.contains(KEY_DEVICE_MAC_ADDRESS)){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(KEY_DEVICE_MAC_ADDRESS);
            editor.apply();
            return true;
        }
        return false;
    }
    /*public void addDevice(BluetoothDevice device)
    {
        myDevice = device;
        setDeviceMacAddress(device.getAddress());
    }*/

}
