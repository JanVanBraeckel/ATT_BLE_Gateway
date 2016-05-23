package com.hogent.jan.attblegateway.bluetoothWrapper;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gebruiker on 13/03/2016.
 */
public class BleService {
    private BluetoothGattService mBleService;
    private List<BleCharacteristic> mBleCharacteristics;

    public BleService(BluetoothGattService bleService){
        mBleService = bleService;
        mBleCharacteristics = new ArrayList<>();
    }

    public BleService(BluetoothGattService bleService, List<BleCharacteristic> bleCharacteristics){
        mBleService = bleService;
        mBleCharacteristics = bleCharacteristics;
    }

    public BluetoothGattService getBleService(){
        return mBleService;
    }

    public List<BleCharacteristic> getBleCharacteristics(){
        return mBleCharacteristics;
    }

    public void setBleService(BluetoothGattService bleService){
        mBleService = bleService;
    }

    public void setBleCharacteristics(List<BleCharacteristic> bleCharacteristics){
        mBleCharacteristics = bleCharacteristics;
    }

    public void addBleCharacteristic(BleCharacteristic bleCharacteristic){
        if(!mBleCharacteristics.contains(bleCharacteristic)){
            mBleCharacteristics.add(bleCharacteristic);
        }
    }

    public void newValueForCharacteristic(BluetoothGattCharacteristic characteristic, String strValue, double doubleValue, byte[] rawValue, String timestamp) {
        for(BleCharacteristic ch : mBleCharacteristics){
            if(ch.getBleCharacteristic()==characteristic){
                ch.setBleCharacteristic(characteristic, strValue, doubleValue, rawValue, timestamp);
            }
        }
    }
}
