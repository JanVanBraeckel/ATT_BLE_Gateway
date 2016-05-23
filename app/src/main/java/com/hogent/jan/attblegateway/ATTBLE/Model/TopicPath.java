package com.hogent.jan.attblegateway.ATTBLE.Model;


import android.text.TextUtils;

/**
 * Contains the data that defines a management command.
 */
public class TopicPath {
    private final String MANAGEMENTCHANNEL = "m";
    private final String FEEDCHANNEL = "f";
    private final String SETTERCHANNEL = "s";
    private final String GATEWAYENTITY = "gateway";
    private final String DEVICEENTITY = "device";
    private final String ASSETENTITY = "asset";
    private final String THINGENTITY = "t";
    private final String CLIENTENTITY = "client";

    private String gateway;
    private String clientId;
    private String deviceId;
    private String remoteDeviceId;
    private String remoteId;
    private String[] assetId;
    private String assetIdStr;
    private boolean isSetter;

    /**
     * Initializes a new instance of the @{@link TopicPath} class.
     * Use this constructor when the object is created to manually define a path
     */
    public TopicPath() {
    }

    /**
     * Initializes a new instance of the @{@link TopicPath} class.
     * Use this constructor when the topicPath is created for an incoming value.
     *
     * @param path The path as supplied by the pub-sub client (the topic).
     */
    public TopicPath(String[] path) {
        if (path.length < 5) {
            throw new UnsupportedOperationException("Topic structure invalid, expecting at least 6 parts");
        }

        if (path[0].equals(CLIENTENTITY)) {
            clientId = path[1];
            if (path[3].equals(GATEWAYENTITY)) {
                gateway = path[4];

                if (path.length == 10) {
                    String[] parts = path[6].split("_");
                    deviceId = parts[parts.length - 1];
                    assetId = new String[]{path[8]};
                    isSetter = path[9].equals(MANAGEMENTCHANNEL);
                } else if (path.length == 8) {
                    String[] parts = path[6].split("_");
                    if (path[5].equals(DEVICEENTITY))
                        deviceId = parts[parts.length - 1];
                    else
                        assetId = getAssetId(parts, 1);
                    isSetter = path[7].equals(MANAGEMENTCHANNEL);
                } else if (path.length == 6)
                    isSetter = true;
            } else if (path[3].equals(DEVICEENTITY)) {
                String[] parts = path[4].split("_");
                deviceId = parts[parts.length - 1];
                if (path.length == 6)
                    isSetter = path[5].equals(MANAGEMENTCHANNEL);
                else {
                    assetId = new String[]{path[6]};
                    isSetter = path[7].equals(MANAGEMENTCHANNEL);
                }
            } else {
                throw new UnsupportedOperationException("topic structure invalid, pos 2 should be 'gateway'");
            }
        } else {
            throw new UnsupportedOperationException("topic structure invalid, pos 0 should be 'client'");
        }
    }

    public TopicPath(TopicPath source) {
        assetId = source.assetId;
        deviceId = source.deviceId;
        gateway = source.gateway;
        clientId = source.clientId;
        isSetter = source.isSetter;
    }

    private String[] getAssetId(String[] parts, int offset) {
        String[] res = new String[parts.length - offset];
        for (int i = offset; i < parts.length; i++) {
            try {
                res[i - offset] = parts[i];
            } catch (Exception ignored) {
                throw new UnsupportedOperationException(String.format("Failed to convert asset id to int[], problem with: %s in %s", parts[i], TextUtils.join("_", parts)));
            }
        }

        return res;
    }

    public String getGateway() {
        return gateway;
    }

    public String getClientId() {
        return clientId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getRemoteDeviceId() {
        if (!(gateway == null || gateway.isEmpty())) {
            return String.format("%s_%s", gateway, deviceId);
        } else {
            return deviceId;
        }
    }

    public String getRemoteId() {
        return toString();
    }

    public byte getDeviceIdAsNr() {
        return (byte) Integer.parseInt(deviceId);
    }

    public String[] getAssetId() {
        return assetId;
    }

    public String getAssetIdStr() {
        return TopicPath.buildAssetStr(assetId);
    }

    public String getRemoteAssetId() {
        return String.format("%s_%s", remoteDeviceId, assetIdStr);
    }

    public boolean isSetter() {
        return isSetter;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setRemoteDeviceId(String remoteDeviceId) {
        String[] temp = remoteDeviceId.split("_");
        if (temp.length == 2) {
            gateway = temp[0];
            deviceId = temp[1];
        } else {
            throw new IllegalArgumentException("Not a device id");
        }
    }

    public void setRemoteId(String remoteId) {
        String[] temp = remoteId.split("_");
        if (temp.length > 2) {
            gateway = temp[0];
            deviceId = temp[1];
            storeAssetId(temp, 2);
        } else {
            throw new IllegalArgumentException("Not a full remote id");
        }
    }

    public void setAssetId(String[] assetId) {
        this.assetId = assetId;
    }

    public void setAssetIdStr(String assetIdStr) {
        if (!(assetIdStr == null || assetIdStr.isEmpty())) {
            String[] parts = assetIdStr.split("_");
            if (parts.length == 1) {
                storeAssetId(parts, 0);
            } else {
                storeAssetId(parts, 1);
            }
        } else {
            assetId = null;
        }
    }

    public void setRemoteAssetId(String remoteAssetId) {
        if (!(remoteAssetId == null || remoteAssetId.isEmpty())) {
            String[] parts = remoteAssetId.split("_");
            if (parts.length > 2) {
                gateway = parts[0];
                deviceId = parts[1];
                assetId = new String[parts.length - 2];
                for (int i = 2; i < parts.length; i++) {
                    try {
                        assetId[i - 2] = parts[i];
                    } catch (Exception ignored) {
                        throw new UnsupportedOperationException(String.format("Can't convert string to asset id: %s", remoteAssetId));
                    }
                }
            }
        }
    }

    private void storeAssetId(String[] parts, int offset) {
        assetId = new String[parts.length - offset];
        for (int i = offset; i < parts.length; i++) {
            try {
                assetId[i - offset] = parts[i];
            } catch (Exception ignored) {
                throw new UnsupportedOperationException(String.format("Can't convert string to asset id: %s", TextUtils.join("_", parts)));
            }
        }
    }

    public static String buildAssetStr(String[] assetPath) {
        if(assetPath!=null){
            StringBuilder builder = new StringBuilder();
            if(assetPath.length > 0){
                builder.append(assetPath[0]);
                for(int i = 1; i < assetPath.length; i++){
                    builder.append(assetPath[i]);
                }
            }
            return builder.toString();
        }
        return null;
    }

    @Override
    public String toString() {
        String assetId;
        if (!(deviceId == null || deviceId.isEmpty())) {
            if (!(gateway == null || gateway.isEmpty())) {
                assetId = String.format("%s_%s_%s", gateway, deviceId, assetIdStr);
            } else {
                assetId = String.format("%s_%s", deviceId, assetIdStr);
            }
        } else {
            assetId = String.format("%s_%s", gateway, assetIdStr);
        }
        return String.format("client/%s/out/asset/%s/state", clientId, assetId);
    }
}
