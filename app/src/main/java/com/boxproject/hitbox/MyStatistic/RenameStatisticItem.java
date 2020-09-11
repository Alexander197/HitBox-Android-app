package com.boxproject.hitbox.MyStatistic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.boxproject.hitbox.R;
import com.boxproject.hitbox.data.TrainingDbHelper;
import com.boxproject.hitbox.data.TrainingsDbContract;

public class RenameStatisticItem extends DialogFragment {

    private final String TAG = "statisticTest";

    private TrainingDbHelper trainingDbHelper;
    private int id;
    private String newTitle;
    private String oldTitle;

    public RenameStatisticItem(int id, String oldTitle) {
        this.id = id;
        this.oldTitle = oldTitle;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        trainingDbHelper = new TrainingDbHelper(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.rename_statistic_item, null);

        builder.setView(view);
        TextView titleText = view.findViewById(R.id.title_text);
        titleText.setText(getString(R.string.rename) + " " + oldTitle);
        titleText.setGravity(Gravity.CENTER);

        final EditText editText = view.findViewById(R.id.edit_text);
        editText.setText(oldTitle);
        builder.setPositiveButton(getString(R.string.apply), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "Apply button clicked");
                newTitle = editText.getText().toString();
                if(!newTitle.equals(""))
                    trainingDbHelper.updateTrainingItemById(id, TrainingsDbContract.COLUMN_TITLE, newTitle);
                if(applyButtonClickedListener != null)
                    applyButtonClickedListener.onApplyButtonClicked();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();
    }
    private ApplyButtonClickListener applyButtonClickedListener;
    public interface ApplyButtonClickListener{
        void onApplyButtonClicked();
    }
    public void setOnApplyButtonClickListener(ApplyButtonClickListener applyButtonClickedListener){
        this.applyButtonClickedListener = applyButtonClickedListener;
    }
}
