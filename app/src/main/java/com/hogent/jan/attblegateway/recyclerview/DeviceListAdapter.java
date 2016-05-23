package com.hogent.jan.attblegateway.recyclerview;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.media.Image;
import android.renderscript.RSIllegalArgumentException;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hogent.jan.attblegateway.MainActivity;
import com.hogent.jan.attblegateway.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by Jan on 25/02/2016.
 */
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.MainViewHolder> {
    private DeviceClickedListener mListener;
    private ArrayList<BluetoothDevice> mDevices;
    private ArrayList<Integer> mRSSIs;
    private Context mContext;

    public class MainViewHolder extends RecyclerView.ViewHolder {
        private final TextView deviceName, deviceAddress, deviceRSSI;
        private final ImageView btLogo;

        public MainViewHolder(View itemView) {
            super(itemView);
            deviceAddress = ButterKnife.findById(itemView, R.id.deviceAddress);
            deviceName = ButterKnife.findById(itemView, R.id.deviceName);
            deviceRSSI = ButterKnife.findById(itemView, R.id.deviceRSSI);
            btLogo = ButterKnife.findById(itemView, R.id.btLogo);
        }
    }

    public DeviceListAdapter(Context context) {
        mContext = context;
        mDevices = new ArrayList<>();
        mRSSIs = new ArrayList<>();
    }

    public void addDevice(BluetoothDevice device, int RSSI) {
        if (!mDevices.contains(device)) {
            mDevices.add(device);
            mRSSIs.add(RSSI);
            notifyDataSetChanged();
        }
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MainViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_device, parent, false));
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        final BluetoothDevice device = mDevices.get(position);
        final int RSSI = mRSSIs.get(position);

        holder.deviceName.setText(device.getName() == null || device.getName().length() <= 0 ? "Unknown Device" : device.getName());
        holder.deviceAddress.setText(device.getAddress());
        holder.deviceRSSI.setText(RSSI == 0 ? "N/A" : RSSI + " db");

        Glide.with(mContext)
                .load(R.drawable.bluetoothlogo)
                .into(holder.btLogo);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.deviceClicked(device, mRSSIs.get(position));
                }
            }
        });
    }

    public void clearList(){
        mDevices.clear();
        mRSSIs.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public BluetoothDevice getDevice(int index) {
        return mDevices.get(index);
    }

    public void setDeviceClickedListener(DeviceClickedListener listener){
        mListener = listener;
    }

    public interface DeviceClickedListener{
        void deviceClicked(BluetoothDevice device, int rssi);
    }
}
