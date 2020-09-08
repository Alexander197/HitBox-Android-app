package com.example.boxproject11.MyDevice;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.boxproject11.R;

public class DeviceDeleteConfirmation extends DialogFragment {
    String TAG = "myDeviceTest";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.delete) + " " + MyDevice.MY_DEVICE_NAME);
        builder.setMessage(getString(R.string.are_you_sure));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(positiveButtonClickListener !=null)
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
    public interface PositiveButtonClickListener {
        void onPositiveButtonClicked();
    }
    public void setOnPositiveButtonClickedListener(PositiveButtonClickListener positiveButtonClickedListener){
        this.positiveButtonClickListener = positiveButtonClickedListener;
    }
}
