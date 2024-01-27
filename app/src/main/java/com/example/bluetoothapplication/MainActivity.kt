package com.example.bluetoothapplication

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bhfinal.utils.Constants
import com.example.bhfinal.utils.Permission.appSettingOpen
import com.example.bhfinal.utils.Permission.warningPermissionDialog

class MainActivity : AppCompatActivity() {

        private val multiPermissionList = if (Build.VERSION.SDK_INT >= 33) {
            arrayListOf(
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_ADVERTISE,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            arrayListOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)


            if (checkMultiplePermission()) {
                doOperation() // Make sure this method completes its tasks before proceeding
            }

        }



        private fun doOperation() {
            Toast.makeText(this, "All Permission Granted", Toast.LENGTH_LONG).show()
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }

        private fun checkMultiplePermission(): Boolean {
            // List to store permissions that need to be requested
            val listPermissionNeeded = arrayListOf<String>()

            // Check each permission in the list
            for (permission in multiPermissionList) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    listPermissionNeeded.add(permission)
                }
            }

            // Request permissions if needed
            if (listPermissionNeeded.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionNeeded.toTypedArray(),
                    Constants.MULTIPLE_PERMISSION_ID
                )
                return false
            }

            // Return true if all permissions are already granted
            return true
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray,
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)

            // Handle the result of multiple permissions request
            if (requestCode == Constants.MULTIPLE_PERMISSION_ID) {
                if (grantResults.isNotEmpty()) {
                    var isGrant = true

                    // Check if all permissions are granted
                    for (element in grantResults) {
                        if (element == PackageManager.PERMISSION_DENIED) {
                            isGrant = false
                        }
                    }

                    // Perform the operation if all permissions are granted
                    if (isGrant) {
                        doOperation()
                    } else {
                        var someDenied = false

                        // Check if some permissions were permanently denied
                        for (permission in permissions) {
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                                if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                                    someDenied = true
                                }
                            }
                        }

                        // Show a toast and open app settings if some permissions were permanently denied
                        if (someDenied) {
                            Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
                            appSettingOpen(this)
                        } else {
                            // Show a warning dialog and recheck permissions if denied
                            warningPermissionDialog(this) { _: DialogInterface, which: Int ->
                                when (which) {
                                    DialogInterface.BUTTON_POSITIVE -> checkMultiplePermission()
                                }
                            }
                        }
                    }
                }
            }
        }

}