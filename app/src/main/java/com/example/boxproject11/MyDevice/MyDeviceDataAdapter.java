package com.example.boxproject11.MyDevice;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boxproject11.R;

import java.util.List;

public class MyDeviceDataAdapter extends RecyclerView.Adapter<MyDeviceDataAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<MyDeviceListItem> devices;
    Context context;

    public MyDeviceDataAdapter(Context context, List<MyDeviceListItem> devices) {
        this.context = context;
        this.devices = devices;
        this.inflater = LayoutInflater.from(context);
    }
    public void changeDeviceList(List<MyDeviceListItem> devices)
    {
        this.devices = devices;
    }
    @Override
    public MyDeviceDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.my_device_list_item, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyDeviceDataAdapter.ViewHolder holder, int position) {
        MyDeviceListItem device = devices.get(position);
        holder.deviceName.setText(device.getName());
        holder.doneImage.setVisibility(View.INVISIBLE);
        holder.progressBar.setVisibility(View.INVISIBLE);
        holder.errorImage.setVisibility(View.INVISIBLE);
        holder.connectionState = device.getConnectionState();
        switch (device.getConnectionState())
        {
            case MyDevice.CONNECTED:
                holder.deviceState.setText(R.string.connected);
                holder.doneImage.setVisibility(View.VISIBLE);
                break;
            case MyDevice.DISCONNECTED:
                holder.deviceState.setText(R.string.disconnected);
                break;
            case MyDevice.CONNECTING:
                holder.deviceState.setText(R.string.connecting);
                holder.progressBar.setVisibility(View.VISIBLE);
                break;
            case MyDevice.SEARCHING:
                holder.deviceState.setText(R.string.searching);
                holder.progressBar.setVisibility(View.VISIBLE);
                break;
            case MyDevice.NONE:
                holder.deviceState.setText(R.string.is_missing);
                holder.errorImage.setVisibility(View.VISIBLE);
                break;
            case MyDevice.NOT_FOUNDED:
                holder.deviceState.setText(R.string.device_not_found_low);
                break;
        }
    }
    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder /*implements View.OnClickListener*//*, View.OnLongClickListener*/{
        private final ImageView doneImage;
        private final ImageView errorImage;
        private final ProgressBar progressBar;
        private final TextView deviceName, deviceState;
        private final CardView cardView;
        private int connectionState;
        public ViewHolder(View view){
            super(view);
            cardView = view.findViewById(R.id.card_view);
            doneImage = view.findViewById(R.id.done_image);
            errorImage = view.findViewById(R.id.error_image);
            progressBar = view.findViewById(R.id.progress_circular);
            deviceName = view.findViewById(R.id.device_name);
            deviceState = view.findViewById(R.id.device_state);

            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Log.i("333222111", "onLongClick: +++");
                    if(deviceItemClickListener != null)
                     deviceItemClickListener.onItemLongClick(getAdapterPosition(), connectionState, view);
                    return true;
                }
            });
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(deviceItemClickListener != null)
                        deviceItemClickListener.onItemClick(getAdapterPosition(), connectionState, view);
                    Log.i("333222111", "onClick: +++");
                }
            });
        }
    }
    private static DeviceItemClickListener deviceItemClickListener;
    public void setOnItemClickListener(DeviceItemClickListener deviceItemClickListener) {
        MyDeviceDataAdapter.deviceItemClickListener = deviceItemClickListener;
    }
    public interface DeviceItemClickListener {
        void onItemClick(int position, int connectionState, View v);
        void onItemLongClick(int position, int connectionState, View v);
    }
}
