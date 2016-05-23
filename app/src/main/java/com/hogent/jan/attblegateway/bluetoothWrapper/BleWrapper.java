package com.hogent.jan.attblegateway.bluetoothWrapper;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Jan on 25/02/2016.
 */
public class BleWrapper {
    private final String TAG = getClass().getSimpleName();

    /* defines (in milliseconds) how often RSSI should be updated */
    private static final int RSSI_UPDATE_TIME_INTERVAL = 1500; // 1.5 seconds

    /* callback object through which we are returning results to the caller */
    private BleWrapperUiCallbacks mUiCallback = null;
    /* define NULL object for UI callbacks */
    private static final BleWrapperUiCallbacks NULL_CALLBACK = new BleWrapperUiCallbacks.Null();

    private Activity mParent = null;
    private boolean mConnected = false;
    private String mDeviceAddress = "";

    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mBluetoothDevice = null;
    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothGattService mBluetoothSelectedService = null;
    private List<BluetoothGattService> mBluetoothGattServices = null;

    private Handler mTimerHandler = new Handler();
    private boolean mTimerEnabled = false;

    /* creates BleWrapper object, set its parent activity and callback object */
    public BleWrapper(Activity parent, BleWrapperUiCallbacks callback) {
        this.mParent = parent;
        mUiCallback = callback;
        if (mUiCallback == null) {
            mUiCallback = NULL_CALLBACK;
        }
    }

    public BluetoothManager getManager() {
        return mBluetoothManager;
    }

    public BluetoothAdapter getAdapter() {
        return mBluetoothAdapter;
    }

    public BluetoothDevice getDevice() {
        return mBluetoothDevice;
    }

    public BluetoothGatt getGatt() {
        return mBluetoothGatt;
    }

    public BluetoothGattService getCachedService() {
        return mBluetoothSelectedService;
    }

    public List<BluetoothGattService> getCachedServices() {
        return mBluetoothGattServices;
    }

    public boolean isConnected() {
        return mConnected;
    }

    /* run test and check if this device has BT and BLE hardware available */
    public boolean checkBleHardwareAvailable() {
        // First check general Bluetooth Hardware:
        // get BluetoothManager...
        final BluetoothManager manager = (BluetoothManager) mParent.getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager == null) {
            return false;
        }
        // .. and then get adapter from manager
        final BluetoothAdapter adapter = manager.getAdapter();
        if (adapter == null) {
            return false;
        }

