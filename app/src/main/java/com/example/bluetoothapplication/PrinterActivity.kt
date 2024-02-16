package com.example.bluetoothapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.epson.epos2.printer.Printer
import com.example.bluetoothapplication.databinding.ActivityPrinterBinding


class PrinterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrinterBinding
    var mPrinter: Printer? = null
    private val REQUEST_PERMISSION = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrinterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestRuntimePermission()

        binding.btnDiscovery.setOnClickListener {
            val intent = Intent(this, DiscoveryActivity::class.java)
            startActivityForResult(intent, 0)
        }

    }


    private fun requestRuntimePermission() {
        val requestPermissions: MutableList<String> = ArrayList()
        if (Build.VERSION_CODES.S <= Build.VERSION.SDK_INT) {
            // If your app targets Android 12 (API level 31) and higher, it's recommended that you declare BLUETOOTH permission.
            val permissionBluetoothScan =
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            val permissionBluetoothConnect =
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            if (permissionBluetoothScan == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (permissionBluetoothConnect == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT) {
            // If your app targets Android 11 (API level 30) or lower, it's necessary that you declare ACCESS_FINE_LOCATION permission.
            val permissionLocationFine =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            if (permissionLocationFine == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        } else {
            // If your app targets Android 9 (API level 28) or lower, you can declare the ACCESS_COARSE_LOCATION permission instead of the ACCESS_FINE_LOCATION permission.
            val permissionLocationCoarse =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            if (permissionLocationCoarse == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
        if (requestPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                requestPermissions.toTypedArray<String>(),
                REQUEST_PERMISSION
            )
        }
    }

}