package com.learntocode.decodebtprinter

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import java.lang.Exception


class DeviceListActivity : Activity() {
    //TODO 3
    lateinit var mBluetoothAdapter: BluetoothAdapter
    private var mPairedDevicesArrayAdapter: ArrayAdapter<String>? = null
    override fun onCreate(mSavedInstanceState: Bundle?) {
        super.onCreate(mSavedInstanceState)
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)//ini semacam progress bar yg muter2, bisa dirubah sama lebih update
        setContentView(R.layout.activity_device_list)
        setResult(RESULT_CANCELED)
        mPairedDevicesArrayAdapter = ArrayAdapter(this, R.layout.device_name)
        val mPairedListView = findViewById<ListView>(R.id.paired_devices)
        mPairedListView.adapter = mPairedDevicesArrayAdapter
        mPairedListView.onItemClickListener = mDeviceClickListener

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val mPairedDevices = mBluetoothAdapter.bondedDevices

        if (mPairedDevices.size > 0) {
            /* List of all paired devices */
            findViewById<View>(R.id.title_paired_devices).visibility = View.VISIBLE
            for (mDevice in mPairedDevices) {
                mPairedDevicesArrayAdapter!!.add(
                    """
                        ${mDevice.name}
                        ${mDevice.address}
                        """.trimIndent()
                )
            }
        } else {
            /* No paired device */
            val mNoDevices = "None Paired"
            mPairedDevicesArrayAdapter!!.add(mNoDevices)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        /* Terminate bluetooth connection and close all sockets opened */if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery()
        }
    }

    private val mDeviceClickListener =
        OnItemClickListener { mAdapterView, mView, mPosition, mLong ->
            try {
                /* Attempt to connect to bluetooth device */
                mBluetoothAdapter.cancelDiscovery()
                val mDeviceInfo = (mView as TextView).text.toString()
                val mDeviceAddress = mDeviceInfo.substring(mDeviceInfo.length - 17)
                Log.v(TAG,"Device_Address $mDeviceAddress")
                val mBundle = Bundle()
                mBundle.putString("DeviceAddress", mDeviceAddress)
                val mBackIntent = Intent()
                mBackIntent.putExtras(mBundle)
                setResult(RESULT_OK, mBackIntent)
                finish()
            } catch (ex: Exception) {
                Log.v(TAG, "Error: $ex")
            }
        }

    companion object {
        protected const val TAG = "DeviceListActivity"
    }
}