        // and then check if BT LE is also available
        return mParent.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }


    /* before any action check if BT is turned ON and enabled for us
     * call this in onResume to be always sure that BT is ON when Your
     * application is put into the foreground */
    public boolean isBtEnabled() {
        final BluetoothManager manager = (BluetoothManager) mParent.getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager == null) {
            return false;
        }

        final BluetoothAdapter adapter = manager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    /* start scanning for BT LE devices around */
    public void startScanning() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        mBluetoothAdapter.getBluetoothLeScanner().startScan(null, builder.build(), mDeviceFoundCallback);
    }

    /* stops current scanning */
    public void stopScanning() {
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mDeviceFoundCallback);
    }

    /* initialize BLE and get BT Manager & Adapter */
    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mParent.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }

        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
        return mBluetoothAdapter != null;
    }

    /* connect to the device with specified address */
    public boolean connect(final String deviceAddress) {
        if (mBluetoothAdapter == null || deviceAddress == null) {
            return false;
        }
        mDeviceAddress = deviceAddress;

        // check if we need to connect from scratch or just reconnect to previous device
        if (mBluetoothGatt != null && mBluetoothGatt.getDevice().getAddress().equals(deviceAddress)) {
            // just reconnect
            return mBluetoothGatt.connect();
        } else {
            // connect from scratch
            // get BluetoothDevice object for specified address
            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
            if (mBluetoothDevice == null) {
                // we got wrong address - that device is not available!
                return false;
            }
            // connect with remote device
            mBluetoothGatt = mBluetoothDevice.connectGatt(mParent, false, mBleCallback);
        }
        return true;
    }

    /* disconnect the device. It is still possible to reconnect to it later with this Gatt client */
    public void disconnect() {
        if (mBluetoothGatt != null) mBluetoothGatt.disconnect();
        mUiCallback.uiDeviceDisconnected(mBluetoothGatt, mBluetoothDevice);
    }

    /* close GATT client completely */
    public void close() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
        mBluetoothGatt = null;
    }

    /* request new RSSi value for the connection*/
    public void readPeriodicalyRssiValue(final boolean repeat) {
        mTimerEnabled = repeat;
        // check if we should stop checking RSSI value
        if (!mConnected || mBluetoothGatt == null || !mTimerEnabled) {
            mTimerEnabled = false;
            return;
        }

        mTimerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothGatt == null || mBluetoothAdapter == null || !mConnected) {
                    mTimerEnabled = false;
                    return;
                }

                // request RSSI value
                mBluetoothGatt.readRemoteRssi();
                // add call it once more in the future
                readPeriodicalyRssiValue(mTimerEnabled);
            }
        }, RSSI_UPDATE_TIME_INTERVAL);
    }

    /* starts monitoring RSSI value */
    public void startMonitoringRssiValue() {
        readPeriodicalyRssiValue(true);
    }

    /* stops monitoring of RSSI value */
    public void stopMonitoringRssiValue() {
        readPeriodicalyRssiValue(false);
    }

    /* request to discover all services available on the remote devices
     * results are delivered through callback object */
    public void startServicesDiscovery() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.discoverServices();
        }
    }

    /* gets services and calls UI callback to handle them
     * before calling getServices() make sure service discovery is finished! */
    public void getSupportedServices() {
        if (mBluetoothGattServices != null && mBluetoothGattServices.size() > 0) {
            mBluetoothGattServices.clear();
        }
        // keep reference to all services in local array:
        if (mBluetoothGatt != null) {
            mBluetoothGattServices = mBluetoothGatt.getServices();
        }

        mUiCallback.uiAvailableServices(mBluetoothGatt, mBluetoothDevice, mBluetoothGattServices);
    }

    /* get all characteristic for particular service and pass them to the UI callback */
    public void getCharacteristicsForService(final BluetoothGattService service) {
        if (service == null) {
            return;
        }
        List<BluetoothGattCharacteristic> chars = service.getCharacteristics();

        mUiCallback.uiCharacteristicForService(mBluetoothGatt, mBluetoothDevice, service, chars);
        // keep reference to the last selected service
        mBluetoothSelectedService = service;
    }

    /* request to fetch newest value stored on the remote device for particular characteristic */
    public void requestCharacteristicValue(BluetoothGattService service, BluetoothGattCharacteristic ch) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }

        mBluetoothSelectedService = service;

        mBluetoothGatt.readCharacteristic(ch);
        // new value available will be notified in Callback Object
    }

    /* get characteristic's value (and parse it for some types of characteristics)
     * before calling this You should always update the value by calling requestCharacteristicValue() */
    public void getCharacteristicValue(BluetoothGattCharacteristic ch) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null || ch == null) {
            return;
        }

        byte[] rawValue = ch.getValue();
        String strValue = null;
        double doubleValue = 0;

        // lets read and do real parsing of some characteristic to get meaningful value from it
        UUID uuid = ch.getUuid();

        if (uuid.equals(BleDefinedUUIDs.Characteristic.HEART_RATE_MEASUREMENT)) { // heart rate
            // follow https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
            // first check format used by the device - it is specified in bit 0 and tells us if we should ask for index 1 (and uint8) or index 2 (and uint16)
            int index = ((rawValue[0] & 0x01) == 1) ? 2 : 1;
            // also we need to define format
            int format = (index == 1) ? BluetoothGattCharacteristic.FORMAT_UINT8 : BluetoothGattCharacteristic.FORMAT_UINT16;
            // now we have everything, get the value
            doubleValue = ch.getIntValue(format, index);
            strValue = doubleValue + " bpm"; // it is always in bpm units
        } else if (uuid.equals(BleDefinedUUIDs.Characteristic.HEART_RATE_MEASUREMENT) || // manufacturer name string
                uuid.equals(BleDefinedUUIDs.Characteristic.MODEL_NUMBER_STRING) || // model number string)
                uuid.equals(BleDefinedUUIDs.Characteristic.FIRMWARE_REVISION_STRING)) // firmware revision string
        {
            // follow https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.manufacturer_name_string.xml etc.
            // string value are usually simple utf8s string at index 0
            strValue = ch.getStringValue(0);
        } else if (uuid.equals(BleDefinedUUIDs.Characteristic.APPEARANCE)) { // appearance
            // follow: https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.gap.appearance.xml
            doubleValue = ((double) rawValue[1]) * 256;
            doubleValue += rawValue[0];
            strValue = BleNamesResolver.resolveAppearance((int) doubleValue);
        } else if (uuid.equals(BleDefinedUUIDs.Characteristic.BODY_SENSOR_LOCATION)) { // body sensor location
            // follow: https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.body_sensor_location.xml
            doubleValue = rawValue[0];
            strValue = BleNamesResolver.resolveHeartRateSensorLocation((int) doubleValue);
        } else if (uuid.equals(BleDefinedUUIDs.Characteristic.BATTERY_LEVEL)) { // battery level
            // follow: https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.battery_level.xml
            doubleValue = rawValue[0];
            strValue = "" + doubleValue + "% battery level";
        }
        // flower power calculations
        else if (uuid.equals(BleDefinedUUIDs.Characteristic.SOILTEMP) ||
                uuid.equals(BleDefinedUUIDs.Characteristic.AIRTEMP)) {

            ByteBuffer bb = ByteBuffer.wrap(rawValue);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            short value = bb.getShort();
            doubleValue = 0.00000003044 * Math.pow(value, 3.0) - 0.00008038 * Math.pow(value, 2.0) + value * 0.1149 - 30.449999999999999;

            strValue = doubleValue + " °C";
        } else if (uuid.equals(BleDefinedUUIDs.Characteristic.LIGHT)) {
            ByteBuffer bb = ByteBuffer.wrap(rawValue);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            short value = bb.getShort();
            doubleValue = 0.08640000000000001 * (192773.17000000001 * Math.pow(value, -1.0606619));

            strValue = String.valueOf(doubleValue);
        } else if (uuid.equals(BleDefinedUUIDs.Characteristic.SOILEC)) {
            ByteBuffer bb = ByteBuffer.wrap(rawValue);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            doubleValue = bb.getShort();

            strValue = String.valueOf(doubleValue);
        } else if (uuid.equals(BleDefinedUUIDs.Characteristic.SOILVWC)) {
            ByteBuffer bb = ByteBuffer.wrap(rawValue);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            short value = bb.getShort();
            double moisture = 11.4293 + (0.0000000010698 * Math.pow(value, 4.0) - 0.00000152538 * Math.pow(value, 3.0) + 0.000866976 * Math.pow(value, 2.0) - 0.169422 * value);
            doubleValue = 100.0 * (0.0000045 * Math.pow(moisture, 3.0) - 0.00055 * Math.pow(moisture, 2.0) + 0.0292 * moisture - 0.053);

            strValue = String.valueOf(doubleValue);
        } else {
            // not known type of characteristic, so we need to handle this in "general" way
            // get first four bytes and transform it to integer
            doubleValue = 0;
            if (rawValue.length > 0) {
                doubleValue = (int) rawValue[0];
            }
            if (rawValue.length > 1) {
                doubleValue = doubleValue + ((double) rawValue[1] * 256);
            }
            if (rawValue.length > 2) {
                doubleValue = doubleValue + ((double) rawValue[2] * 8);
            }
            if (rawValue.length > 3) {
                doubleValue = doubleValue + ((double) rawValue[3] * 8);
            }

            if (rawValue.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(rawValue.length);
                for (byte byteChar : rawValue) {
                    try {
                        stringBuilder.append(String.format("%c", byteChar));
                    } catch (Exception ignored) {
                    }
                }
                strValue = stringBuilder.toString();
            }
        }

        String timestamp = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS").format(new Date());
        mUiCallback.uiNewValueForCharacteristic(mBluetoothGatt,
                mBluetoothDevice,
                mBluetoothSelectedService,
                ch,
                strValue,
                doubleValue,
                rawValue,
                timestamp);
    }

    /* reads and return what what FORMAT is indicated by characteristic's properties
     * seems that value makes no sense in most cases */
    public int getValueFormat(BluetoothGattCharacteristic ch) {
        int properties = ch.getProperties();

        if ((BluetoothGattCharacteristic.FORMAT_FLOAT & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_FLOAT;
        }
        if ((BluetoothGattCharacteristic.FORMAT_SFLOAT & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_SFLOAT;
        }
        if ((BluetoothGattCharacteristic.FORMAT_SINT16 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_SINT16;
        }
        if ((BluetoothGattCharacteristic.FORMAT_SINT32 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_SINT32;
        }
        if ((BluetoothGattCharacteristic.FORMAT_SINT8 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_SINT8;
        }
        if ((BluetoothGattCharacteristic.FORMAT_UINT16 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_UINT16;
        }
        if ((BluetoothGattCharacteristic.FORMAT_UINT32 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_UINT32;
        }
        if ((BluetoothGattCharacteristic.FORMAT_UINT8 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_UINT8;
        }

        return 0;
    }

    /* set new value for particular characteristic */
    public void writeDataToCharacteristic(final BluetoothGattCharacteristic ch, final byte[] dataToWrite) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null || ch == null) {
            return;
        }

        // first set it locally....
        ch.setValue(dataToWrite);
        // ... and then "commit" changes to the peripheral
        mBluetoothGatt.writeCharacteristic(ch);
    }

    /* enables/disables notification for characteristic */
    public void setNotificationForCharacteristic(BluetoothGattCharacteristic ch, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }

        boolean success = mBluetoothGatt.setCharacteristicNotification(ch, enabled);
        if (!success) {
            Log.e("------", "Seting proper notification status for characteristic failed!");
        }

        // This is also sometimes required (e.g. for heart rate monitors) to enable notifications/indications
        // see: https://developer.bluetooth.org/gatt/descriptors/Pages/DescriptorViewer.aspx?u=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
        BluetoothGattDescriptor descriptor = ch.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        if (descriptor != null) {
            byte[] val = enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            descriptor.setValue(val);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    private ScanCallback mDeviceFoundCallback = new ScanCallback() {
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult() called with: " + "callbackType = [" + callbackType + "], result = [" + result + "]");
            mUiCallback.uiDeviceFound(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
        }

        /**
         * Callback when batch results are delivered.
         *
         * @param results List of scan results that are previously scanned.
         */
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG, "onBatchScanResults() called with: " + "results = [" + results + "]");
        }

        /**
         * Callback when scan could not be started.
         *
         * @param errorCode Error code (one of SCAN_FAILED_*) for scan failure.
         */
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "onScanFailed() called with: " + "errorCode = [" + errorCode + "]");
        }

    };

    /* callbacks called for any action on particular Ble Device */
    private final BluetoothGattCallback mBleCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnected = true;
                mUiCallback.uiDeviceConnected(mBluetoothGatt, mBluetoothDevice);

                // now we can start talking with the device, e.g.
                mBluetoothGatt.readRemoteRssi();
                // response will be delivered to callback object!

                // in our case we would also like automatically to call for services discovery
                startServicesDiscovery();

                // and we also want to get RSSI value to be updated periodically
                startMonitoringRssiValue();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnected = false;
                mUiCallback.uiDeviceDisconnected(mBluetoothGatt, mBluetoothDevice);
                try {
                    mBluetoothGatt.close();
                } catch (Exception e) {
                    Log.d("DDDD", "close ignoring: " + e);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // now, when services discovery is finished, we can call getServices() for Gatt
                getSupportedServices();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            // we got response regarding our request to fetch characteristic value
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // and it success, so we can get the value
                getCharacteristicValue(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            // characteristic's value was updated due to enabled notification, lets get this value
            // the value itself will be reported to the UI inside getCharacteristicValue
            getCharacteristicValue(characteristic);
            // also, notify UI that notification are enabled for particular characteristic
            mUiCallback.uiGotNotification(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService, characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            String deviceName = gatt.getDevice().getName();
            String serviceName = BleNamesResolver.resolveServiceName(characteristic.getService().getUuid().toString().toLowerCase(Locale.getDefault()));
            String charName = BleNamesResolver.resolveCharacteristicName(characteristic.getUuid().toString().toLowerCase(Locale.getDefault()));
            String description = "Device: " + deviceName + " Service: " + serviceName + " Characteristic: " + charName;

            // we got response regarding our request to write new value to the characteristic
            // let see if it failed or not
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mUiCallback.uiSuccessfulWrite(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService, characteristic, description);
            } else {
                mUiCallback.uiFailedWrite(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService, characteristic, description + " STATUS = " + status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // we got new value of RSSI of the connection, pass it to the UI
                mUiCallback.uiNewRssiAvailable(mBluetoothGatt, mBluetoothDevice, rssi);
            }
        }
    };
}
