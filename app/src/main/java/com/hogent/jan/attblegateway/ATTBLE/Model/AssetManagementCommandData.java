package com.hogent.jan.attblegateway.ATTBLE.Model;

/**
 * Created by Jan on 26/03/2016.
 */
public class AssetManagementCommandData {
    private String asset;
    private String command;

    public String getAsset() {
        return asset;
    }

    public String getCommand() {
        return command;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
