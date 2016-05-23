package com.hogent.jan.attblegateway.bluetoothWrapper;

import android.bluetooth.BluetoothGattDescriptor;

import java.util.List;

/**
 * Created by Gebruiker on 13/03/2016.
 */
public class BleDescriptor {
    private BluetoothGattDescriptor mBleGattDescriptor;

    public BleDescriptor(BluetoothGattDescriptor bluetoothGattDescriptor){
        mBleGattDescriptor = bluetoothGattDescriptor;
    }

    public BluetoothGattDescriptor getBleGattDescriptor(){
        return mBleGattDescriptor;
    }

    public void setBleGattDescriptor(BluetoothGattDescriptor bluetoothGattDescriptor){
        mBleGattDescriptor = bluetoothGattDescriptor;
    }
}
