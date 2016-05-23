package com.hogent.jan.attblegateway.bluetoothWrapper;

import android.util.SparseArray;

import java.util.HashMap;

/**
 * Created by Jan on 25/02/2016.
 */
public class BleNamesResolver {
    private static HashMap<String, String> mServices = new HashMap<String, String>();
    private static HashMap<String, String> mCharacteristics = new HashMap<String, String>();
    private static HashMap<String, String> mCharacteristicTypes = new HashMap<>();
    private static SparseArray<String> mValueFormats = new SparseArray<String>();
    private static SparseArray<String> mAppearance = new SparseArray<String>();
    private static SparseArray<String> mHeartRateSensorLocation = new SparseArray<String>();

    static public String resolveServiceName(final String uuid) {
        String result = mServices.get(uuid);
        if (result == null) {
            result = "Unknown Service";
        }
        return result;
    }

    static public String resolveValueTypeDescription(final int format) {
        Integer tmp = Integer.valueOf(format);
        return mValueFormats.get(tmp, "Unknown Format");
    }


    static public String resolveCharacteristicType(final String uuid) {
        String result = mCharacteristicTypes.get(uuid);
        return result == null ? "string" : result;
    }

    static public String resolveCharacteristicName(final String uuid) {
        String result = mCharacteristics.get(uuid);
        if (result == null){
            result = "Unknown Characteristic";
        }
        return result;
    }

    static public String resolveUuid(final String uuid) {
        String result = mServices.get(uuid);
        if (result != null){
            return "Service: " + result;
        }

        result = mCharacteristics.get(uuid);
        if (result != null){
            return "Characteristic: " + result;
        }

        result = "Unknown UUID: " + uuid;
        return result;
    }

    static public String resolveAppearance(int key) {
        Integer tmp = Integer.valueOf(key);
        return mAppearance.get(tmp, "Unknown Appearance");
    }

    static public String resolveHeartRateSensorLocation(int key) {
        Integer tmp = Integer.valueOf(key);
        return mHeartRateSensorLocation.get(tmp, "Other");
    }

    static public boolean isService(final String uuid) {
        return mServices.containsKey(uuid);
    }

    static public boolean isCharacteristic(final String uuid) {
        return mCharacteristics.containsKey(uuid);
    }

