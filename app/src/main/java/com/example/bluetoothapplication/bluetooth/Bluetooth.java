package com.example.bluetoothapplication.bluetooth;

import static com.example.bhfinal.utils.Constants.REQUEST_ENABLE_BT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.example.bhfinal.utils.Constants;
import com.example.bluetoothapplication.bluetooth.reader.LineReader;
import com.example.bluetoothapplication.bluetooth.reader.SocketReader;
import com.example.bluetoothapplication.callbacks.BluetoothCallBack;
import com.example.bluetoothapplication.callbacks.DeviceCallBack;
import com.example.bluetoothapplication.callbacks.DiscoveryCallBack;
import com.example.bluetoothapplication.utils.ThreadHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Bluetooth {

    private final String DEFAULT_UUID = "00001101-0000-1000-8000-00805f9b34fb";
    private final String TAG = this.getClass().getSimpleName();

    private Activity activity;
    private Context context;
    private UUID uuid;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    private DeviceCallBack deviceCallBack;
    private DiscoveryCallBack discoveryCallBack;
    private BluetoothCallBack bluetoothCallBack;

    private ReceiveThread receiveThread;
    private boolean connected;
    private boolean runOnUi;
    private String pin;

    private Class readerClass;

    public Bluetooth(Context context) {
        initialize(context, UUID.fromString(DEFAULT_UUID));
    }

    public Bluetooth(Context context, UUID uuid) {
        initialize(context, uuid);
    }

    public void initialize(Context context, UUID uuid) {
        this.context = context;
        this.uuid = uuid;
        this.readerClass = LineReader.class;
        this.deviceCallBack = null;
        this.discoveryCallBack = null;
        this.bluetoothCallBack = null;
        this.connected = false;
        this.runOnUi = false;
    }

    public void onStart() {
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        context.registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    public void onStop() {
        context.unregisterReceiver(bluetoothReceiver);
    }

    @SuppressLint("MissingPermission")
    public void showEnableDialog(Activity activity) {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(intent, REQUEST_ENABLE_BT);
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void enable() {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void disable(){
        if(bluetoothAdapter!=null) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
            }
        }
    }

    public BluetoothSocket getSocket(){
        return receiveThread.getSocket();
    }

    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public boolean isEnabled(){
        if(bluetoothAdapter!=null) {
            return bluetoothAdapter.isEnabled();
        }
        return false;
    }

    public void setCallbackOnUI(Activity activity){
        this.activity = activity;
        this.runOnUi = true;
    }

    public void setReader(Class readerClass){
        this.readerClass = readerClass;
    }

    public void onActivityResult(int requestCode, final int resultCode){
        if(bluetoothCallBack!=null){
            if(requestCode==REQUEST_ENABLE_BT){
                ThreadHelper.run(runOnUi, activity, new Runnable() {
                    @Override
                    public void run() {
                        if(resultCode==Activity.RESULT_CANCELED){
                            bluetoothCallBack.onUserDeniedActivation();
                        }
                    }
                });
            }
        }
    }

    public void connectToAddress(String address, boolean insecureConnection, boolean withPortTrick) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        connectToDevice(device, insecureConnection, withPortTrick);
    }

    public void connectToAddress(String address) {
        connectToAddress(address, false, false);
    }

    public void connectToAddressWithPortTrick(String address) {
        connectToAddress(address, false, true);
    }

    @SuppressLint("MissingPermission")
    public void connectToName(String name, boolean insecureConnection, boolean withPortTrick) {
        for (BluetoothDevice blueDevice : bluetoothAdapter.getBondedDevices()) {
            if (blueDevice.getName().equals(name)) {
                connectToDevice(blueDevice, insecureConnection, withPortTrick);
                return;
            }
        }
    }

    public void connectToName(String name) {
        connectToName(name, false, false);
    }

    public void connectToNameWithPortTrick(String name) {
        connectToName(name, false, true);
    }

    @SuppressLint("MissingPermission")
    public void connectToDevice(final BluetoothDevice device, boolean insecureConnection, boolean withPortTrick){
        if(bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        connect(device, insecureConnection, withPortTrick);
    }

    public void connectToDevice(BluetoothDevice device){
        connectToDevice(device, false, false);
    }


    public void connectToDeviceWithPortTrick(BluetoothDevice device){
        connectToDevice(device, false, false);
    }

    public void disconnect() {
        try {
            receiveThread.getSocket().close();
        } catch (final IOException e) {
            if(deviceCallBack !=null) {
                ThreadHelper.run(runOnUi, activity, new Runnable() {
                    @Override
                    public void run() {
                        Log.w(getClass().getSimpleName(), e.getMessage());
                        deviceCallBack.onError(Constants.FAILED_WHILE_CLOSING);
                    }
                });
            }
        }
    }

    public boolean isConnected(){
        return connected;
    }

    public void send(byte[] data){
        OutputStream out = receiveThread.getOutputStream();
        try {
            out.write(data);
        } catch (final IOException e) {
            connected = false;
            if(deviceCallBack !=null){
                ThreadHelper.run(runOnUi, activity, new Runnable() {
                    @Override
                    public void run() {
                        deviceCallBack.onDeviceDisconnected(receiveThread.getDevice(), e.getMessage());
                    }
                });
            }
        }
    }

    public void send(String msg, Charset charset){
        if(charset==null){
            send(msg.getBytes());
        } else{
            send(msg.getBytes(charset));
        }
    }

    public void send(String msg){
        send(msg, null);
    }

    @SuppressLint("MissingPermission")
    public List<BluetoothDevice> getPairedDevices(){
        return new ArrayList<>(bluetoothAdapter.getBondedDevices());
    }

    @SuppressLint("MissingPermission")
    public void startScanning(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);

        context.registerReceiver(scanReceiver, filter);
        bluetoothAdapter.startDiscovery();
    }

    @SuppressLint("MissingPermission")
    public void stopScanning(){
        context.unregisterReceiver(scanReceiver);
        bluetoothAdapter.cancelDiscovery();
    }

    public void pair(BluetoothDevice device, String pin){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        if(pin != null) {
            this.pin = pin;
            filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        }

        context.registerReceiver(pairReceiver, filter);
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (final Exception e) {
            if(discoveryCallBack!=null) {
                ThreadHelper.run(runOnUi, activity, new Runnable() {
                    @Override
                    public void run() {
                        Log.w(getClass().getSimpleName(), e.getMessage());
                        discoveryCallBack.onError(Constants.FAILED_TO_PAIR);
                    }
                });
            }
        }
    }

    public void pair(BluetoothDevice device){
        pair(device, null);
    }

    public void unpair(BluetoothDevice device) {
        context.registerReceiver(pairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (final Exception e) {
            if(discoveryCallBack!=null) {
                ThreadHelper.run(runOnUi, activity, new Runnable() {
                    @Override
                    public void run() {
                        Log.w(getClass().getSimpleName(), e.getMessage());
                        discoveryCallBack.onError(Constants.FAILED_TO_UNPAIR);
                    }
                });
            }
        }
    }

    @SuppressLint("MissingPermission")
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device, boolean insecureConnection){
        BluetoothSocket socket = null;
        try {
            if(insecureConnection){
                socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            }
            else{
                socket = device.createRfcommSocketToServiceRecord(uuid);
            }
        } catch (IOException e) {
            if(deviceCallBack !=null){
                Log.w(getClass().getSimpleName(), Objects.requireNonNull(e.getMessage()));
                deviceCallBack.onError(Constants.FAILED_WHILE_CREATING_SOCKET);
            }
        }
        return socket;
    }

    private BluetoothSocket createBluetoothSocketWithPortTrick(BluetoothDevice device){
        BluetoothSocket socket = null;
        try {
            socket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(getClass().getSimpleName(), e.getMessage());
        }
        return socket;
    }

    @SuppressLint("MissingPermission")
    private void connect(BluetoothDevice device, boolean insecureConnection, boolean withPortTrick){
        BluetoothSocket socket = null;
        if(withPortTrick){
            socket = createBluetoothSocketWithPortTrick(device);
        }
        if(socket==null){
            try {
                if(insecureConnection){
                    socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                }
                else{
                    socket = device.createRfcommSocketToServiceRecord(uuid);
                }
            } catch (IOException e) {
                if(deviceCallBack !=null){
                    Log.w(getClass().getSimpleName(), e.getMessage());
                    deviceCallBack.onError(Constants.FAILED_WHILE_CREATING_SOCKET);
                }
            }
        }
        connectInThread(socket, device);
    }

    private void connectInThread(final BluetoothSocket socket, final BluetoothDevice device){
        new Thread(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                try {
                    socket.connect();
                    connected = true;
                    receiveThread = new ReceiveThread(readerClass, socket, device);
                    if(deviceCallBack !=null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                deviceCallBack.onDeviceConnected(device);
                            }
                        });
                    }
                    receiveThread.start();
                } catch (final IOException e) {
                    if(deviceCallBack !=null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                deviceCallBack.onConnectError(device, e.getMessage());
                            }
                        });
                    }
                }
            }
        }).start();
    }

    private class ReceiveThread extends Thread implements Runnable {

        private SocketReader reader;
        private BluetoothSocket socket;
        private BluetoothDevice device;
        private OutputStream out;
        private InputStream mmInputStream;

        public ReceiveThread(Class<?> readerClass, BluetoothSocket socket, BluetoothDevice bluetoothDevice) {
            this.socket = socket;
            this.device = bluetoothDevice;
            InputStream tmpIn = null;
            try {
                out = socket.getOutputStream();
                InputStream in = socket.getInputStream();
                this.reader = (SocketReader) readerClass.getDeclaredConstructor(InputStream.class).newInstance(in);
                tmpIn = socket.getInputStream();
            } catch (IOException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                Log.w(getClass().getSimpleName(), e.getMessage());
            }

            mmInputStream = tmpIn;
        }


        @Override
        public void run() {
            byte[] msg;
            try {
                while((msg = reader.read()) != null) {
                    if(deviceCallBack != null){
                        byte[] msgCopy = msg;
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "Message Getting from Machine "+ Arrays.toString(msgCopy));
                                deviceCallBack.onMessage(msgCopy);
                            }
                        });
                    }
                }
            } catch (final IOException e) {
                connected = false;
                if(deviceCallBack != null){
                    ThreadHelper.run(runOnUi, activity, new Runnable() {
                        @Override
                        public void run() {
                            deviceCallBack.onDeviceDisconnected(device, e.getMessage());
                        }
                    });
                }
            }
        }

