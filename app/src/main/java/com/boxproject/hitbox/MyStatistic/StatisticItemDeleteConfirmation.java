package com.boxproject.hitbox.MyStatistic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.boxproject.hitbox.R;
import com.boxproject.hitbox.data.TrainingDbHelper;

public class StatisticItemDeleteConfirmation extends DialogFragment {
    private int id;
    private String name;
    private TrainingDbHelper trainingDbHelper;

    public StatisticItemDeleteConfirmation(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        trainingDbHelper = new TrainingDbHelper(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.delete) + " " + name);
        builder.setMessage(getString(R.string.are_you_sure));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                trainingDbHelper.deleteTrainingItemById(id);
                if(positiveButtonClickListener != null)
                   positiveButtonClickListener.onPositiveButtonClicked();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        return builder.create();
    }
    private PositiveButtonClickListener positiveButtonClickListener;
    public interface PositiveButtonClickListener{
        void onPositiveButtonClicked();
    }
    public void setOnPositiveButtonClickedListener(PositiveButtonClickListener positiveButtonClickedListener){
        this.positiveButtonClickListener = positiveButtonClickedListener;
    }
}
