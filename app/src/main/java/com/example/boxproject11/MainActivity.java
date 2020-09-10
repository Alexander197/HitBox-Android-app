package com.example.boxproject11;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;


import com.example.boxproject11.BagSetting.Bag;
import com.example.boxproject11.BagSetting.BagSetting;
import com.example.boxproject11.MyTraining.TrainingDataItem;
import com.example.boxproject11.MyTraining.TrainingService;
import com.example.boxproject11.data.TrainingDbHelper;
import com.example.boxproject11.data.TrainingDbItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.boxproject11.BluetoothLeService.ACTION_DATA_AVAILABLE;
import static com.example.boxproject11.BluetoothLeService.ACTION_GATT_CONNECTED;
import static com.example.boxproject11.BluetoothLeService.ACTION_GATT_DISCONNECTED;
import static com.example.boxproject11.BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED;
import static com.example.boxproject11.BluetoothLeService.ACTION_STOP_SCANNING;
import static com.example.boxproject11.BluetoothLeService.BATTERY_LEVEL_UPDATE;
import static com.example.boxproject11.BluetoothLeService.EXTRA_DATA;

public class MainActivity extends AppCompatActivity {
    private String TAG = "trainingServiceTest";

    TrainingService trainingService;
    TrainingDbHelper trainingDbHelper;

    BottomNavigationView navigationView;
    NavController navController;
    ActionBar actionBar;

    MenuItem batteryLevelItem;
    Handler handler = new Handler();

    int homeState = R.id.navigation_home;
    int c = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        trainingDbHelper = new TrainingDbHelper(this);

