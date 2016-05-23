package com.hogent.jan.attblegateway;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hogent.jan.attblegateway.bluetoothWrapper.BleNamesResolver;
import com.hogent.jan.attblegateway.bluetoothWrapper.BleWrapper;
import com.hogent.jan.attblegateway.bluetoothWrapper.BleWrapperUiCallbacks;
import com.hogent.jan.attblegateway.recyclerview.DeviceListAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceListFragment extends Fragment implements DeviceListAdapter.DeviceClickedListener, SwipeRefreshLayout.OnRefreshListener{
    private final String TAG = getClass().getSimpleName();
    private static final long SCANNING_TIMEOUT = 10 * 1000;
    private static final int ENABLE_BT_REQUEST_ID = 1;

    private DeviceListListener mListener;
    private boolean mScanning = false;
    private BleWrapper mBleWrapper = null;
    private Handler mHandler = new Handler();
    private DeviceListAdapter mDeviceListAdapter = null;

    @Bind(R.id.device_list)
    RecyclerView mDeviceList;

    @Bind(R.id.swipeRefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    public static DeviceListFragment newInstance() {
        DeviceListFragment fragment = new DeviceListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_device_list, container, false);

        ButterKnife.bind(this, v);

        mBleWrapper = new BleWrapper(getActivity(), new BleWrapperUiCallbacks.Null() {
            @Override
            public void uiDeviceFound(final BluetoothDevice device, final int rssi, final byte[] record) {
                Log.d(TAG, "uiDeviceFound() called with: " + "device = [" + device.getName() + "], rssi = [" + rssi + "], record = [" + record + "]");
                foundDevice(device, rssi);
            }
        });

        if (!mBleWrapper.checkBleHardwareAvailable()) {
            Toast.makeText(getContext(), "Device doesn't support BLE", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mBleWrapper.isBtEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
        }

        mBleWrapper.initialize();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mDeviceList.setLayoutManager(layoutManager);
        mDeviceListAdapter = new DeviceListAdapter(getContext());
        mDeviceListAdapter.setDeviceClickedListener(this);
        mDeviceList.setAdapter(mDeviceListAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        mScanning = true;
        addScanningTimeout();
        mBleWrapper.startScanning();
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                ((DeviceListAdapter) mDeviceList.getAdapter()).clearList();
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mScanning = false;
        mBleWrapper.stopScanning();
        mBleWrapper.disconnect();
        mBleWrapper.close();
    }

    @Override
    public void onRefresh() {
        if (!mScanning) {
            mBleWrapper.disconnect();
            mDeviceListAdapter.clearList();
            addScanningTimeout();
            mBleWrapper.startScanning();
        }
    }

    private void foundDevice(final BluetoothDevice device, final int rssi){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceListAdapter.addDevice(device, rssi);
            }
        });
    }

    private void addScanningTimeout() {
        Runnable timeout = new Runnable() {
            @Override
            public void run() {
                if(mBleWrapper == null) {
                    return;
                }
                mScanning = false;
                mBleWrapper.stopScanning();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        };
        mHandler.postDelayed(timeout, SCANNING_TIMEOUT);
    }

    @Override
    public void deviceClicked(BluetoothDevice device, int rssi) {
        mListener.deviceClicked(device, rssi);
    }

    public void setDeviceClickedListener(DeviceListListener listener){
        mListener = listener;
    }

    public interface DeviceListListener {
        void deviceClicked(BluetoothDevice device, int rssi);
    }
}
