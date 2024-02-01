package com.example.bluetoothapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
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
    private DataParser dataParser;
    private static final String TAG = "myTag";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        binding.activityChatHelloWorld.setEnabled(false);
//        binding.activityChatHelloWorld.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String msg = "Hello World !";
//                bluetooth.send(msg);
//                appendToChat("-> "+msg);
//            }
//        });

        device = getIntent().getParcelableExtra("device");
        bluetooth = new Bluetooth(this);
        bluetooth.setCallbackOnUI(this);
        bluetooth.setDeviceCallback(deviceCallback);
        binding.layoutNibp.getRoot().setVisibility(View.GONE);
        dataParser = new DataParser(this);
        dataParser.start();

        binding.layoutNibp.btnNIBPStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(StartActivity.this, "Command Sended Waiting for response", Toast.LENGTH_LONG).show();
                bluetooth.send(DataParser.CMD_START_NIBP);
            }
        });

        binding.layoutNibp.btnNIBPStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(StartActivity.this, "Command Stopped", Toast.LENGTH_LONG).show();
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

//    private void appendToChat(String msg){
//        String text = binding.activityChatMessages.getText().toString()+"\n"+msg;
//        binding.activityChatMessages.setText(text);
//    }

    private DeviceCallBack deviceCallback = new DeviceCallBack() {
        @Override
        public void onDeviceConnected(BluetoothDevice device) {
//            binding.activityChatHelloWorld.setEnabled(true);
            binding.activityChatStatus.setText("Connected!");
            binding.layoutNibp.getRoot().setVisibility(View.VISIBLE);
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, String message) {
            binding.activityChatStatus.setText("Device disconnected !");
            binding.layoutNibp.getRoot().setVisibility(View.GONE);
        }

//        [1, -17, -65, -67, -17, -65, -67, -17, -65, -67, -17, -65, -67, -17, -65, -67, -17, -65, -67, -17, -65, -67, 116, 85, -17, -65, -67]
//        android:text="Cuff:--\nHigh:-- Low:-- Mean:--"
        @Override
        public void onMessage(byte[] message) {
//            NIBP nibp = new NIBP(message[0], message[1], message[2], message[3], message[4]);
            Log.d(TAG, "Receiveing response : "+ Arrays.toString(message));
            dataParser.add(message);
            binding.layoutNibp.tvNIBPinfo.setText(Arrays.toString(message));
        }

        @Override
        public void onError(int errorCode) {

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
        Log.d(TAG, "Received");
    }

    @Override
    public void onSpO2Received(SpO2 spo2) {
        Log.d(TAG, "Received");
    }

    @Override
    public void onECGWaveReceived(int dat) {
        Log.d(TAG, "Received");
    }

    @Override
    public void onECGReceived(ECG ecg) {
        Log.d(TAG, "Received");
    }

    @Override
    public void onTempReceived(Temp temp) {
        Log.d(TAG, "Received");

    }

    @Override
    public void onNIBPReceived(NIBP nibp) {
        Log.d(TAG, "Receiveing nibp : "+nibp.toString());
        binding.layoutNibp.tvNIBPinfo.setText(nibp.toString());
    }

    @Override
    public void onFirmwareReceived(String str) {
        Log.d(TAG, "Received");
    }

    @Override
    public void onHardwareReceived(String str) {
        Log.d(TAG, "Received");
    }
}