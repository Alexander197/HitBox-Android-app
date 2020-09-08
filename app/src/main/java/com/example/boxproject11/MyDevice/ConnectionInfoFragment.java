package com.example.boxproject11.MyDevice;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.boxproject11.MyTraining.TrainingService;
import com.example.boxproject11.R;

import java.util.Timer;
import java.util.TimerTask;

import static com.example.boxproject11.BluetoothLeService.ACTION_GATT_CONNECTED;
import static com.example.boxproject11.BluetoothLeService.ACTION_GATT_DISCONNECTED;
import static com.example.boxproject11.BluetoothLeService.EXTRA_DATA;
import static com.example.boxproject11.BluetoothLeService.RSSI_READ;

public class ConnectionInfoFragment extends Fragment {
    
    String TAG = "infoFragmentTest";
    
    private TrainingService trainingService;
    private Handler handler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        final Intent gattServiceIntent = new Intent(getActivity(), TrainingService.class);
        getActivity().bindService(gattServiceIntent, serviceConnection, getActivity().BIND_AUTO_CREATE);
        super.onCreate(savedInstanceState);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            trainingService = ((TrainingService.LocalBinder) service).getService();
            int serviceConnectionState = trainingService.initialize();
            startTimer();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            trainingService = null;
        }
    };

    private Timer timer;
    private TimerTask timerTask;

    private void startTimer(){
        if(timer != null)
            timer.cancel();
        timer = new Timer();
        timerTask = new MyTimerTask();
        timer.schedule(timerTask, 400, 5000);
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
            if(trainingService != null)
                trainingService.readRSSI();
        }
    }


    private void setInfoPanel(int rssi, boolean connectionState){
        if(connectionState){
            if(rssiLevelText != null){
                rssiLevelText.setText(getString(R.string.signal_level_dbm_values, rssi));
            }
            if(rssiLevelIcon != null){
                if(rssi >= -75) rssiLevelIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_signal_24_4, null));
                else if(rssi < -75 && rssi > -85) rssiLevelIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_signal_24_3, null));
                else if(rssi < -85 && rssi > -95) rssiLevelIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_signal_24_2, null));
                else if(rssi < -95) rssiLevelIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_signal_24_1, null));
            }
        }
        else {
            if(rssiLevelText != null)
                rssiLevelText.setText("");
            if(rssiLevelIcon != null)
                rssiLevelIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_signal_24_0, null));
        }
    }

    private TextView rssiLevelText;
    private ImageView rssiLevelIcon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.connection_info_fragment, container,false);

        rssiLevelText = root.findViewById(R.id.rssi_level_text);
        rssiLevelIcon = root.findViewById(R.id.rssi_level_icon);
        int connectionState = this.getArguments().getInt("connectionState");
        Log.i(TAG, "onCreateView: " + connectionState);
        if(connectionState == BluetoothAdapter.STATE_DISCONNECTED || connectionState == MyDevice.NONE)
            setInfoPanel(0, false);

        return root;
    }

    private static IntentFilter mIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RSSI_READ);
        intentFilter.addAction(EXTRA_DATA);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_GATT_CONNECTED);

        return  intentFilter;
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action != null)
                switch(action){
                    case RSSI_READ:
                        setInfoPanel(intent.getIntExtra(EXTRA_DATA, 0), true);
                        break;
                    case ACTION_GATT_DISCONNECTED:
                        setInfoPanel(0, false);
                        stopTimer();
                        break;
                    case ACTION_GATT_CONNECTED:
                        startTimer();
                        break;
                }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mBroadcastReceiver, mIntentFilter());
        startTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mBroadcastReceiver);
        Log.i(TAG, "onPause: ");
        stopTimer();
    }
}
