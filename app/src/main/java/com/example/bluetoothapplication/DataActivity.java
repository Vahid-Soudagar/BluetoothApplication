package com.example.bluetoothapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.bluetoothapplication.bluetooth.Bluetooth;
import com.example.bluetoothapplication.callbacks.BluetoothCallBack;
import com.example.bluetoothapplication.callbacks.DeviceCallBack;
import com.example.bluetoothapplication.callbacks.DiscoveryCallBack;
import com.example.bluetoothapplication.databinding.ActivityDataBinding;
import com.example.bluetoothapplication.utils.BluetoothDeviceAdapter;
import com.example.bluetoothapplication.utils.SearchDevicesDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DataActivity extends AppCompatActivity {

    private static final String TAG = "myTag";
    private ActivityDataBinding dataBinding;
    private Bluetooth bluetooth;
    private BluetoothDeviceAdapter bluetoothDeviceAdapter;
    private SearchDevicesDialog searchDevicesDialog;
    private ProgressDialog progressDialog;
    private List<BluetoothDevice> bluetoothDeviceList;
    private boolean scanning = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = ActivityDataBinding.inflate(getLayoutInflater());
        setContentView(dataBinding.getRoot());



        bluetooth = new Bluetooth(this);
        bluetooth.setCallbackOnUI(this);
        bluetooth.setBluetoothCallback(bluetoothCallBack);
        bluetooth.setDiscoveryCallback(discoveryCallBack);
        bluetooth.setDeviceCallback(deviceCallBack);


        bluetoothDeviceList = new ArrayList<>();
        bluetoothDeviceAdapter = new BluetoothDeviceAdapter(
                this,
                bluetoothDeviceList
        );
        searchDevicesDialog = new SearchDevicesDialog(
                this,
                bluetoothDeviceAdapter
        ) {
            @Override
            public void onStartSearch() {
                bluetooth.startScanning();
            }

            @Override
            public void onClickDeviceItem(int pos) {
                if (scanning) {
                    bluetooth.stopScanning();
                }
                setProgressAndState("Pairing");
                bluetooth.pair(bluetoothDeviceList.get(pos));
                progressDialog.show();
                searchDevicesDialog.dismiss();
            }
        };


        searchDevicesDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                bluetooth.stopScanning();
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Connecting...");


        dataBinding.layoutBluetooth.btnBtCtr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bluetooth.isConnected()) {
                    searchDevicesDialog.show();
                    searchDevicesDialog.startSearch();
                    bluetooth.startScanning();
                } else {
                    bluetooth.disconnect();
                    dataBinding.layoutBluetooth.tvbtinfo.setText("");
                }
            }
        });

    }

    private void setProgressAndState(String msg){
        dataBinding.layoutBluetooth.tvbtinfo.setText(msg);
    }

    private BluetoothCallBack bluetoothCallBack = new BluetoothCallBack() {
        @Override
        public void onBluetoothTurningOn() {

        }

        @Override
        public void onBluetoothOn() {

        }

        @Override
        public void onBluetoothTurningOff() {

        }

        @Override
        public void onBluetoothOff() {

        }

        @Override
        public void onUserDeniedActivation() {

        }
    };


    @SuppressLint("MissingPermission")
    private DiscoveryCallBack discoveryCallBack = new DiscoveryCallBack() {
        @Override
        public void onDiscoveryStarted() {
            scanning = true;
            Log.d(TAG, "Discovery Started");
        }

        @Override
        public void onDiscoveryFinished() {
            scanning = false;
            Log.d(TAG, "Discovery Finished");
        }


        @Override
        public void onDeviceFound(BluetoothDevice device) {
            Log.d(TAG, "Bluetooth Device Found "+device.getName()+" "+device.getAddress());
            if (bluetoothDeviceList.contains(device)) {
                return;
            }
            bluetoothDeviceList.add(device);
            bluetoothDeviceAdapter.notifyDataSetChanged();
        }

        @Override
        public void onDevicePaired(BluetoothDevice device) {
            Log.d(TAG, "Paired to "+device.getName());
        }

        @Override
        public void onDeviceUnpaired(BluetoothDevice device) {
            Log.d(TAG, "UnPaired from "+device.getName());
        }

        @Override
        public void onError(int errorCode) {
            Log.d(TAG, "Error "+errorCode);
        }
    };

    private DeviceCallBack deviceCallBack = new DeviceCallBack() {
        @Override
        public void onDeviceConnected(BluetoothDevice device) {
            progressDialog.setMessage("Connected");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    });
                }
            },800);

            dataBinding.layoutBluetooth.btnBtCtr.setText("Disconnect");
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, String message) {
            dataBinding.layoutBluetooth.btnBtCtr.setText("Search Device..");
        }

        @Override
        public void onMessage(byte[] message) {

        }

        @Override
        public void onError(int errorCode) {

        }

        @Override
        public void onConnectError(BluetoothDevice device, String message) {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        bluetooth.onStart();
        if (!bluetooth.isEnabled()) {
            bluetooth.showEnableDialog(DataActivity.this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        bluetooth.onStop();
    }
}