//        @Override
//        public void run() {
//            int bytes;
//            while (isConnected()) {
//                try {
//                    byte[] buffer = new byte[256];
//                    bytes = mmInputStream.read(buffer);
//                    if (bytes > 0) {
//                        b yte[] data = new byte[bytes];
//                        System.arraycopy(buffer, 0, data, 0, bytes);
//                        if (deviceCallBack != null){
//                            ThreadHelper.run(runOnUi, activity, new Runnable() {
//                                @Override
//                                public void run() {
//                                    Log.d(TAG, "Message Getting from Machine and sending it to activity "+ Arrays.toString(data));
//                                    deviceCallBack.onMessage(data);
//                                }
//                            });
//                        }
//                    }
//                } catch (IOException e) {
//                    connected = false;
//                    if(deviceCallBack != null){
//                        ThreadHelper.run(runOnUi, activity, new Runnable() {
//                            @Override
//                            public void run() {
//                                deviceCallBack.onDeviceDisconnected(device, e.getMessage());
//                            }
//                        });
//                    }
//                }
//            }
//        }

        public BluetoothSocket getSocket() {
            return socket;
        }

        public BluetoothDevice getDevice() {
            return device;
        }

        public OutputStream getOutputStream() {
            return out;
        }
    }

    private BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action!=null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                        if (state == BluetoothAdapter.STATE_OFF) {
                            if (discoveryCallBack != null) {
                                ThreadHelper.run(runOnUi, activity, new Runnable() {
                                    @Override
                                    public void run() {
                                        discoveryCallBack.onError(Constants.BLUETOOTH_DISABLED);
                                    }
                                });
                            }
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        if (discoveryCallBack != null){
                            ThreadHelper.run(runOnUi, activity, new Runnable() {
                                @Override
                                public void run() {
                                    discoveryCallBack.onDiscoveryStarted();
                                }
                            });
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        context.unregisterReceiver(scanReceiver);
                        if (discoveryCallBack != null){
                            ThreadHelper.run(runOnUi, activity, new Runnable() {
                                @Override
                                public void run() {
                                    discoveryCallBack.onDiscoveryFinished();
                                }
                            });
                        }
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (discoveryCallBack != null){
                            ThreadHelper.run(runOnUi, activity, new Runnable() {
                                @Override
                                public void run() {
                                    discoveryCallBack.onDeviceFound(device);
                                }
                            });
                        }
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver pairReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    context.unregisterReceiver(pairReceiver);
                    if (discoveryCallBack != null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                discoveryCallBack.onDevicePaired(device);
                            }
                        });
                    }
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    context.unregisterReceiver(pairReceiver);
                    if (discoveryCallBack != null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                discoveryCallBack.onDeviceUnpaired(device);
                            }
                        });
                    }
                }
            }
            else if(BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)){
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                device.setPin(pin.getBytes());
                try {
                    device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    if (discoveryCallBack != null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                discoveryCallBack.onError(-1);
                            }
                        });
                    }
                }
            }
        }
    };

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action!=null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if(bluetoothCallBack!=null) {
                    ThreadHelper.run(runOnUi, activity, new Runnable() {
                        @Override
                        public void run() {
                            switch (state) {
                                case BluetoothAdapter.STATE_OFF:
                                    bluetoothCallBack.onBluetoothOff();
                                    break;
                                case BluetoothAdapter.STATE_TURNING_OFF:
                                    bluetoothCallBack.onBluetoothTurningOff();
                                    break;
                                case BluetoothAdapter.STATE_ON:
                                    bluetoothCallBack.onBluetoothOn();
                                    break;
                                case BluetoothAdapter.STATE_TURNING_ON:
                                    bluetoothCallBack.onBluetoothTurningOn();
                                    break;
                            }
                        }
                    });
                }
            }
        }
    };

    public void setDeviceCallback(DeviceCallBack deviceCallback) {
        this.deviceCallBack = deviceCallback;
    }

    /**
     * Remove device callback. No updates will be received anymore.
     */
    public void removeDeviceCallback(){
        this.deviceCallBack = null;
    }

    /**
     * Callback to receive scanning related updates.
     * @param discoveryCallback Non-null callback.
     */
    public void setDiscoveryCallback(DiscoveryCallBack discoveryCallback){
        this.discoveryCallBack = discoveryCallback;
    }

    /**
     * Remove discovery callback. No updates will be received anymore.
     */
    public void removeDiscoveryCallback(){
        this.discoveryCallBack = null;
    }

    /**
     * Callback to receive bluetooth status related updates.
     * @param bluetoothCallback Non-null callback.
     */
    public void setBluetoothCallback(BluetoothCallBack bluetoothCallback){
        this.bluetoothCallBack = bluetoothCallback;
    }

    /**
     * Remove bluetooth callback. No updates will be received anymore.
     */
    public void removeBluetoothCallback(){
        this.bluetoothCallBack = null;
    }

}
