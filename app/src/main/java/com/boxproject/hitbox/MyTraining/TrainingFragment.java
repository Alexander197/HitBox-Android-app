package com.boxproject.hitbox.MyTraining;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.boxproject.hitbox.R;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

import static com.boxproject.hitbox.BluetoothLeService.EXTRA_DATA;
import static com.boxproject.hitbox.MyTraining.TrainingService.TIMER_TICK;

public class TrainingFragment extends Fragment {
    private  String TAG = "trainingServiceTest";
    private TrainingService trainingService;

    private TextView averageImpactForceNumber;
    private TextView numberOfSeriesNumber;
    private TextView hitsPerSeriesNumber;
    private TextView currentImpactForceNumber;
    private TextView numberOfHitsNumber;
    private TextView strongestHitNumber;
    private TextView timer;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent gattServiceIntent = new Intent(getActivity(), TrainingService.class);
        getActivity().bindService(gattServiceIntent, serviceConnection, getActivity().BIND_AUTO_CREATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.training_fragment, container, false);
        averageImpactForceNumber = root.findViewById(R.id.average_impact_force_number);
        numberOfSeriesNumber = root.findViewById(R.id.number_of_series_number);
        hitsPerSeriesNumber = root.findViewById(R.id.hits_per_series_number);
        currentImpactForceNumber = root.findViewById(R.id.current_impact_force);
        numberOfHitsNumber = root.findViewById(R.id.number_of_hits_number);
        strongestHitNumber = root.findViewById(R.id.strongest_hit_number);
        timer = root.findViewById(R.id.timer);

        float radius = 20f;
        View decorView = getActivity().getWindow().getDecorView();
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();
        BlurView blurView1 = root.findViewById(R.id.blur_no_statistic_text);
        BlurView blurView2 = root.findViewById(R.id.blur_recycler_view);

        blurView1.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(getActivity()))
                .setBlurRadius(radius)
                .setHasFixedTransformationMatrix(true);
        blurView2.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(getActivity()))
                .setBlurRadius(radius)
                .setHasFixedTransformationMatrix(true);

        Button stopButton = root.findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(trainingService != null)
                    trainingService.stopTrainingForResult();
            }
        });
        return root;
    }

    private void setTrainingDataItem(TrainingDataItem trainingDataItem){
        averageImpactForceNumber.setText(getString(R.string.training_item_float_kg_values, trainingDataItem.averageImpactForce));
        numberOfSeriesNumber.setText(String.valueOf(trainingDataItem.numberOfSeries));
        hitsPerSeriesNumber.setText(getString(R.string.training_item_float_values, trainingDataItem.hitsPerSeries));
        currentImpactForceNumber.setText(getString(R.string.training_item_integer_kg_values, trainingDataItem.currentImpactForce));
        numberOfHitsNumber.setText(String.valueOf(trainingDataItem.numberOfHits));
        strongestHitNumber.setText(getString(R.string.training_item_integer_kg_values, trainingDataItem.strongestHit));
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            trainingService = ((TrainingService.LocalBinder) service).getService();
            Log.i(TAG, "onServiceConnected: ");
            if(trainingService != null) {
                setTrainingDataItem(trainingService.getTrainingDataItem());
                trainingService.setOnTrainingDataListener(new TrainingService.TrainingDataListener() {
                    @Override
                    public void onTrainingDataReceived(TrainingDataItem trainingDataItem) {
                        Log.i(TAG, "onTrainingDataReceived: ");
                        setTrainingDataItem(trainingDataItem);
                    }
                });
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            trainingService = null;
        }
    };
    private static IntentFilter mIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TIMER_TICK);
        return  intentFilter;
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action == TIMER_TICK)
                timer.setText(intent.getStringExtra(EXTRA_DATA));
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mBroadcastReceiver, mIntentFilter());
        Log.i(TAG, "onResume: training");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mBroadcastReceiver);
        Log.i(TAG, "onDestroyView: training");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: training");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: training");
    }
}
