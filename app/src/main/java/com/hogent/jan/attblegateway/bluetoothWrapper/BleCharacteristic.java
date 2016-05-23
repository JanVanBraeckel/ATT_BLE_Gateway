package com.hogent.jan.attblegateway.bluetoothWrapper;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gebruiker on 13/03/2016.
 */
public class BleCharacteristic {
    private BluetoothGattCharacteristic mBleCharacteristic;
    private List<BleDescriptor> mBleDescriptors;
    private String mStrValue = "N/A";
    private double mDoubleValue = 0;
    private String mAsciiValue = "N/A";
    private String mTimetamp = "N/A";

    public BleCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        mBleCharacteristic = bluetoothGattCharacteristic;
        mBleDescriptors = new ArrayList<>();
    }

    public BleCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic, List<BleDescriptor> bleDescriptors) {
        mBleCharacteristic = bluetoothGattCharacteristic;
        mBleDescriptors = bleDescriptors;
    }

    public BluetoothGattCharacteristic getBleCharacteristic() {
        return mBleCharacteristic;
    }

    public List<BleDescriptor> getBleDescriptors() {
        return mBleDescriptors;
    }

    public double getDoubleValue() {
        return mDoubleValue;
    }

    public String getAsciiValue() {
        return mAsciiValue;
    }

    public String getStrValue() {
        return mStrValue;
    }

    public String getTimestamp() {
        return mTimetamp;
    }

    public void setBleCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic, String strValue, double doubleValue, byte[] rawValue, String timestamp) {
        mBleCharacteristic = bluetoothGattCharacteristic;
        String asciiValue = "";
        if (rawValue != null && rawValue.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(rawValue.length);
            for (byte byteChar : rawValue) {
                stringBuilder.append(String.format("%02X", byteChar));
            }
            asciiValue = "0x" + stringBuilder.toString();
        }

        mStrValue = strValue;
        mDoubleValue = doubleValue;
        mAsciiValue = asciiValue;
        mTimetamp = timestamp;
    }

    public void setBleDescriptors(List<BleDescriptor> bleDescriptors) {
        mBleDescriptors = bleDescriptors;
    }

    public void addBleDescriptor(BleDescriptor bleDescriptor) {
        if (!mBleDescriptors.contains(bleDescriptor)) {
            mBleDescriptors.add(bleDescriptor);
        }
    }
}
