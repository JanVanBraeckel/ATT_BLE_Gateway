package com.hogent.jan.attblegateway.ATTBLE.Model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Exchanger;

/**
 * Contains the data that we found when an actuator value was sent from the cloud to a device.
 */
public class StringActuatorData extends ActuatorData{
    private String value;

    @Override
    public void load(String value) {
        this.value = value;
    }

    public double getAsDouble(){
        try{
            return Double.parseDouble(value);
        }catch(Exception ignored){
            throw new IllegalArgumentException("Expected double value");
        }
    }

    public boolean getAsBool(){
        try{
            return Boolean.parseBoolean(value);
        }catch (Exception ignored){
            throw new IllegalArgumentException("Expected boolean value");
        }
    }

    public int getAsInt(){
        try{
            return Integer.parseInt(value);
        }catch (Exception ignored){
            throw new IllegalArgumentException("Expected integer value");
        }
    }

    public Date getAsDate(){
        try{
            return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(value);
        }catch (Exception ignored){
            throw new IllegalArgumentException("Expected Date value");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
