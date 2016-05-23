package com.hogent.jan.attblegateway;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.hogent.jan.attblegateway.ATTBLE.DeviceUICallbacks;
import com.hogent.jan.attblegateway.ATTBLE.IoTGateway;
import com.hogent.jan.attblegateway.ATTBLE.Model.ActuatorData;
import com.hogent.jan.attblegateway.ATTBLE.Model.AssetManagementCommandData;
import com.hogent.jan.attblegateway.ExpandableListView.ExpandableListAdapter;
import com.hogent.jan.attblegateway.bluetoothWrapper.BleCharacteristic;
import com.hogent.jan.attblegateway.bluetoothWrapper.BleNamesResolver;
import com.hogent.jan.attblegateway.bluetoothWrapper.BleService;
import com.hogent.jan.attblegateway.bluetoothWrapper.BleWrapper;
import com.hogent.jan.attblegateway.bluetoothWrapper.BleWrapperUiCallbacks;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DeviceDetailFragment extends Fragment implements BleWrapperUiCallbacks, ExpandableListAdapter.ExpandableListAdapterListener, DeviceUICallbacks {
    private final String TAG = getClass().getSimpleName();

    private static final String BLE_NAME = "deviceName";
    private static final String BLE_ADDRESS = "deviceAddress";
    private static final String BLE_RSSI = "deviceRssi";
    private IoTGateway iotGateway;

    private BleWrapper mBleWrapper = null;
    private ExpandableListAdapter mListAdapter;

    private String mDeviceName = "";
    private String mDeviceAddress = "";
    private int mDeviceRssi = 0;

    @Bind(R.id.deviceDetailAddress)
    TextView mDeviceAddressView;

    @Bind(R.id.deviceDetailRssi)
    TextView mDeviceRssiView;

    @Bind(R.id.deviceDetailName)
    TextView mDeviceNameView;

    @Bind(R.id.deviceServices)
    ExpandableListView mExpandableListView;

    public static DeviceDetailFragment newInstance(String name, String address, int rssi, IoTGateway iotGateway) {
        DeviceDetailFragment fragment = new DeviceDetailFragment(iotGateway);
        Bundle args = new Bundle();
        args.putString(BLE_NAME, name);
        args.putString(BLE_ADDRESS, address);
        args.putInt(BLE_RSSI, rssi);
        fragment.setArguments(args);

        return fragment;
    }

    public DeviceDetailFragment() {
    }

    public DeviceDetailFragment(IoTGateway iotGateway) {
        this.iotGateway = iotGateway;
    }

    @Override
    public void uiDeviceFound(BluetoothDevice device, int rssi, byte[] record) {
        //
    }

    @Override
    public void uiDeviceConnected(BluetoothGatt gatt, BluetoothDevice device) {
        Log.d(TAG, "uiDeviceConnected() called with: " + "state = [" + mBleWrapper.getAdapter().getState() + "]");
    }

    @Override
    public void uiDeviceDisconnected(BluetoothGatt gatt, BluetoothDevice device) {
        Log.d(TAG, "uiDeviceDisconnected() called with: " + "state = [" + mBleWrapper.getAdapter().getState() + "]");
        mBleWrapper.connect(device.getAddress());
    }

    @Override
    public void uiNewRssiAvailable(BluetoothGatt gatt, BluetoothDevice device, final int rssi) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceRssi = rssi;
                mDeviceRssiView.setText(rssi + " db");
            }
        });
    }

    @Override
    public void uiAvailableServices(BluetoothGatt gatt, BluetoothDevice device, final List<BluetoothGattService> services) {
        final List<BleService> bleServices = new ArrayList<>();
        for (BluetoothGattService s : services) {
            bleServices.add(new BleService(s));
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListAdapter.addServices(bleServices);
            }
        });

        for (BluetoothGattService service : services) {
            final String serviceName = BleNamesResolver.resolveUuid(service.getUuid().toString());
            Log.d(TAG, "uiAvailableServices() called with: " + "Service found = [" + serviceName + "]");

            mBleWrapper.getCharacteristicsForService(service);
        }
    }

    @Override
    public void uiCharacteristicForService(BluetoothGatt gatt, BluetoothDevice device, final BluetoothGattService service, final List<BluetoothGattCharacteristic> chars) {
        final List<BleCharacteristic> characteristics = new ArrayList<>();
        for (BluetoothGattCharacteristic ch : chars) {
            characteristics.add(new BleCharacteristic(ch));
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListAdapter.addCharacteristicsForService(service, characteristics);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (BluetoothGattCharacteristic bluetoothGattCharacteristic : chars) {
                    int properties = bluetoothGattCharacteristic.getProperties();
                    if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
                        iotGateway.addAsset(mDeviceAddress.replace(":", ""),
                                service.getUuid().toString().replace("-", "") + "_" + bluetoothGattCharacteristic.getUuid().toString().replace("-", ""),
                                BleNamesResolver.resolveCharacteristicName(bluetoothGattCharacteristic.getUuid().toString()),
                                "",
                                true,
                                BleNamesResolver.resolveCharacteristicType(bluetoothGattCharacteristic.getUuid().toString()));
                    } else if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) != 0
                            || (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
                            || (properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                        iotGateway.addAsset(mDeviceAddress.replace(":", ""),
                                service.getUuid().toString().replace("-", "") + "_" + bluetoothGattCharacteristic.getUuid().toString().replace("-", ""),
                                BleNamesResolver.resolveCharacteristicName(bluetoothGattCharacteristic.getUuid().toString()),
                                "",
                                false,
                                BleNamesResolver.resolveCharacteristicType(bluetoothGattCharacteristic.getUuid().toString()));
                    }
                    String characteristicName = BleNamesResolver.resolveCharacteristicName(bluetoothGattCharacteristic.getUuid().toString());
                    Log.d(TAG, "uiCharacteristicForService() called with: " + "Characteristic found = [" + characteristicName + "]");
                }
            }
        }).start();
    }

    @Override
    public void uiCharacteristicsDetails(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "uiCharacteristicsDetails() called with: " + "gatt = [" + gatt + "], device = [" + device + "], service = [" + service + "], characteristic = [" + characteristic + "]");
    }

    @Override
    public void uiNewValueForCharacteristic(BluetoothGatt gatt, BluetoothDevice device, final BluetoothGattService service, final BluetoothGattCharacteristic ch, final String strValue, final double doubleValue, final byte[] rawValue, final String timestamp) {
        Log.d(TAG, "uiNewValueForCharacteristic() called with: " + "gatt = [" + gatt + "], device = [" + device + "], service = [" + service.getUuid() + "], ch = [" + ch.getUuid() + "], strValue = [" + strValue + "], intValue = [" + doubleValue + "], rawValue = [" + rawValue + "], timestamp = [" + timestamp + "]");

        new Thread(new Runnable() {
            @Override
            public void run() {
                String type = BleNamesResolver.resolveCharacteristicType(ch.getUuid().toString());

                switch (type) {
                    case "integer":
                        iotGateway.send(mDeviceAddress.replace(":", ""),
                                service.getUuid().toString().replace("-", "") + "_" + ch.getUuid().toString().replace("-", ""),
                                String.valueOf((int)doubleValue));
                        break;
                    case "boolean":
                        if (doubleValue != 0) {
                            iotGateway.send(mDeviceAddress.replace(":", ""),
                                    service.getUuid().toString().replace("-", "") + "_" + ch.getUuid().toString().replace("-", ""),
                                    "true");
                        } else {
                            iotGateway.send(mDeviceAddress.replace(":", ""),
                                    service.getUuid().toString().replace("-", "") + "_" + ch.getUuid().toString().replace("-", ""),
                                    "false");
                        }
                        break;
                    case "double":
                        iotGateway.send(mDeviceAddress.replace(":", ""),
                                service.getUuid().toString().replace("-", "") + "_" + ch.getUuid().toString().replace("-", ":"),
                                String.valueOf(doubleValue));
                        break;
                    default:
                        iotGateway.send(mDeviceAddress.replace(":", ""),
                                service.getUuid().toString().replace("-", "") + "_" + ch.getUuid().toString().replace("-", ""),
                                strValue);
                        break;
                }
            }
        }).start();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListAdapter.newValueForCharacteristic(service, ch, strValue, doubleValue, rawValue, timestamp);
            }
        });
    }

    @Override
    public void uiSuccessfulWrite(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String description) {

    }

    @Override
    public void uiFailedWrite(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String description) {

    }

    @Override
    public void uiGotNotification(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "uiGotNotification() called with: " + "gatt = [" + gatt + "], device = [" + device + "], service = [" + service + "], characteristic = [" + characteristic + "]");
    }

    @Override
    public void requestCharacteristicValue(BluetoothGattService service, BluetoothGattCharacteristic characteristic) {
        mBleWrapper.requestCharacteristicValue(service, characteristic);
}

    @Override
    public void writeDataToCharacteristic(final BluetoothGattService service, final BluetoothGattCharacteristic characteristic, byte[] data, final String originalValue) {
        mBleWrapper.writeDataToCharacteristic(characteristic, data);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String type = BleNamesResolver.resolveCharacteristicType(characteristic.getUuid().toString());

                switch (type) {
                    case "integer":
                        iotGateway.send(mDeviceAddress.replace(":", ""),
                                service.getUuid().toString().replace("-", "") + "_" + characteristic.getUuid().toString().replace("-", ""),
                                String.valueOf(originalValue));
                        break;
                    case "boolean":
                        try {
                            if (Integer.parseInt(originalValue) != 0) {
                                iotGateway.send(mDeviceAddress.replace(":", ""),
                                        service.getUuid().toString().replace("-", "") + "_" + characteristic.getUuid().toString().replace("-", ""),
                                        "true");
                            } else {
                                iotGateway.send(mDeviceAddress.replace(":", ""),
                                        service.getUuid().toString().replace("-", "") + "_" + characteristic.getUuid().toString().replace("-", ""),
                                        "false");
                            }
                        } catch (Exception ignored) {
                        }
                        try {
                            if (Boolean.parseBoolean(originalValue)) {
                                iotGateway.send(mDeviceAddress.replace(":", ""),
                                        service.getUuid().toString().replace("-", "") + "_" + characteristic.getUuid().toString().replace("-", ""),
                                        "true");
                            } else {
                                iotGateway.send(mDeviceAddress.replace(":", ""),
                                        service.getUuid().toString().replace("-", "") + "_" + characteristic.getUuid().toString().replace("-", ""),
                                        "false");
                            }
                        } catch (Exception ignored) {
                        }
                        break;
                    default:
                        iotGateway.send(mDeviceAddress.replace(":", ""),
                                service.getUuid().toString().replace("-", "") + "_" + characteristic.getUuid().toString().replace("-", ""),
                                originalValue);
                        break;
                }
            }
        }).start();
    }

    @Override
    public int getValueFormat(BluetoothGattCharacteristic characteristic) {
        return mBleWrapper.getValueFormat(characteristic);
    }

    @Override
    public void setNotificationForCharacteristic(BluetoothGattService service, BluetoothGattCharacteristic characteristic, boolean enabled) {
        mBleWrapper.setNotificationForCharacteristic(characteristic, enabled);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDeviceName = getArguments().getString(BLE_NAME);
            mDeviceAddress = getArguments().getString(BLE_ADDRESS);
            mDeviceRssi = getArguments().getInt(BLE_RSSI);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    iotGateway.addDevice(mDeviceAddress.replace(":", ""), mDeviceName, "A BLE device", true);
                }
            }).start();
        }

        iotGateway.addObserver(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mBleWrapper == null) {
            mBleWrapper = new BleWrapper(getActivity(), this);
        }

        if (!mBleWrapper.initialize()) {
            getActivity().finish();
        }

        mListAdapter = new ExpandableListAdapter(getContext());
        mListAdapter.setExpandableListAdapterListener(this);
        mExpandableListView.setAdapter(mListAdapter);

        mBleWrapper.connect(mDeviceAddress);
    }

    @Override
    public void onPause() {
        super.onPause();

        mListAdapter.clearLists();

        mBleWrapper.stopMonitoringRssiValue();
        mBleWrapper.disconnect();
        mBleWrapper.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_device_detail, container, false);

        ButterKnife.bind(this, v);

        mDeviceAddressView.setText(mDeviceAddress);
        mDeviceRssiView.setText(mDeviceRssi + " db");
        mDeviceNameView.setText(mDeviceName);

        return v;
    }

    @Override
    public void onActuatorValue(IoTGateway caller, ActuatorData data) {
        System.out.println("main.onActuatorValue");
        System.out.println("caller = [" + caller.getDeviceId() + "], data = [" + data + "], asset = [" + data.getAsset() + "]");
        mListAdapter.onActuatorValue(data.getAsset().split("_")[0], data.getAsset().split("_")[1], data.toString());
    }

    @Override
    public void onAssetManagementCommand(IoTGateway caller, AssetManagementCommandData data) {
        System.out.println("main.onAssetManagementCommand");
        System.out.println("caller = [" + caller.getDeviceId() + "], data = [" + data + "], asset = [" + data.getAsset() + "]");
    }

    @Override
    public void onDeviceManagementCommand(IoTGateway caller, String command) {
        System.out.println("main.onDeviceManagementCommand");
        System.out.println("caller = [" + caller.getDeviceId() + "], command = [" + command + "]");
    }

    @Override
    public void onConnectionReset(IoTGateway caller) {
        System.out.println("main.onConnectionReset");
        System.out.println("caller = [" + caller + "]");
    }

    @Override
    public String getDeviceId() {
        return mDeviceAddress.replace(":", "");
    }
}