        final Intent gattServiceIntent = new Intent(MainActivity.this, TrainingService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navigationView = findViewById(R.id.nav_view);
        actionBar = getSupportActionBar();
        setActionBarTitle(getString(R.string.title_training));

        Bitmap original = BitmapFactory.decodeResource(getResources(), R.drawable.background_image);
        Bitmap blurred = BlurBuilder.blur(this, original);
        ImageView image = findViewById(R.id.image);
        image.setBackground(new BitmapDrawable(getResources(), blurred));

        navigation();

        if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }
    }
    public static class BlurBuilder {
        private static final float BITMAP_SCALE = 0.4f;
        private static final float BLUR_RADIUS = 7.0f;

        public static Bitmap blur(Context context, Bitmap image) {
            int width = Math.round(image.getWidth() * BITMAP_SCALE);
            int height = Math.round(image.getHeight() * BITMAP_SCALE);

            Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
            Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

            RenderScript rs = RenderScript.create(context);
            ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
            Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
            theIntrinsic.setRadius(BLUR_RADIUS);
            theIntrinsic.setInput(tmpIn);
            theIntrinsic.forEach(tmpOut);
            tmpOut.copyTo(outputBitmap);

            return outputBitmap;
        }
    }

    public void setActionBarTitle(String title){
        if(actionBar != null)
            actionBar.setTitle(title);
    }
    public void setActionBarSubTitle(String subtitle){
        if(actionBar != null)
            actionBar.setSubtitle(subtitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_main_menu, menu);
        batteryLevelItem = menu.findItem(R.id.battery_level);
        if(trainingService != null) setBatteryState(trainingService.getBatteryLevel());
        return super.onCreateOptionsMenu(menu);
    }
    private void setBatteryState(int batteryLevel){
        if(batteryLevelItem != null) {
            if(batteryLevel == -1) {
                batteryLevelItem.setTitle(R.string.device_not_found);
                batteryLevelItem.setIcon(R.drawable.ic_battery_not_found_30);
            }
            else {
                batteryLevelItem.setTitle(getString(R.string.battery_level, batteryLevel));
                if(batteryLevel >= 0 && batteryLevel<=20)
                    batteryLevelItem.setIcon(R.drawable.ic_battery_20_30);
                else  if(batteryLevel > 20 && batteryLevel <=60)
                    batteryLevelItem.setIcon(R.drawable.ic_battery_40_30);
                else  if(batteryLevel > 60 && batteryLevel <= 80)
                    batteryLevelItem.setIcon(R.drawable.ic_battery_60_30);
                else  if(batteryLevel > 80 && batteryLevel <= 95)
                    batteryLevelItem.setIcon(R.drawable.ic_battery_80_30);
                else  if(batteryLevel > 95 && batteryLevel <= 100)
                    batteryLevelItem.setIcon(R.drawable.ic_battery_full_30);
            }
        }
    }


    private final ServiceConnection serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    trainingService = ((TrainingService.LocalBinder)service).getService();
                    if(trainingService != null){
                        trainingService.initialize();
                        setBatteryState(trainingService.getBatteryLevel());
                        trainingService.setOnStopTrainingListener(new TrainingService.StopTrainingListener() {
                            @Override
                            public void onStopTraining(TrainingDataItem trainingDataItem, String barChart, String duration) {
                                if(trainingDbHelper != null && trainingDataItem.numberOfHits != 0){
                                    StringBuilder builder = new StringBuilder();
                                    builder.append(getString(R.string.training));
                                    builder.append(" ");
                                    builder.append(trainingDbHelper.getLastId() + 1);
                                    final DateFormat dateFormat = SimpleDateFormat.getDateInstance();
                                    String date = dateFormat.format(new Date());
                                    trainingDbHelper.insertTrainingItem(new TrainingDbItem(builder.toString(), date, barChart, trainingDataItem.numberOfHits,
                                            trainingDataItem.averageImpactForce, trainingDataItem.strongestHit, trainingDataItem.numberOfSeries, trainingDataItem.hitsPerSeries, duration));
                                }
                                homeState = R.id.navigation_home;
                                navController.navigate(homeState);
                            }
                        });
                            }
                    Log.i(TAG, "Service connected");
                }
                @Override
                public void onServiceDisconnected(ComponentName name) {
                    trainingService = null;
                }
            };
    private void navigation()
    {
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.bottom_navigation_home:
                        setActionBarTitle(getString(R.string.title_training));
                        setActionBarSubTitle("");
                        navController.navigate(homeState);
                        break;
                    case R.id.bottom_navigation_my_device:
                        if(trainingService != null) {
                            setActionBarTitle(getString(R.string.title_my_device));
                            setActionBarSubTitle("");
                            navController.navigate(R.id.navigation_my_device, trainingService.getDeviceStates());
                        }
                        break;
                    case R.id.bottom_navigation_statistic:
                            setActionBarTitle(getString(R.string.title_statistic));
                        navController.navigate(R.id.navigation_statistic);
                        break;
                }
                return true;
            }
        });
    }
    public void startTraining(){
        if(trainingService != null){
            int connectionState = trainingService.checkDeviceConnection();
            if( connectionState == BluetoothAdapter.STATE_CONNECTED){
                homeState = R.id.navigation_training;
                navController.navigate(homeState);
                Bag bag = new Bag(this);
                BagSetting bagSetting = bag.getBagSettings();
                trainingService.startTraining(bagSetting);
            }
            else {
                View.OnClickListener onSnackBarClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View view = navigationView.findViewById(R.id.bottom_navigation_my_device);
                        view.performClick();
                    }
                };
                Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator), getString(R.string.device_not_connected),Snackbar.LENGTH_LONG);
                snackbar.setAction(getString(R.string.connect), onSnackBarClickListener);
                snackbar.getView().setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.snackbar_shape, null));
//                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)snackbar.getView().getLayoutParams();
//                //params.setMargins(12,12,12,12);
//                snackbar.getView().setLayoutParams(params);
                snackbar.show();
            }
        }
    }
    private static IntentFilter mIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        intentFilter.addAction(ACTION_STOP_SCANNING);
        intentFilter.addAction(EXTRA_DATA);
        intentFilter.addAction(BATTERY_LEVEL_UPDATE);
        return  intentFilter;
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action != null)
                switch(action) {
                /*case ACTION_DEVICE_FOUNDED:
                    Log.i(TAG,"Device founded");
                    break;
                case ACTION_DEVICE_NOT_FOUNDED:
                    Log.i(TAG,"Device not founded");
                    break;*/
                    case BATTERY_LEVEL_UPDATE:
                        setBatteryState(intent.getIntExtra(EXTRA_DATA, -1));
                    default:
                        break;
                }
        }
    };

//    @Override
//    public void onBackPressed() {
//        //super.onBackPressed();
//        Intent startMain = new Intent(Intent.ACTION_MAIN);
//        startMain.addCategory(Intent.CATEGORY_HOME);
//        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(startMain);
//    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver, mIntentFilter());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
       unregisterReceiver(mBroadcastReceiver);
    }
}