package com.example.bluetoothapplication.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.bluetoothapplication.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ZXX on 2015/12/30.
 */
public class BluetoothDeviceAdapter extends BaseAdapter
{

    private LayoutInflater mInflater;
    private List<BluetoothDevice> mDevices;
    private HashMap<String,Integer> mRssiMap;

    public BluetoothDeviceAdapter(Context context, List<BluetoothDevice> devices)
    {
        this.mInflater = LayoutInflater.from(context);
        this.mDevices  = devices;
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("MissingPermission")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        BluetoothDevice dev = mDevices.get(position);

        LinearLayout llItem = null;
        if(convertView != null)
        {
            llItem = (LinearLayout) convertView;
        }
        else
        {
            llItem  = (LinearLayout) mInflater.inflate(R.layout.devices_dialog_bluetooth_item,null);
        }

        TextView tvName = (TextView) llItem.findViewById(R.id.tvBtItemName);
        TextView tvAddr = (TextView) llItem.findViewById(R.id.tvBtItemAddr);
        tvName.setText(dev.getName());
        tvAddr.setText("MAC: "+dev.getAddress());

        return llItem;
    }
}
