package com.example.bluetoothapplication.data;


public class Temp {
    public int TEMP_INVALID = 0;

    private double temperature;
    private int status;


    public Temp(double temperature, int status) {
        this.temperature = temperature;
        this.status = status;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return  String.format("TEMP: %.1f °C",temperature);
    }
}
