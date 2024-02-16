package com.example.bluetoothapplication

import android.content.Intent
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

class DiscoveryActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    private lateinit var binding: ActivityDiscoveryBinding

    private var mPrinterList: ArrayList<HashMap<String, String>>? = null
    private var mPrinterListAdapter: SimpleAdapter? = null
    private var mFilterOption: FilterOption? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiscoveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mPrinterList = java.util.ArrayList()
        mPrinterListAdapter = SimpleAdapter(
            this,
            mPrinterList,
            R.layout.devices_dialog_bluetooth_item,
            arrayOf<String>("PrinterName", "Target"),
            intArrayOf(R.id.tvBtItemName, R.id.tvBtItemAddr)
        )

        binding.lstReceiveData.adapter = mPrinterListAdapter
        binding.lstReceiveData.onItemClickListener = this

        mFilterOption = FilterOption()
        mFilterOption!!.deviceType = Discovery.TYPE_PRINTER
        mFilterOption!!.epsonFilter = Discovery.FILTER_NAME
        mFilterOption!!.usbDeviceName = Discovery.TRUE

        try {
            Discovery.start(this, mFilterOption, mDiscoveryListener)
        } catch (e: Exception) {
            Log.d("myTag", e.toString())
        }

        binding.btnRestart.setOnClickListener {
            startDiscovery()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        while (true) {
            try {
                Discovery.stop()
                break
            } catch (e: Epos2Exception) {
                if (e.errorStatus != Epos2Exception.ERR_PROCESSING) {
                    break
                }
            }
        }
        mFilterOption = null
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val intent = Intent()

        val item = mPrinterList!![position]
        var target = item["Target"]
        if (target!!.startsWith("USB:")) {
            target = "USB:"
        }

        intent.putExtra(getString(R.string.title_target), target)

        setResult(RESULT_OK, intent)

        finish()
    }

    private fun startDiscovery() {
        while (true) {
            try {
                Discovery.stop()
                break
            } catch (e: Epos2Exception) {
                if (e.errorStatus != Epos2Exception.ERR_PROCESSING) {
                    Log.d("myTag", e.toString())
                    return
                }
            }
        }
        mPrinterList!!.clear()
        mPrinterListAdapter!!.notifyDataSetChanged()
        try {
            Discovery.start(this, mFilterOption, mDiscoveryListener)
        } catch (e: java.lang.Exception) {
            Log.d("myTag", e.toString())
        }
    }

    private val mDiscoveryListener =
        DiscoveryListener { deviceInfo ->
            runOnUiThread {
                val item = java.util.HashMap<String, String>()
                item["PrinterName"] = deviceInfo.deviceName
                item["Target"] = deviceInfo.target
                mPrinterList!!.add(item)
                mPrinterListAdapter!!.notifyDataSetChanged()
            }
        }
}