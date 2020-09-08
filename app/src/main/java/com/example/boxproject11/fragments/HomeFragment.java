package com.example.boxproject11.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxproject11.MainActivity;
import com.example.boxproject11.MyTraining.TrainingService;
import com.example.boxproject11.R;
import com.example.boxproject11.data.TrainingDbHelper;
import com.example.boxproject11.data.TrainingDbItem;
import com.example.boxproject11.data.TrainingItemFragment;

public class HomeFragment extends Fragment {

    private  String TAG = "fragmentTest";
    private TrainingService trainingService;
    private TrainingDbHelper trainingDbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.home_fragment, container, false);

        trainingDbHelper = new TrainingDbHelper(getActivity());
        TrainingDbItem lastTrainingItem =  trainingDbHelper.getLastTrainingDbItem();

        TextView noPreviousTraining = root.findViewById(R.id.no_previous_trainings);

        if(lastTrainingItem != null)
        {
            noPreviousTraining.setVisibility(View.INVISIBLE);
            TrainingItemFragment trainingItemFragment = new TrainingItemFragment(lastTrainingItem);
            getChildFragmentManager().beginTransaction().replace(R.id.previous_training_fragment, trainingItemFragment).commit();
        }
        else noPreviousTraining.setVisibility(View.VISIBLE);

        Button startTrainingButton = root.findViewById(R.id.start_button);
        startTrainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).startTraining();
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: home");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: home");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: home");
    }
}