    static {
        mServices.put("00001811-0000-1000-8000-00805f9b34fb", "Alert Notification Service");
        mServices.put("0000180f-0000-1000-8000-00805f9b34fb", "Battery Service");
        mServices.put("00001810-0000-1000-8000-00805f9b34fb", "Blood Pressure");
        mServices.put("00001805-0000-1000-8000-00805f9b34fb", "Current Time Service");
        mServices.put("00001818-0000-1000-8000-00805f9b34fb", "Cycling Power");
        mServices.put("00001816-0000-1000-8000-00805f9b34fb", "Cycling Speed and Cadence");
        mServices.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information");
        mServices.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access");
        mServices.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute");
        mServices.put("00001808-0000-1000-8000-00805f9b34fb", "Glucose");
        mServices.put("00001809-0000-1000-8000-00805f9b34fb", "Health Thermometer");
        mServices.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate");
        mServices.put("00001812-0000-1000-8000-00805f9b34fb", "Human Interface Device");
        mServices.put("00001802-0000-1000-8000-00805f9b34fb", "Immediate Alert");
        mServices.put("00001803-0000-1000-8000-00805f9b34fb", "Link Loss");
        mServices.put("00001819-0000-1000-8000-00805f9b34fb", "Location and Navigation");
        mServices.put("00001807-0000-1000-8000-00805f9b34fb", "Next DST Change Service");
        mServices.put("0000180e-0000-1000-8000-00805f9b34fb", "Phone Alert Status Service");
        mServices.put("00001806-0000-1000-8000-00805f9b34fb", "Reference Time Update Service");
        mServices.put("00001814-0000-1000-8000-00805f9b34fb", "Running Speed and Cadence");
        mServices.put("00001813-0000-1000-8000-00805f9b34fb", "Scan Parameters");
        mServices.put("00001804-0000-1000-8000-00805f9b34fb", "Tx Power");
        /*Custom services*/
        mServices.put("19b10000-e8f2-537e-4f6c-d104768a1214", "Generic On/Off Service");
        mServices.put("39e1fa00-84a8-11e2-afba-0002a5d5c51b", "Live service");
        mServices.put("39e1fb00-84a8-11e2-afba-0002a5d5c51b", "Upload service");
        mServices.put("39e1fc00-84a8-11e2-afba-0002a5d5c51b", "History service");
        mServices.put("39e1fd00-84a8-11e2-afba-0002a5d5c51b", "FlowerPower clock service");
        mServices.put("39e1fe00-84a8-11e2-afba-0002a5d5c51b", "FlowerPower calibration service");
        mServices.put("f000ffc0-0451-4000-b000-000000000000", "Over The Air Download Service");

        mCharacteristics.put("00002a43-0000-1000-8000-00805f9b34fb", "Alert Category ID");
        mCharacteristics.put("00002a42-0000-1000-8000-00805f9b34fb", "Alert Category ID Bit Mask");
        mCharacteristics.put("00002a06-0000-1000-8000-00805f9b34fb", "Alert Level");
        mCharacteristics.put("00002a44-0000-1000-8000-00805f9b34fb", "Alert Notification Control Point");
        mCharacteristics.put("00002a3f-0000-1000-8000-00805f9b34fb", "Alert Status");
        mCharacteristics.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
        mCharacteristics.put("00002a19-0000-1000-8000-00805f9b34fb", "Battery Level");
        mCharacteristics.put("00002a49-0000-1000-8000-00805f9b34fb", "Blood Pressure Feature");
        mCharacteristics.put("00002a35-0000-1000-8000-00805f9b34fb", "Blood Pressure Measurement");
        mCharacteristics.put("00002a38-0000-1000-8000-00805f9b34fb", "Body Sensor Location");
        mCharacteristics.put("00002a22-0000-1000-8000-00805f9b34fb", "Boot Keyboard Input Report");
        mCharacteristics.put("00002a32-0000-1000-8000-00805f9b34fb", "Boot Keyboard Output Report");
        mCharacteristics.put("00002a33-0000-1000-8000-00805f9b34fb", "Boot Mouse Input Report");
        mCharacteristics.put("00002a5c-0000-1000-8000-00805f9b34fb", "CSC Feature");
        mCharacteristics.put("00002a5b-0000-1000-8000-00805f9b34fb", "CSC Measurement");
        mCharacteristics.put("00002a2b-0000-1000-8000-00805f9b34fb", "Current Time");
        mCharacteristics.put("00002a66-0000-1000-8000-00805f9b34fb", "Cycling Power Control Point");
        mCharacteristics.put("00002a65-0000-1000-8000-00805f9b34fb", "Cycling Power Feature");
        mCharacteristics.put("00002a63-0000-1000-8000-00805f9b34fb", "Cycling Power Measurement");
        mCharacteristics.put("00002a64-0000-1000-8000-00805f9b34fb", "Cycling Power Vector");
        mCharacteristics.put("00002a08-0000-1000-8000-00805f9b34fb", "Date Time");
        mCharacteristics.put("00002a0a-0000-1000-8000-00805f9b34fb", "Day Date Time");
        mCharacteristics.put("00002a09-0000-1000-8000-00805f9b34fb", "Day of Week");
        mCharacteristics.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
        mCharacteristics.put("00002a0d-0000-1000-8000-00805f9b34fb", "DST Offset");
        mCharacteristics.put("00002a0c-0000-1000-8000-00805f9b34fb", "Exact Time 256");
        mCharacteristics.put("00002a26-0000-1000-8000-00805f9b34fb", "Firmware Revision String");
        mCharacteristics.put("00002a51-0000-1000-8000-00805f9b34fb", "Glucose Feature");
        mCharacteristics.put("00002a18-0000-1000-8000-00805f9b34fb", "Glucose Measurement");
        mCharacteristics.put("00002a34-0000-1000-8000-00805f9b34fb", "Glucose Measurement Context");
        mCharacteristics.put("00002a27-0000-1000-8000-00805f9b34fb", "Hardware Revision String");
        mCharacteristics.put("00002a39-0000-1000-8000-00805f9b34fb", "Heart Rate Control Point");
        mCharacteristics.put("00002a37-0000-1000-8000-00805f9b34fb", "Heart Rate Measurement");
        mCharacteristics.put("00002a4c-0000-1000-8000-00805f9b34fb", "HID Control Point");
        mCharacteristics.put("00002a4a-0000-1000-8000-00805f9b34fb", "HID Information");
        mCharacteristics.put("00002a2a-0000-1000-8000-00805f9b34fb", "IEEE 11073-20601 Regulatory Certification Data List");
        mCharacteristics.put("00002a36-0000-1000-8000-00805f9b34fb", "Intermediate Cuff Pressure");
        mCharacteristics.put("00002a1e-0000-1000-8000-00805f9b34fb", "Intermediate Temperature");
        mCharacteristics.put("00002a6b-0000-1000-8000-00805f9b34fb", "LN Control Point");
        mCharacteristics.put("00002a6a-0000-1000-8000-00805f9b34fb", "LN Feature");
        mCharacteristics.put("00002a0f-0000-1000-8000-00805f9b34fb", "Local Time Information");
        mCharacteristics.put("00002a67-0000-1000-8000-00805f9b34fb", "Location and Speed");
        mCharacteristics.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        mCharacteristics.put("00002a21-0000-1000-8000-00805f9b34fb", "Measurement Interval");
        mCharacteristics.put("00002a24-0000-1000-8000-00805f9b34fb", "Model Number String");
        mCharacteristics.put("00002a68-0000-1000-8000-00805f9b34fb", "Navigation");
        mCharacteristics.put("00002a46-0000-1000-8000-00805f9b34fb", "New Alert");
        mCharacteristics.put("00002a04-0000-1000-8000-00805f9b34fb", "Peripheral Preferred Connection Parameters");
        mCharacteristics.put("00002a02-0000-1000-8000-00805f9b34fb", "Peripheral Privacy Flag");
        mCharacteristics.put("00002a50-0000-1000-8000-00805f9b34fb", "PnP ID");
        mCharacteristics.put("00002a69-0000-1000-8000-00805f9b34fb", "Position Quality");
        mCharacteristics.put("00002a4e-0000-1000-8000-00805f9b34fb", "Protocol Mode");
        mCharacteristics.put("00002a03-0000-1000-8000-00805f9b34fb", "Reconnection Address");
        mCharacteristics.put("00002a52-0000-1000-8000-00805f9b34fb", "Record Access Control Point");
        mCharacteristics.put("00002a14-0000-1000-8000-00805f9b34fb", "Reference Time Information");
        mCharacteristics.put("00002a4d-0000-1000-8000-00805f9b34fb", "Report");
        mCharacteristics.put("00002a4b-0000-1000-8000-00805f9b34fb", "Report Map");
        mCharacteristics.put("00002a40-0000-1000-8000-00805f9b34fb", "Ringer Control Point");
        mCharacteristics.put("00002a41-0000-1000-8000-00805f9b34fb", "Ringer Setting");
        mCharacteristics.put("00002a54-0000-1000-8000-00805f9b34fb", "RSC Feature");
        mCharacteristics.put("00002a53-0000-1000-8000-00805f9b34fb", "RSC Measurement");
        mCharacteristics.put("00002a55-0000-1000-8000-00805f9b34fb", "SC Control Point");
        mCharacteristics.put("00002a4f-0000-1000-8000-00805f9b34fb", "Scan Interval Window");
        mCharacteristics.put("00002a31-0000-1000-8000-00805f9b34fb", "Scan Refresh");
        mCharacteristics.put("00002a5d-0000-1000-8000-00805f9b34fb", "Sensor Location");
        mCharacteristics.put("00002a25-0000-1000-8000-00805f9b34fb", "Serial Number String");
        mCharacteristics.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed");
        mCharacteristics.put("00002a28-0000-1000-8000-00805f9b34fb", "Software Revision String");
        mCharacteristics.put("00002a47-0000-1000-8000-00805f9b34fb", "Supported New Alert Category");
        mCharacteristics.put("00002a48-0000-1000-8000-00805f9b34fb", "Supported Unread Alert Category");
        mCharacteristics.put("00002a23-0000-1000-8000-00805f9b34fb", "System ID");
        mCharacteristics.put("00002a1c-0000-1000-8000-00805f9b34fb", "Temperature Measurement");
        mCharacteristics.put("00002a1d-0000-1000-8000-00805f9b34fb", "Temperature Type");
        mCharacteristics.put("00002a12-0000-1000-8000-00805f9b34fb", "Time Accuracy");
        mCharacteristics.put("00002a13-0000-1000-8000-00805f9b34fb", "Time Source");
        mCharacteristics.put("00002a16-0000-1000-8000-00805f9b34fb", "Time Update Control Point");
        mCharacteristics.put("00002a17-0000-1000-8000-00805f9b34fb", "Time Update State");
        mCharacteristics.put("00002a11-0000-1000-8000-00805f9b34fb", "Time with DST");
        mCharacteristics.put("00002a0e-0000-1000-8000-00805f9b34fb", "Time Zone");
        mCharacteristics.put("00002a07-0000-1000-8000-00805f9b34fb", "Tx Power Level");
        mCharacteristics.put("00002a45-0000-1000-8000-00805f9b34fb", "Unread Alert Status");
        /*Custom Characteristics*/
        mCharacteristics.put("19b10001-e8f2-537e-4f6c-d104768a1214", "Generic On/Off Switch");
        mCharacteristics.put("39e1fa01-84a8-11e2-afba-0002a5d5c51b", "Light sensor Value");
        mCharacteristics.put("39e1fa02-84a8-11e2-afba-0002a5d5c51b", "Soil EC");
        mCharacteristics.put("39e1fa03-84a8-11e2-afba-0002a5d5c51b", "Soil Temperature");
        mCharacteristics.put("39e1fa04-84a8-11e2-afba-0002a5d5c51b", "Air Temperature");
        mCharacteristics.put("39e1fa05-84a8-11e2-afba-0002a5d5c51b", "Soil % VWC");
        mCharacteristics.put("39e1fa06-84a8-11e2-afba-0002a5d5c51b", "Live measure Period");
        mCharacteristics.put("39e1fa07-84a8-11e2-afba-0002a5d5c51b", "Led state");
        mCharacteristics.put("39e1fa08-84a8-11e2-afba-0002a5d5c51b", "Last move date");
        mCharacteristics.put("39e1fa09-84a8-11e2-afba-0002a5d5c51b", "Calibrated VWC");
        mCharacteristics.put("39e1fa0a-84a8-11e2-afba-0002a5d5c51b", "Calibrated air temperature");
        mCharacteristics.put("39e1fa0b-84a8-11e2-afba-0002a5d5c51b", "Calibrated DLI");
        mCharacteristics.put("39e1fa0c-84a8-11e2-afba-0002a5d5c51b", "Calibrated Ea");
        mCharacteristics.put("39e1fa0d-84a8-11e2-afba-0002a5d5c51b", "Calibrated Ecb");
        mCharacteristics.put("39e1fa0e-84a8-11e2-afba-0002a5d5c51b", "Calibrated Ec porous");
        mCharacteristics.put("39e1fb01-84a8-11e2-afba-0002a5d5c51b", "Tx buffer");
        mCharacteristics.put("39e1fb02-84a8-11e2-afba-0002a5d5c51b", "Tx Status");
        mCharacteristics.put("39e1fb03-84a8-11e2-afba-0002a5d5c51b", "Rx Status");
        mCharacteristics.put("39e1fb04-84a8-11e2-afba-0002a5d5c51b", "Rx buffer");
        mCharacteristics.put("39e1fc01-84a8-11e2-afba-0002a5d5c51b", "Nb entries");
        mCharacteristics.put("39e1fc02-84a8-11e2-afba-0002a5d5c51b", "Last entry index");
        mCharacteristics.put("39e1fc03-84a8-11e2-afba-0002a5d5c51b", "Transfer Start Index");
        mCharacteristics.put("39e1fc04-84a8-11e2-afba-0002a5d5c51b", "Current Session ID");
        mCharacteristics.put("39e1fc05-84a8-11e2-afba-0002a5d5c51b", "Current Session Start Index");
        mCharacteristics.put("39e1fc06-84a8-11e2-afba-0002a5d5c51b", "Current Session Period");
        mCharacteristics.put("39e1fd01-84a8-11e2-afba-0002a5d5c51b", "FlowerPower current time (s)");
        mCharacteristics.put("39e1fe01-84a8-11e2-afba-0002a5d5c51b", "Calibration data");
        mCharacteristics.put("39e1fe02-84a8-11e2-afba-0002a5d5c51b", "Force bond characteristic");
        mCharacteristics.put("39e1fe03-84a8-11e2-afba-0002a5d5c51b", "Name");
        mCharacteristics.put("39e1fe04-84a8-11e2-afba-0002a5d5c51b", "Color");
        mCharacteristics.put("f000ffc1-0451-4000-b000-000000000000", "OAD Image notify");
        mCharacteristics.put("f000ffc2-0451-4000-b000-000000000000", "OAD Image block request/response");

        mCharacteristicTypes.put("00002a37-0000-1000-8000-00805f9b34fb", "integer");
        mCharacteristicTypes.put("00002a04-0000-1000-8000-00805f9b34fb", "integer");
        mCharacteristicTypes.put("00002a23-0000-1000-8000-00805f9b34fb", "integer");
        mCharacteristicTypes.put("00002a50-0000-1000-8000-00805f9b34fb", "integer");
        mCharacteristicTypes.put("39e1fd01-84a8-11e2-afba-0002a5d5c51b", "integer");
        mCharacteristicTypes.put("39e1fa03-84a8-11e2-afba-0002a5d5c51b", "integer");
        mCharacteristicTypes.put("39e1fa04-84a8-11e2-afba-0002a5d5c51b", "integer");
        mCharacteristicTypes.put("39e1fa02-84a8-11e2-afba-0002a5d5c51b", "integer");
        mCharacteristicTypes.put("39e1fa05-84a8-11e2-afba-0002a5d5c51b", "integer");
        mCharacteristicTypes.put("39e1fa01-84a8-11e2-afba-0002a5d5c51b", "integer");
        mCharacteristicTypes.put("19b10001-e8f2-537e-4f6c-d104768a1214", "boolean");
        mCharacteristicTypes.put("39e1fa07-84a8-11e2-afba-0002a5d5c51b", "boolean");
        mCharacteristicTypes.put("39e1fa06-84a8-11e2-afba-0002a5d5c51b", "boolean");

        mValueFormats.put(Integer.valueOf(52), "32bit float");
        mValueFormats.put(Integer.valueOf(50), "16bit float");
        mValueFormats.put(Integer.valueOf(34), "16bit signed int");
        mValueFormats.put(Integer.valueOf(36), "32bit signed int");
        mValueFormats.put(Integer.valueOf(33), "8bit signed int");
        mValueFormats.put(Integer.valueOf(18), "16bit unsigned int");
        mValueFormats.put(Integer.valueOf(20), "32bit unsigned int");
        mValueFormats.put(Integer.valueOf(17), "8bit unsigned int");

        // lets add also couple appearance string description
        // https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.gap.appearance.xml
        mAppearance.put(Integer.valueOf(833), "Heart Rate Sensor: Belt");
        mAppearance.put(Integer.valueOf(832), "Generic Heart Rate Sensor");
        mAppearance.put(Integer.valueOf(0), "Unknown");
        mAppearance.put(Integer.valueOf(64), "Generic Phone");
        mAppearance.put(Integer.valueOf(1157), "Cycling: Speed and Cadence Sensor");
        mAppearance.put(Integer.valueOf(1152), "General Cycling");
        mAppearance.put(Integer.valueOf(1153), "Cycling Computer");
        mAppearance.put(Integer.valueOf(1154), "Cycling: Speed Sensor");
        mAppearance.put(Integer.valueOf(1155), "Cycling: Cadence Sensor");
        mAppearance.put(Integer.valueOf(1156), "Cycling: Speed and Cadence Sensor");
        mAppearance.put(Integer.valueOf(1157), "Cycling: Power Sensor");

        mHeartRateSensorLocation.put(Integer.valueOf(0), "Other");
        mHeartRateSensorLocation.put(Integer.valueOf(1), "Chest");
        mHeartRateSensorLocation.put(Integer.valueOf(2), "Wrist");
        mHeartRateSensorLocation.put(Integer.valueOf(3), "Finger");
        mHeartRateSensorLocation.put(Integer.valueOf(4), "Hand");
        mHeartRateSensorLocation.put(Integer.valueOf(5), "Ear Lobe");
        mHeartRateSensorLocation.put(Integer.valueOf(6), "Foot");
    }
}
