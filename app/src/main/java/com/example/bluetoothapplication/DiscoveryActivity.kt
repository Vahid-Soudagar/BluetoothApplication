package com.example.bluetoothapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity
import com.epson.epos2.Epos2Exception
import com.epson.epos2.discovery.Discovery
import com.epson.epos2.discovery.DiscoveryListener
import com.epson.epos2.discovery.FilterOption
import com.example.bluetoothapplication.databinding.ActivityDiscoveryBinding

class DiscoveryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiscoveryBinding
    private val TAG = "myTag"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiscoveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRestart.setOnClickListener {
            discoverDevices()
        }

    }

    @SuppressLint("MissingPermission")
    private fun discoverDevices() {
        val manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        val channel: WifiP2pManager.Channel = manager.initialize(this, mainLooper, null)

        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Discovery initiated successfully
                Log.d(TAG, "Discovery initiated successfully")

                // Request the current list of peers
                manager.requestPeers(channel) { peers ->
                    // Log all discovered devices
                    for (device in peers.deviceList) {
                        Log.d(TAG, "Device name: ${device.deviceName}, Address: ${device.deviceAddress}")
                        binding.txtList.text = device.deviceName
                        binding.txtList.setOnClickListener {
                            connectToDevice(device)
                        }
                    }
                }
            }

            override fun onFailure(reasonCode: Int) {
                // Log failure
                Log.e(TAG, "Discovery failed with reason code: $reasonCode")
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: WifiP2pDevice) {
        val config = WifiP2pConfig()
        config.deviceAddress = device.deviceAddress

        val manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        val channel: WifiP2pManager.Channel = manager.initialize(this, mainLooper, null)

        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Connection successful
                Log.d(TAG, "Connected to device: ${device.deviceName}")
                // You can perform any action here after successfully connecting to the device
            }

            override fun onFailure(reason: Int) {
                // Connection failed
                Log.e(TAG, "Failed to connect to device: ${device.deviceName}, Reason: $reason")
                // You can handle the failure case here
            }
        })
    }

}