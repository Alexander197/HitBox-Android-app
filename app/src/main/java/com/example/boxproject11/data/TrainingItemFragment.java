package com.example.boxproject11.data;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxproject11.BagSetting.Bag;
import com.example.boxproject11.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class TrainingItemFragment extends Fragment {
    private final String TAG = "statisticTest";

    private final TrainingDbItem trainingDbItem;

    public TrainingItemFragment(TrainingDbItem trainingDbItem) {
        this.trainingDbItem = trainingDbItem;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.training_item, container, false);

        BarChart barChart = root.findViewById(R.id.bar_chart);

        ArrayList<BarEntry> hits = new ArrayList();
        int counter = 0;
        for(int integer: parseIntArray(trainingDbItem.barChart, "-")) {
            hits.add(new BarEntry(counter, integer));
            counter++;
        }

        BarDataSet barDataSet = new BarDataSet(hits, getString(R.string.impact_force_distribution));
        barDataSet.setHighlightEnabled(false);
        barDataSet.setDrawValues(false);
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        BarData data = new BarData(barDataSet);
        barChart.setData(data);

        barChart.animateY(300, Easing.EaseInExpo);
        barChart.getDescription().setEnabled(false);


        barChart.getAxisLeft().setGranularity(1f);
        barChart.getAxisLeft().setAxisMinValue(0f);
        barChart.getAxisRight().setGranularity(1f);
        barChart.getAxisRight().setAxisMinValue(0f);

        barChart.getXAxis().setAxisMinValue(0f);
        barChart.getXAxis().setAxisMaxValue(parseIntArray(trainingDbItem.barChart, "-").length);
        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int)(Bag.MIN_THRESHOLD + value * Bag.BAR_CHART_STEP));
            }
        });

        TextView title = root.findViewById(R.id.training_title);
        title.setText(trainingDbItem.trainingTitle);
        title.setGravity(Gravity.CENTER);

        TextView dateText = root.findViewById(R.id.training_date);
        dateText.setText(trainingDbItem.trainingDate);
        dateText.setGravity(Gravity.CENTER);

        TextView numberOfHitsView = root.findViewById(R.id.number_of_hits_number);
        numberOfHitsView.setText(String.valueOf(trainingDbItem.numberOfHits));
        numberOfHitsView.setGravity(Gravity.END);

        TextView averageImpactForceView = root.findViewById(R.id.average_impact_force_number);
        averageImpactForceView.setText(getString(R.string.training_item_float_kg_values, trainingDbItem.averageImpactForce));
        averageImpactForceView.setGravity(Gravity.END);

        TextView strongestHitView = root.findViewById(R.id.strongest_hit_number);
        strongestHitView.setText(getString(R.string.training_item_integer_kg_values, trainingDbItem.strongestHit));
        strongestHitView.setGravity(Gravity.END);

        TextView numberOfSeriesView = root.findViewById(R.id.number_of_series_number);
        numberOfSeriesView.setText(String.valueOf(trainingDbItem.numberOfSeries));
        numberOfSeriesView.setGravity(Gravity.END);

        TextView hitsPerSeriesView = root.findViewById(R.id.hits_per_series_number);
        hitsPerSeriesView.setText(getString(R.string.training_item_float_values, trainingDbItem.hitsPerSeries));
        hitsPerSeriesView.setGravity(Gravity.END);

        TextView trainingDurationView = root.findViewById(R.id.training_duration_number);
        trainingDurationView.setText(trainingDbItem.trainingDuration);
        trainingDurationView.setGravity(Gravity.END);


        return  root;
    }
    private int[] parseIntArray(String str, String key){
        String[] string = str.split(key);
        int[] array = new int[string.length];
        for(int i = 0; i < string.length; i++)
            array[i] = Integer.parseInt(string[i]);
        return array;
    }
}
