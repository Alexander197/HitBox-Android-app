package com.boxproject.hitbox.BagSetting;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.boxproject.hitbox.R;

public class BagSettingFragment extends Fragment{

    private TextView bagWeightNumber;
    private TextView thresholdNumber;
    private SeekBar weightBar;
    private SeekBar thresholdBar;
    private Button saveSettingsButton;
    private Bag bag;
    private BagSetting bs;
    private int bagWeight;
    private int threshold;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.bag_settings_fragment, container, false);
        bag = new Bag(getActivity());
        bs = bag.getBagSettings();

        bagWeight = bs.bagWeight;
        threshold = bs.threshold;

        bagWeightNumber = root.findViewById(R.id.bag_weight_number);
        thresholdNumber = root.findViewById(R.id.threshold_number);
        weightBar = root.findViewById(R.id.bag_weight_seekbar);
        thresholdBar = root.findViewById(R.id.threshold_seekbar);
        saveSettingsButton = root.findViewById(R.id.save_settings_button);
        setBagSettingView();

        return root;
    }
    private boolean setBagSettingView()
    {
        if(bs.error)return false;
        bagWeightNumber.setText(getString(R.string.bag_setting_kg_values, bagWeight));
        bagWeightNumber.setGravity(Gravity.END);

        thresholdNumber.setText(getString(R.string.bag_setting_kg_values, threshold));
        thresholdNumber.setGravity(Gravity.END);

        weightBar.setMax(Bag.MAX_BAG_WEIGHT - Bag.MIN_BAG_WEIGHT);
        weightBar.setProgress(bagWeight - Bag.MIN_BAG_WEIGHT);

        thresholdBar.setMax(Bag.MAX_THRESHOLD - Bag.MIN_THRESHOLD);
        thresholdBar.setProgress(threshold- Bag.MIN_THRESHOLD);
        checkSettingsEqual(bag.getBagSettings());

        weightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bagWeight = (seekBar.getProgress() + Bag.MIN_BAG_WEIGHT);
                bagWeightNumber.setText(getString(R.string.bag_setting_kg_values, bagWeight));
                bagWeightNumber.setGravity(Gravity.END);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                checkSettingsEqual(bag.getBagSettings());
            }
        });
        thresholdBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold = (seekBar.getProgress() + Bag.MIN_THRESHOLD)/5;
                threshold *=5;
                thresholdNumber.setText(getString(R.string.bag_setting_kg_values, threshold));
                thresholdNumber.setGravity(Gravity.END);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                checkSettingsEqual(bag.getBagSettings());
            }
        });
        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bag.setBagSettings(bagWeight,threshold);
                checkSettingsEqual(bag.getBagSettings());
            }
        });
        return true;
    }
    private void checkSettingsEqual(BagSetting bs){
        if(bagWeight == bs.bagWeight && threshold == bs.threshold)
            saveSettingsButton.setEnabled(false);
        else saveSettingsButton.setEnabled(true);
    }
}
