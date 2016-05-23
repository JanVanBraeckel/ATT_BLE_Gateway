package com.hogent.jan.attblegateway.ATTBLE.Model;

/**
 * Contains the data that we found when an actuator value was sent from the cloud to a device.
 */
public abstract class ActuatorData {
    private String asset;

    public abstract void load(String value);

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }
}
