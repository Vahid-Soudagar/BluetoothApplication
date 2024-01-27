package com.example.bluetoothapplication.callbacks;

public interface BluetoothCallBack {
    void onBluetoothTurningOn();

    void onBluetoothOn();

    void onBluetoothTurningOff();

    void onBluetoothOff();

    void onUserDeniedActivation();
}
