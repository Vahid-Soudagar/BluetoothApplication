package com.example.bluetoothapplication.data;


import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class DataParser {

    //Const
    public String TAG = "myTag";

    public String PARSE_CLASS = "parseClass";
    public String IRR_TAG = "irreleventData";

    //Buffer queue
    private LinkedBlockingQueue<Integer> bufferQueue   = new LinkedBlockingQueue<Integer>(1010);
    private int[]         PACKAGE_HEAD        = new int[]{0x55,0xaa};
    private final int     PKG_ECG_WAVE        = 0x01;
    private final int     PKG_ECG_PARAMS      = 0x02;
    private final int     PKG_NIBP            = 0x03;
    private final int     PKG_SPO2_PARAMS     = 0x04;
    private final int     PKG_TEMP            = 0x05;
    private final int     PKG_SW_VER          = 0xfc;
    private final int     PKG_HW_VER          = 0xfd;
    private final int     PKG_SPO2_WAVE       = 0xfe;
    public static byte[]  CMD_START_NIBP = new byte[]{0x55, (byte) 0xaa, 0x04, 0x02, 0x01, (byte) 0xf8};
    public static byte[]  CMD_STOP_NIBP  = new byte[]{0x55, (byte) 0xaa, 0x04, 0x02, 0x00, (byte) 0xf9};
    public static byte[]  CMD_FW_VERSION = new byte[]{0x55, (byte) 0xaa, 0x04, (byte) 0xfc, 0x00, (byte) 0xff};
    public static byte[]  CMD_HW_VERSION = new byte[]{0x55, (byte) 0xaa, 0x04, (byte) 0xfd, 0x00, (byte) 0xfe};

    //Parse Runnable
    private ParseRunnable mParseRunnable;
    private boolean isStop = true;
    private onPackageReceivedListener mListener;

    public interface onPackageReceivedListener {
        void onSpO2WaveReceived(int dat);
        void onSpO2Received(SpO2 spo2);

        void onECGWaveReceived(int dat);
        void onECGReceived(ECG ecg);

        void onTempReceived(Temp temp);

        void onNIBPReceived(NIBP nibp);

        void onFirmwareReceived(String str);
        void onHardwareReceived(String str);
    }

    public DataParser(onPackageReceivedListener listener) {
        Log.d(TAG, "Inside Data Parser Construction");
        this.mListener = listener;
    }

    public void start() {
        Log.d(TAG, "Inside Data Parser Start Method");
        mParseRunnable = new ParseRunnable();
//        operation will perform in seperate thread
        new Thread(mParseRunnable).start();
    }

    public void stop() {
        isStop = true;
    }


    class ParseRunnable implements Runnable {
        int dat;
        int[] packageData;
        @Override
        public void run() {
            while (isStop) {
                dat = getData();
                if(dat == PACKAGE_HEAD[0]){
                    dat = getData();
                    if(dat == PACKAGE_HEAD[1]) {
                        int packageLen = getData();
                        packageData = new int[packageLen + PACKAGE_HEAD.length];

                        packageData[0] = PACKAGE_HEAD[0];
                        packageData[1] = PACKAGE_HEAD[1];
                        packageData[2] = packageLen;

                        for (int i = 3; i < packageLen + PACKAGE_HEAD.length; i++) {
                            packageData[i] = getData();
                        }

                        if(CheckSum(packageData)){
                            ParsePackage(packageData);
                        }
                    }
                }
            }
        }
    }

    private void ParsePackage(int[] pkgData) {
        int pkgType = pkgData[3];
        Log.d(TAG, "Pkg Data: "+ Arrays.toString(pkgData));
        int[] tempBuffer = new int[5];

        switch (pkgType) {
            case PKG_ECG_WAVE:
                mListener.onECGWaveReceived(pkgData[4]);
                break;
            case PKG_SPO2_WAVE:
                mListener.onSpO2WaveReceived(pkgData[4]);
                break;
            case PKG_ECG_PARAMS:
                int heartRate = pkgData[5];
                ECG params = new ECG(heartRate,pkgData[6],pkgData[4]);
                mListener.onECGReceived(params);
                break;
            case PKG_NIBP:
                Log.d(TAG, "pkg_nibp");
                NIBP params2 = new NIBP(pkgData[6],pkgData[7],pkgData[8],pkgData[5]*2,pkgData[4]);
                Log.d(TAG, "pkg_nibp: "+ params2);
                mListener.onNIBPReceived(params2);
                break;
            case PKG_SPO2_PARAMS:
                SpO2 params3 = new SpO2(pkgData[5],pkgData[6],pkgData[4]);
                mListener.onSpO2Received(params3);
                break;
            case PKG_TEMP:
                Temp params4 = new Temp((pkgData[5]*10 + pkgData[6])/10.0,pkgData[4]);
                mListener.onTempReceived(params4);
                break;
            case PKG_SW_VER:
                StringBuilder sb = new StringBuilder();
                for (int i = 4; i < pkgData.length-1; i++){
                    sb.append((char)(pkgData[i]&0xff));
                }
                mListener.onFirmwareReceived(sb.toString());
                break;
            case PKG_HW_VER:
                StringBuilder sb1 = new StringBuilder();
                for (int i = 4; i < pkgData.length-1; i++){
                    sb1.append((char)(pkgData[i]&0xff));
                }
                mListener.onHardwareReceived(sb1.toString());
                break;
            default:
                Log.d(IRR_TAG, "Data irrelevant");
                break;
        }

    }

    public void add(byte[] dat) {
        Log.d(PARSE_CLASS, "Inside Add Method "+Arrays.toString(dat));
        for(byte b : dat)
        {
            try {
                bufferQueue.put(toUnsignedInt(b));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(PARSE_CLASS, "After Add Method "+bufferQueue.toString());
    }


    private int getData() {
        int dat = 0;
        try {
            Log.d(PARSE_CLASS, "Inside Get Data "+bufferQueue.take());
            dat = bufferQueue.take();
        } catch (InterruptedException e) {
            Log.d(TAG, String.valueOf(e));
        }
        Log.d(PARSE_CLASS, "Exiting Get Data "+dat);
        return dat;
    }
    private boolean CheckSum(int[] packageData) {
        int sum = 0;
        for(int i = 2; i < packageData.length-1; i++) {
            sum+=(packageData[i]);
        }

        if(((~sum)&0xff) == (packageData[packageData.length-1]&0xff)) {
            return true;
        }

        return false;
    }


    private int toUnsignedInt(byte x) {
        return ((int) x) & 0xff;
    }
}
