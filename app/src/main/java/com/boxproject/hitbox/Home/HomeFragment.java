package com.boxproject.hitbox.Home;

import android.graphics.drawable.Drawable;
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

import com.boxproject.hitbox.MainActivity;
import com.boxproject.hitbox.MyTraining.TrainingService;
import com.boxproject.hitbox.R;
import com.boxproject.hitbox.data.TrainingDbHelper;
import com.boxproject.hitbox.data.TrainingDbItem;
import com.boxproject.hitbox.data.TrainingItemFragment;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

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

        if(lastTrainingItem != null) {
            noPreviousTraining.setVisibility(View.INVISIBLE);
            TrainingItemFragment trainingItemFragment = new TrainingItemFragment(lastTrainingItem);
            getChildFragmentManager().beginTransaction().replace(R.id.previous_training_fragment, trainingItemFragment).commit();
        }
        else {
            noPreviousTraining.setVisibility(View.VISIBLE);
            blurView2.setupWith(rootView)
                    .setFrameClearDrawable(windowBackground)
                    .setBlurAlgorithm(new RenderScriptBlur(getActivity()))
                    .setBlurRadius(radius)
                    .setHasFixedTransformationMatrix(true);
        }

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
