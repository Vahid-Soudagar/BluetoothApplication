package com.example.bluetoothapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.bluetoothapplication.bluetooth.Bluetooth;
import com.example.bluetoothapplication.callbacks.BluetoothCallBack;
import com.example.bluetoothapplication.callbacks.DiscoveryCallBack;
import com.example.bluetoothapplication.databinding.ActivityScanBinding;

import java.util.ArrayList;
import java.util.List;

public class ScanActivity extends AppCompatActivity {

    private ActivityScanBinding binding;
    private Bluetooth bluetooth;
    private ArrayAdapter<String> scanListAdapter;
    private ArrayAdapter<String> pairedListAdapter;
    private List<BluetoothDevice> pairedDevices;
    private List<BluetoothDevice> scannedDevices;

    private boolean scanning = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pairedListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        binding.activityScanPairedList.setAdapter(pairedListAdapter);
        binding.activityScanPairedList.setOnItemClickListener(onPairedListItemClick);

        scanListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        binding.activityScanList.setAdapter(scanListAdapter);
        binding.activityScanList.setOnItemClickListener(onScanListItemClick);


        bluetooth = new Bluetooth(this);
        bluetooth.setCallbackOnUI(this);
        bluetooth.setBluetoothCallback(bluetoothCallback);
        bluetooth.setDiscoveryCallback(discoveryCallback);


        setProgressAndState("", View.GONE);
        binding.activityScanButton.setEnabled(false);

        binding.activityScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetooth.startScanning();
            }
        });

    }

    private void setProgressAndState(String msg, int p){
        binding.activityScanState.setText(msg);
        binding.activityScanProgress.setVisibility(p);
    }

    @SuppressLint("MissingPermission")
    private void displayPairedDevices(){
        pairedDevices = bluetooth.getPairedDevices();
        for(BluetoothDevice device : pairedDevices){
            pairedListAdapter.add(device.getAddress()+" : "+device.getName());
        }
    }


    private AdapterView.OnItemClickListener onPairedListItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(scanning){
                bluetooth.stopScanning();
            }
            startChatActivity(pairedDevices.get(i));
        }
    };

    private AdapterView.OnItemClickListener onScanListItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(scanning){
                bluetooth.stopScanning();
            }
            setProgressAndState("Pairing...", View.VISIBLE);
            bluetooth.pair(scannedDevices.get(i));
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        bluetooth.onStart();
        if(bluetooth.isEnabled()){
            displayPairedDevices();
            binding.activityScanButton.setEnabled(true);
        } else {
            bluetooth.showEnableDialog(ScanActivity.this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        bluetooth.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        bluetooth.onActivityResult(requestCode, resultCode);
    }

    private BluetoothCallBack bluetoothCallback = new BluetoothCallBack() {
        @Override
        public void onBluetoothTurningOn() {
        }

        @Override
        public void onBluetoothOn() {
            displayPairedDevices();
            binding.activityScanButton.setEnabled(true);
        }

        @Override
        public void onBluetoothTurningOff() {
            binding.activityScanButton.setEnabled(false);
        }

        @Override
        public void onBluetoothOff() {
        }

        @Override
        public void onUserDeniedActivation() {
            Toast.makeText(ScanActivity.this, "I need to activate bluetooth...", Toast.LENGTH_SHORT).show();
        }
    };

    private DiscoveryCallBack discoveryCallback = new DiscoveryCallBack() {
        @Override
        public void onDiscoveryStarted() {
            setProgressAndState("Scanning...", View.VISIBLE);
            scannedDevices = new ArrayList<>();
            scanning = true;
        }

        @Override
        public void onDiscoveryFinished() {
            setProgressAndState("Done.", View.INVISIBLE);
            scanning = false;
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onDeviceFound(BluetoothDevice device) {
            scannedDevices.add(device);
            scanListAdapter.add(device.getAddress()+" : "+device.getName());
        }

        @Override
        public void onDevicePaired(BluetoothDevice device) {
            Toast.makeText(ScanActivity.this, "Paired !", Toast.LENGTH_SHORT).show();
            startChatActivity(device);
        }

        @Override
        public void onDeviceUnpaired(BluetoothDevice device) {

        }

        @Override
        public void onError(int errorCode) {

        }
    };

    private void startChatActivity(BluetoothDevice device){
        Intent intent = new Intent(this, StartActivity.class);
        intent.putExtra("device", device);
        startActivity(intent);
    }

}