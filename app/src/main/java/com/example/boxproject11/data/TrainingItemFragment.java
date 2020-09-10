package com.example.boxproject11.data;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.boxproject11.BagSetting.Bag;
import com.example.boxproject11.MyStatistic.RoundedBarChartRenderer;
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

    private int dbLength;

    private final TrainingDbItem trainingDbItem;

    public TrainingItemFragment(TrainingDbItem trainingDbItem){
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
        barDataSet.setGradientColor(ContextCompat.getColor(getActivity(), R.color.barChartBottom), ContextCompat.getColor(getActivity(), R.color.barChartTop));

        BarData data = new BarData(barDataSet);
        barChart.setData(data);

        barChart.animateY(300, Easing.EaseInExpo);
        barChart.getDescription().setEnabled(false);

        RoundedBarChartRenderer renderer = new RoundedBarChartRenderer(barChart, barChart.getAnimator(), barChart.getViewPortHandler());
        renderer.setRadius(5);
        barChart.setRenderer(renderer);

        barChart.getAxisLeft().setGranularity(1f);
        barChart.getAxisLeft().setAxisMinValue(0f);
        barChart.getAxisRight().setGranularity(1f);
        barChart.getAxisRight().setAxisMinValue(0f);

        barChart.getXAxis().setDrawGridLines(false);
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

        TrainingDbHelper trainingDbHelper = new TrainingDbHelper(getActivity());
        float[] average = trainingDbHelper.getColumnAverage(new String[]{TrainingsDbContract.COLUMN_NUMBER_OF_HITS, TrainingsDbContract.COLUMN_AVERAGE_IMPACT_FORCE, TrainingsDbContract.COLUMN_STRONGEST_HIT,
                TrainingsDbContract.COLUMN_NUMBER_OF_SERIES, TrainingsDbContract.COLUMN_HITS_PER_SERIES});
        dbLength = trainingDbHelper.dbLength();

        TextView numberOfHitsPercent = root.findViewById(R.id.number_of_hits_percent);
        TextView averageImpactForcePercent = root.findViewById(R.id.average_impact_force_percent);
        TextView strongestHitPercent = root.findViewById(R.id.strongest_hit_percent);
        TextView numberOfSeriesPercent = root.findViewById(R.id.number_of_series_percent);
        TextView hitsPerSeriesPercent = root.findViewById(R.id.hits_per_series_percent);

        setPercent(numberOfHitsPercent, average[0], trainingDbItem.numberOfHits);
        setPercent(averageImpactForcePercent, average[1], trainingDbItem.averageImpactForce);
        setPercent(strongestHitPercent, average[2], trainingDbItem.strongestHit);
        setPercent(numberOfSeriesPercent, average[3], trainingDbItem.numberOfSeries);
        setPercent(hitsPerSeriesPercent, average[4], trainingDbItem.hitsPerSeries);

        return  root;
    }

    private void setPercent(TextView view, float average, float value){
        if(dbLength > 1){
            float trueAverage = (average * dbLength - value) / (dbLength - 1);
            float percent = (value - trueAverage)/trueAverage * 100;
            if(percent > 0){
                view.setText(getString(R.string.highPercent, percent));
                view.setTextColor(ContextCompat.getColor(getActivity(), R.color.high_percent_text));
            }
            else if (percent < 0){
                view.setText(getString(R.string.lowPercent, Math.abs(percent)));
                view.setTextColor(ContextCompat.getColor(getActivity(), R.color.low_percent_text));
            }
            else if (percent == 0)
                view.setText(getString(R.string.percent, percent));
        }
    }
    private int[] parseIntArray(String str, String key){
        String[] string = str.split(key);
        int[] array = new int[string.length];
        for(int i = 0; i < string.length; i++)
            array[i] = Integer.parseInt(string[i]);
        return array;
    }
}
