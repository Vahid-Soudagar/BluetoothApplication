package com.example.bluetoothapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.example.bluetoothapplication.bluetooth.Bluetooth;
import com.example.bluetoothapplication.callbacks.DeviceCallBack;
import com.example.bluetoothapplication.databinding.ActivityStartBinding;

public class StartActivity extends AppCompatActivity {

    private ActivityStartBinding binding;

    private Bluetooth bluetooth;
    private BluetoothDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        device = getIntent().getParcelableExtra("device");
        bluetooth = new Bluetooth(this);
        bluetooth.setCallbackOnUI(this);
        bluetooth.setDeviceCallback(deviceCallback);
        binding.activityChatHelloWorld.setEnabled(false);


        binding.activityChatHelloWorld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = "Hello World !";
                bluetooth.send(msg);
                appendToChat("-> "+msg);
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

    private void appendToChat(String msg){
        String text = binding.activityChatMessages.getText().toString()+"\n"+msg;
        binding.activityChatMessages.setText(text);
    }

    private DeviceCallBack deviceCallback = new DeviceCallBack() {
        @Override
        public void onDeviceConnected(BluetoothDevice device) {
            binding.activityChatStatus.setText("Connected !");
            binding.activityChatHelloWorld.setEnabled(true);
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, String message) {
            binding.activityChatStatus.setText("Device disconnected !");
            binding.activityChatHelloWorld.setEnabled(false);
        }

        @Override
        public void onMessage(byte[] message) {
            String str = new String(message);
            appendToChat("<- "+str);
        }

        @Override
        public void onError(int errorCode) {

        }
        @SuppressLint("SetTextI18n")
        @Override
        public void onConnectError(final BluetoothDevice device, String message) {
            binding.activityChatStatus.setText("Could not connect, next try in 3 sec...");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bluetooth.connectToDevice(device);
                }
            }, 3000);
        }
    };


}