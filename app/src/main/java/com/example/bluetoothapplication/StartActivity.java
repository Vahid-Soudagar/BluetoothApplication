package com.example.bluetoothapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.bluetoothapplication.bluetooth.Bluetooth;
import com.example.bluetoothapplication.callbacks.DeviceCallBack;
import com.example.bluetoothapplication.data.DataParser;
import com.example.bluetoothapplication.data.ECG;
import com.example.bluetoothapplication.data.NIBP;
import com.example.bluetoothapplication.data.SpO2;
import com.example.bluetoothapplication.data.Temp;
import com.example.bluetoothapplication.databinding.ActivityStartBinding;

import java.util.Arrays;

public class StartActivity extends AppCompatActivity implements DataParser.onPackageReceivedListener {

    private ActivityStartBinding binding;

    private Bluetooth bluetooth;
    private BluetoothDevice device;
    private static final String TAG = "myTag";
    private final String MY_TAG = "debugTag";
    private static final String RESPONSE_TAG = "responseTag";
    private static final String ERROR_TAG = "errorTag";
    DataParser dataParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d(TAG, "Log in sarting of create method");

        device = getIntent().getParcelableExtra("device");

        bluetooth = new Bluetooth(this);
        bluetooth.setCallbackOnUI(this);
        bluetooth.setDeviceCallback(deviceCallback);

        binding.layoutNibp.getRoot().setVisibility(View.GONE);
        binding.layoutTemp.getRoot().setVisibility(View.GONE);
        binding.layoutAbout.getRoot().setVisibility(View.GONE);
        binding.layoutEcg.getRoot().setVisibility(View.GONE);
        binding.layoutSpo2.getRoot().setVisibility(View.GONE);

//        initData();
//        this should noe be null
        Log.d(TAG, "Created Data PArse object");
        dataParser = new DataParser(this);
        Log.d(TAG, "Data parse initiate start");
        dataParser.start();

        binding.layoutNibp.btnNIBPStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetooth.send(DataParser.CMD_START_NIBP);
            }
        });

        binding.layoutNibp.btnNIBPStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetooth.send(DataParser.CMD_STOP_NIBP);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetooth.onStart();
        bluetooth.connectToDevice(device);
        binding.activityChatStatus.setText("Connecting...");
    }

    @Override
    protected void onStop() {
        super.onStop();
        bluetooth.disconnect();
        bluetooth.onStop();
    }




    private DeviceCallBack deviceCallback = new DeviceCallBack() {
        @Override
        public void onDeviceConnected(BluetoothDevice device) {
            binding.activityChatStatus.setText("Connected!");
            binding.layoutNibp.getRoot().setVisibility(View.VISIBLE);
            binding.layoutEcg.getRoot().setVisibility(View.VISIBLE);
            binding.layoutAbout.getRoot().setVisibility(View.VISIBLE);
            binding.layoutSpo2.getRoot().setVisibility(View.VISIBLE);
            binding.layoutTemp.getRoot().setVisibility(View.VISIBLE);
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, String message) {
            binding.activityChatStatus.setText("Device disconnected !");
            binding.layoutNibp.getRoot().setVisibility(View.GONE);
            binding.layoutTemp.getRoot().setVisibility(View.GONE);
            binding.layoutAbout.getRoot().setVisibility(View.GONE);
            binding.layoutEcg.getRoot().setVisibility(View.GONE);
            binding.layoutSpo2.getRoot().setVisibility(View.GONE);        }


        @Override
        public void onMessage(byte[] message) {
            Log.d(MY_TAG, "Message received from Bluetooth");
            Log.d(RESPONSE_TAG, Arrays.toString(message));
            Log.d(MY_TAG, "Message sending to data parser activity");
            dataParser.add(message);
        }

        @Override
        public void onError(int errorCode) {
            Log.d(ERROR_TAG, errorCode+" ");
        }
        @SuppressLint("SetTextI18n")
        @Override
        public void onConnectError(final BluetoothDevice device, String message) {
            if (!bluetooth.isConnected()) {
                binding.activityChatStatus.setText("Could not connect, next try in 3 sec...");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bluetooth.connectToDevice(device);
                    }
                }, 3000);
            }
        }
    };

    @Override
    public void onSpO2WaveReceived(int dat) {
        Log.d(TAG, "onSpO2WaveReceived "+dat);
        binding.layoutSpo2.wfSpO2.addAmp(dat);
    }

    @Override
    public void onSpO2Received(SpO2 spo2) {
        Log.d(TAG, "onSpO2Received "+spo2.toString());
        binding.layoutSpo2.tvSPO2info.setText(spo2.toString());
    }

    @Override
    public void onECGWaveReceived(int dat) {
        Log.d(TAG, "onECGWaveReceived "+dat);
        binding.layoutEcg.wfECG.addAmp(dat);
    }
    @Override
    public void onECGReceived(ECG ecg) {
        Log.d(TAG, "onECGReceived "+ecg.toString());
        binding.layoutEcg.tvECGinfo.setText(ecg.toString());
    }

    @Override
    public void onTempReceived(Temp temp) {
        Log.d(TAG, "onTempReceived "+temp.toString());
        binding.layoutTemp.tvTEMPinfo.setText(temp.toString());
    }
    @Override
    public void onNIBPReceived(NIBP nibp) {
        Log.d(TAG, "onNIBPReceived "+nibp.toString());
        binding.layoutNibp.tvNIBPinfo.setText(nibp.toString());
    }

//    bluetooth -> activity -> parsing -> ui set

    @Override
    public void onFirmwareReceived(String str) {
        Log.d(TAG, "onHardwareReceived "+str);
        binding.layoutAbout.tvFWverison.setText(str);
    }

    @Override
    public void onHardwareReceived(String str) {
        Log.d(TAG, "onHardwareReceived "+str);
        binding.layoutAbout.tvHWverison.setText(str);
    }
}