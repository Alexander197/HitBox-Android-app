package com.example.boxproject11.MyDevice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boxproject11.BluetoothLeService;
import com.example.boxproject11.MyTraining.TrainingService;
import com.example.boxproject11.R;

import java.util.ArrayList;
import java.util.List;

import static com.example.boxproject11.BluetoothLeService.*;

public class MyDeviceFragment extends Fragment{

    String TAG = "myDeviceTest";

    private DeviceList deviceList;
    private TrainingService trainingService;
    private MyDevice myDevice;

    private Button actionButton;
    private ConnectionInfoFragment fragment = new ConnectionInfoFragment();
    FragmentTransaction transaction;
    Handler handler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        requestPermissions();
        final Intent gattServiceIntent = new Intent(getActivity(), TrainingService.class);
        getActivity().bindService(gattServiceIntent, serviceConnection, getActivity().BIND_AUTO_CREATE);
        super.onCreate(savedInstanceState);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            trainingService = ((TrainingService.LocalBinder) service).getService();
            setDeviceItem();
            int serviceConnectionState = trainingService.initialize();
            Log.i(TAG,"ServiceConnected " + serviceConnectionState);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            trainingService = null;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.my_device_fragment, container, false);
        myDevice = new MyDevice(getActivity());
        actionButton = root.findViewById(R.id.action_button);

        deviceList = new DeviceList(root, getActivity());
        deviceList.beginList(MyDevice.MY_DEVICE_NAME, MyDevice.CONNECTED);
        setDeviceItem(getArguments());

        deviceList.getAdapter().setOnItemClickListener(new MyDeviceDataAdapter.DeviceItemClickListener() {
            @Override
            public void onItemClick(int position, int connectionState, View v) {
                transaction = getFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.animator.info_fragment_enter, R.animator.info_fragment_exit);
                if(!fragment.isVisible()) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("connectionState", connectionState);
                    fragment.setArguments(bundle);
                    transaction.replace(R.id.connection_info_fragment, fragment);
                }
                else{
                    transaction.remove(fragment);
                }
                transaction.commit();
            }

            @Override
            public void onItemLongClick(int position,  int connectionState, View v) {
                DeviceDeleteConfirmation deviceDeleteConfirmation = new DeviceDeleteConfirmation();
                if(getFragmentManager() != null && connectionState != MyDevice.NONE){
                    deviceDeleteConfirmation.show(getFragmentManager(), "deviceDeleteDialog");
                    deviceDeleteConfirmation.setOnPositiveButtonClickedListener(new DeviceDeleteConfirmation.PositiveButtonClickListener() {
                        @Override
                        public void onPositiveButtonClicked() {
                            if(trainingService != null){
                                if(!trainingService.trainingState){
                                    if(trainingService.checkDeviceConnection() == BluetoothAdapter.STATE_CONNECTED){
                                        trainingService.disconnectBleDevice();
                                        myDevice.removeDeviceMacAddress();
                                        Toast.makeText(getActivity(), getString(R.string.device_deleted), Toast.LENGTH_SHORT).show();
                                        setDeviceItem();
                                    }
                                }
                                else Toast.makeText(getActivity(), getString(R.string.finish_your_training_first), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(trainingService != null) {
                    trainingService.startScanning(5000);
                    setDeviceItem();
                    trainingService.setOnScanDeviceListener(new BluetoothLeService.ScanDeviceListener() {
                        @Override
                        public void scanResult(BluetoothDevice device) {
                            if(MyDevice.MY_DEVICE_NAME.equals(device.getName())){
                                if(myDevice.macAvailable()) {
                                    Log.i(TAG, "scanResult: " + device.getAddress() + "\t" + myDevice.getDeviceMacAddress());
                                    if(myDevice.getDeviceMacAddress().equals(device.getAddress())){
                                        trainingService.connectBleDevice(device);
                                    }
                                }
                                else {
                                    myDevice.setDeviceMacAddress(device.getAddress());
                                    trainingService.connectBleDevice(device);
                                }
                            }
                        }
                    });
                }
            }
        });
        return root;
    }

    private static class DeviceList {
        private View view;
        private Context context;
        private List<MyDeviceListItem> devices;
        private MyDeviceDataAdapter adapter;
        private RecyclerView recyclerView;

        private DeviceList(View view, Context context){
            this.view = view;
            this.context = context;
        }
        public void beginList(String deviceName, int state){
            recyclerView = view.findViewById(R.id.recycler_view);
            devices = new ArrayList<>();
            devices.add(new MyDeviceListItem(deviceName,state));
            adapter = new MyDeviceDataAdapter(context, devices);
            recyclerView.setAdapter(adapter);
        }
        public void updateList(String deviceName, int state){
            devices.set(0, new MyDeviceListItem(deviceName, state));
            adapter.changeDeviceList(devices);
            adapter.notifyDataSetChanged();
        }
        public MyDeviceDataAdapter getAdapter (){
            return this.adapter;
        }
    }

    private void setDeviceItem()
    {
        if(trainingService != null && deviceList != null){
            Bundle bundle = trainingService.getDeviceStates();
            if(bundle.getBoolean(TrainingService.SEARCHING_STATE_KEY)) deviceList.updateList(MyDevice.MY_DEVICE_NAME, MyDevice.SEARCHING);
            else {
                if (myDevice.macAvailable()) deviceList.updateList(MyDevice.MY_DEVICE_NAME, bundle.getInt(TrainingService.CONNECTION_STATE_KEY));
                else deviceList.updateList(MyDevice.MY_DEVICE_NAME, MyDevice.NONE);
            }
            if(myDevice.macAvailable()) actionButton.setText(getString(R.string.connect_device));
            else actionButton.setText(getString(R.string.find_device));
        }
    }
    private void setDeviceItem(Bundle bundle)
    {
        if(bundle == null) return;
        if(bundle.getBoolean(TrainingService.SEARCHING_STATE_KEY)) deviceList.updateList(MyDevice.MY_DEVICE_NAME, MyDevice.SEARCHING);
        else {
            if (myDevice.macAvailable()) deviceList.updateList(MyDevice.MY_DEVICE_NAME, bundle.getInt(TrainingService.CONNECTION_STATE_KEY));
            else deviceList.updateList(MyDevice.MY_DEVICE_NAME, MyDevice.NONE);
        }
        if(myDevice.macAvailable()) actionButton.setText(getString(R.string.connect_device));
        else actionButton.setText(getString(R.string.find_device));
    }
    private final int ACTION_LOCATION_SOURCE_SETTINGS_REQUEST_CODE = 2;
    private boolean checkAccessLocation(){
        boolean network_enable = ((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gps_enable = ((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.i(TAG, "checkAccessLocation, network_enable " + network_enable);
        Log.i(TAG, "checkAccessLocation, gps_enable " + gps_enable);
        return network_enable || gps_enable;
    }
    private void requestPermissions() {
        Log.i(TAG, "requestPermissions " + checkAccessLocation());
        if(!checkAccessLocation()){
            RequestAccessLocation requestAccessLocation = new RequestAccessLocation();
            if (getFragmentManager() != null) {
                requestAccessLocation.show(getFragmentManager(), "request_access_location");
                requestAccessLocation.setOnClickListener(new RequestAccessLocation.OnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableLocationIntent, ACTION_LOCATION_SOURCE_SETTINGS_REQUEST_CODE);
                    }

                    @Override
                    public void onNegativeButtonClick() {

                    }
                });
            }
        }
        else{
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent,1);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ACTION_LOCATION_SOURCE_SETTINGS_REQUEST_CODE && ((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent,1);
        }
        //Log.i(TAG, "onActivityResult: " + requestCode + "\t" + resultCode);
    }

    private static IntentFilter mIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_CONNECTING);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        intentFilter.addAction(ACTION_STOP_SCANNING);
        intentFilter.addAction(EXTRA_DATA);
        intentFilter.addAction(ACTION_DEVICE_NOT_FOUNDED);

        return  intentFilter;
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action != null)
            switch(action) {
                case ACTION_STOP_SCANNING:
                    setDeviceItem();
                    Log.i(TAG, "BR Stop scanning");
                    break;
                case ACTION_GATT_CONNECTED:
                    setDeviceItem();
                    Log.i(TAG, "BR Gatt connected ");
                    break;
                case ACTION_GATT_CONNECTING:
                    setDeviceItem();
                    Log.i(TAG, "BR Gatt connecting ");
                    break;
                case ACTION_GATT_DISCONNECTED:
                    setDeviceItem();
                    Log.i(TAG, "BR Gatt disconnected ");
                    break;
                case ACTION_DEVICE_NOT_FOUNDED:
                    if(trainingService != null) {
                        Bundle bundle = trainingService.getDeviceStates();
                        bundle.putInt(CONNECTION_STATE_KEY, MyDevice.NOT_FOUNDED);
                        setDeviceItem(bundle);
                        Log.i(TAG, "BR Gatt not founded ");
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setDeviceItem();
                            }
                        }, 5000);
                    }
                    break;
            }
        }
    };
    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mBroadcastReceiver, mIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

}
