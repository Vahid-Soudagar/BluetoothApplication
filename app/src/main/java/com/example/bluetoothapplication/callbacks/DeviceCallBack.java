package com.example.bluetoothapplication.callbacks;

import android.bluetooth.BluetoothDevice;

public interface DeviceCallBack {
    void onDeviceConnected(BluetoothDevice device);
    void onDeviceDisconnected(BluetoothDevice device, String message);
    void onMessage(byte[] message);
    void onError(int errorCode);
    void onConnectError(BluetoothDevice device, String message);
}
