package com.hogent.jan.attblegateway.ExpandableListView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.hogent.jan.attblegateway.R;
import com.hogent.jan.attblegateway.bluetoothWrapper.BleCharacteristic;
import com.hogent.jan.attblegateway.bluetoothWrapper.BleNamesResolver;
import com.hogent.jan.attblegateway.bluetoothWrapper.BleService;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.ButterKnife;

/**
 * Created by Jan on 28/02/2016.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private List<BleService> mBluetoothServices;
    private ExpandableListAdapterListener mListener;

    public ExpandableListAdapter(Context context) {
        mContext = context;
        mBluetoothServices = new ArrayList<>();
    }

    public void addServices(List<BleService> services) {
        for (BleService service : services) {
            if (!mBluetoothServices.contains(service)) {
                mBluetoothServices.add(service);
            }
        }

        notifyDataSetChanged();
    }

    public void addCharacteristicsForService(BluetoothGattService service, List<BleCharacteristic> characteristics) {
        for(BleService s : mBluetoothServices){
            if(s.getBleService() == service){
                s.setBleCharacteristics(characteristics);
            }
        }

        notifyDataSetChanged();
    }

    public void newValueForCharacteristic(BluetoothGattService bluetoothGattService, BluetoothGattCharacteristic characteristic, String strValue, double doubleValue, byte[] rawValue, String timestamp) {
        for(BleService s : mBluetoothServices){
            if(s.getBleService() == bluetoothGattService){
                s.newValueForCharacteristic(characteristic, strValue, doubleValue, rawValue, timestamp);
            }
        }

        notifyDataSetChanged();
    }

    public void onActuatorValue(String service, String characteristic, String message) {
        for(BleService s : mBluetoothServices){
            if(s.getBleService().getUuid().toString().replace("-", "").equals(service)){
                for(BleCharacteristic ch : s.getBleCharacteristics()){
                    if(ch.getBleCharacteristic().getUuid().toString().replace("-", "").equals(characteristic)){
                        BluetoothGattService serv = s.getBleService();
                        BluetoothGattCharacteristic charact = ch.getBleCharacteristic();
                        String type = BleNamesResolver.resolveCharacteristicType(charact.getUuid().toString());

                        switch (type) {
                            case "integer":
                                try {
                                    int val = Integer.valueOf(message);
                                    byte[] byteArray = new byte[]{
                                            (byte) (val >>> 24),
                                            (byte) (val >>> 16),
                                            (byte) (val >>> 8),
                                            (byte) val
                                    };
                                    mListener.writeDataToCharacteristic(serv, charact, byteArray, message);
                                } catch (Exception ignored) {
                                }
                                break;
                            case "boolean":
                                try {
                                    if (Boolean.parseBoolean(message)) {
                                        mListener.writeDataToCharacteristic(serv, charact, parseHexStringToBytes("0x01"), message);
                                    } else {
                                        mListener.writeDataToCharacteristic(serv, charact, parseHexStringToBytes("0x00"), message);
                                    }
                                } catch (Exception ignored) {
                                }
                                break;
                            default:
                                try {
                                    mListener.writeDataToCharacteristic(serv, charact, message.getBytes("UTF-8"), message);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }
            }
        }
    }

    public void clearLists() {
        mBluetoothServices.clear();
        notifyDataSetChanged();
    }

    public byte[] parseHexStringToBytes(final String hex) {
        String tmp = hex.substring(2).replaceAll("[^[0-9][a-f]]", "");
        byte[] bytes = new byte[tmp.length() / 2]; // every two letters in the string are one byte finally

        String part = "";

        for (int i = 0; i < bytes.length; ++i) {
            part = "0x" + tmp.substring(i * 2, i * 2 + 2);
            bytes[i] = Long.decode(part).byteValue();
        }

        return bytes;
    }

    @Override
    public BluetoothGattCharacteristic getChild(int groupPosition, int childPosition) {
        return mBluetoothServices.get(groupPosition).getBleCharacteristics().get(childPosition).getBleCharacteristic();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final BluetoothGattService service = getGroup(groupPosition);
        final BluetoothGattCharacteristic characteristic = getChild(groupPosition, childPosition);
        final String characteristicName = BleNamesResolver.resolveUuid(characteristic.getUuid().toString());
        final String characteristicUUID = characteristic.getUuid().toString().toUpperCase();

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_characteristic, null);
        }

        TextView lblCharacteristicName = (TextView) convertView.findViewById(R.id.deviceServiceCharacteristic);
        TextView lblCharacteristicUUID = (TextView) convertView.findViewById(R.id.deviceServiceCharacteristicUUID);
        lblCharacteristicName.setText(characteristicName);
        lblCharacteristicUUID.setText(characteristicUUID);

        final LinearLayout detailView = ButterKnife.findById(convertView, R.id.characteristicDetail);

        if (detailView.getVisibility() == View.VISIBLE) {
            final TextView lblValue = ButterKnife.findById(detailView, R.id.characteristicDetailValue);
            TextView lblStringValue = ButterKnife.findById(detailView, R.id.characteristicDetailStringValue);
            TextView lblDecimalValue = ButterKnife.findById(detailView, R.id.characteristicDetailDecimalValue);
            TextView lblUpdated = ButterKnife.findById(detailView, R.id.characteristicDetailLastUpdated);

            BleCharacteristic ch = mBluetoothServices.get(groupPosition).getBleCharacteristics().get(childPosition);
            lblValue.setText(ch.getAsciiValue());
            lblDecimalValue.setText(String.format("%.2f", ch.getDoubleValue()));
            lblStringValue.setText(ch.getStrValue());
            lblUpdated.setText(ch.getTimestamp());
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (detailView.getVisibility() == View.GONE) {
                    detailView.setVisibility(View.VISIBLE);

                    TextView lblDataType = ButterKnife.findById(detailView, R.id.characteristicDetailDataType);
                    TextView lblProperties = ButterKnife.findById(detailView, R.id.characteristicDetailProperties);
                    ToggleButton btnNotification = ButterKnife.findById(detailView, R.id.characteristicDetailNotificationToggle);
                    Button btnRead = ButterKnife.findById(detailView, R.id.characteristicDetailRead);
                    Button btnWrite = ButterKnife.findById(detailView, R.id.characteristicDetailWrite);
                    final TextView lblValue = ButterKnife.findById(detailView, R.id.characteristicDetailValue);
                    TextView lblStringValue = ButterKnife.findById(detailView, R.id.characteristicDetailStringValue);
                    TextView lblDecimalValue = ButterKnife.findById(detailView, R.id.characteristicDetailDecimalValue);
                    TextView lblUpdated = ButterKnife.findById(detailView, R.id.characteristicDetailLastUpdated);

                    btnNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                mListener.setNotificationForCharacteristic(service, characteristic, true);
                                mListener.requestCharacteristicValue(service, characteristic);
                            } else {
                                mListener.setNotificationForCharacteristic(service, characteristic, false);
                            }
                        }
                    });

                    btnWrite.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setTitle("Write hex value");

                            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                            builder.setView(inflater.inflate(R.layout.alertdialog_input, null))
                                    .setPositiveButton("Write", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Dialog f = (Dialog) dialog;
                                            EditText input = ButterKnife.findById(f, R.id.txtWriteValue);
                                            if(input.getText() != null && !input.getText().toString().equals("")){
                                                String value = input.getText().toString().toLowerCase(Locale.getDefault());
                                                byte[] dataToWrite = parseHexStringToBytes("0x" + value);
                                                mListener.writeDataToCharacteristic(service, characteristic, dataToWrite, value);
                                            }
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                            builder.show();
                        }
                    });

                    btnRead.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mListener.requestCharacteristicValue(service, characteristic);
                        }
                    });

                    int format = mListener.getValueFormat(characteristic);
                    lblDataType.setText(BleNamesResolver.resolveValueTypeDescription(format));
                    int properties = characteristic.getProperties();
                    String propertiesString = String.format("0x%04X [", properties);
                    if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                        propertiesString += "read ";
                    }
                    if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
                        propertiesString += "write ";
                    }
                    if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                        propertiesString += "notify ";
                    }
                    if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                        propertiesString += "indicate ";
                    }
                    if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                        propertiesString += "write_no_response ";
                    }
                    if (propertiesString.endsWith(" ")) {
                        propertiesString = propertiesString.substring(0, propertiesString.length() - 1);
                    }
                    lblProperties.setText(propertiesString + "]");

                    btnNotification.setEnabled((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0);
                    btnRead.setEnabled((properties & BluetoothGattCharacteristic.PROPERTY_READ) != 0);
                    btnWrite.setEnabled((properties & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0);

                    BleCharacteristic ch = mBluetoothServices.get(groupPosition).getBleCharacteristics().get(childPosition);
                    lblValue.setText(ch.getAsciiValue());
                    lblDecimalValue.setText(String.format("%.2f", ch.getDoubleValue()));
                    lblStringValue.setText(ch.getStrValue());
                    lblUpdated.setText(ch.getTimestamp());
                    detailView.setVisibility(View.GONE);
                    detailView.setVisibility(View.VISIBLE);
                } else {
                    detailView.setVisibility(View.GONE);
                }
            }
        });

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mBluetoothServices.get(groupPosition).getBleCharacteristics().size();
    }

    @Override
    public BluetoothGattService getGroup(int groupPosition) {
        return mBluetoothServices.get(groupPosition).getBleService();
    }

    @Override
    public int getGroupCount() {
        return mBluetoothServices.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final String serviceName = BleNamesResolver.resolveUuid(getGroup(groupPosition).getUuid().toString());
        final String serviceUUID = getGroup(groupPosition).getUuid().toString().toUpperCase();
        final String type = getGroup(groupPosition).getType() == BluetoothGattService.SERVICE_TYPE_PRIMARY ? "Primary service" : "Secondary service";
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_service, null);
        }

        TextView lblServiceName = ButterKnife.findById(convertView, R.id.deviceService);
        TextView lblServiceUUID = ButterKnife.findById(convertView, R.id.deviceServiceUUID);
        TextView lblServiceType = ButterKnife.findById(convertView, R.id.deviceServiceType);
        lblServiceName.setText(serviceName);
        lblServiceUUID.setText(serviceUUID);
        lblServiceType.setText(type);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void setExpandableListAdapterListener(ExpandableListAdapterListener mListener) {
        this.mListener = mListener;
    }

    public interface ExpandableListAdapterListener {
        void requestCharacteristicValue(BluetoothGattService service, BluetoothGattCharacteristic characteristic);

        void writeDataToCharacteristic(BluetoothGattService service, BluetoothGattCharacteristic characteristic, byte[] data, String originalValue);

        int getValueFormat(BluetoothGattCharacteristic characteristic);

        void setNotificationForCharacteristic(BluetoothGattService service, BluetoothGattCharacteristic characteristic, boolean enabled);
    }
}