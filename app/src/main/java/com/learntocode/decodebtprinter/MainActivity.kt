package com.learntocode.decodebtprinter

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.lang.Exception
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : Activity(), Runnable {
    //var mScan: Button? = null
    lateinit var mScan: Button

//    var mPrint: Button? = null
    lateinit var mPrint: Button
    var mBluetoothAdapter: BluetoothAdapter? = null

    private val applicationUUID = UUID
        .fromString("00001101-0000-1000-8000-00805F9B34FB")
    //Hint: If you are connecting to a Bluetooth serial board then try using the well-known
    // SPP UUID 00001101-0000-1000-8000-00805F9B34FB.
    //However if you are connecting to an Android peer then please generate your own unique UUID.
    private var mBluetoothConnectProgressDialog: ProgressDialog? = null
    lateinit var mBluetoothSocket: BluetoothSocket
    lateinit var mBluetoothDevice: BluetoothDevice
    //var mBluetoothDevice: BluetoothDevice? = null
    //var stat: TextView? = null
    lateinit var stat: TextView
    var printstat = 0
    var layout: LinearLayout? = null
    var fullName: EditText? = null
    var companyName: EditText? = null
    var age: EditText? = null
    var agent_detail: EditText? = null

    /* Get time and date */
    var c = Calendar.getInstance()
    var df = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US)
    val formattedDate = df.format(c.time)

    lateinit var mLayout :View

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        stat = findViewById(R.id.bpstatus)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) //ini gunanya agar keyboard tidak muncul
        layout = findViewById(R.id.layout)
        fullName = findViewById(R.id.edit_full_name)
        companyName = findViewById(R.id.edit_company_name)
        age = findViewById(R.id.edit_age)
        agent_detail = findViewById(R.id.et_agent_details)
        mScan = findViewById(R.id.Scan)
        testing()
        //cekBtPermission()
        cekForBTConPermision()
        //cekAdminBTPermission()
        cekScanBTPermission()

//Todo 1
        mScan.setOnClickListener {
            if ((mScan.text == "Connect")) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                if (mBluetoothAdapter == null) {
                    Toast.makeText(this@MainActivity, "Device doestn support bluetooth", Toast.LENGTH_SHORT).show()
                } else {
                    if (!mBluetoothAdapter!!.isEnabled) {
                        val enableBtIntent = Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE
                        )
                        startActivityForResult(
                            enableBtIntent,
                            REQUEST_ENABLE_BT
                        )
                    } else {
                        //TODO 2
                        ListPairedDevices()
                        val connectIntent = Intent(
                            this@MainActivity,
                            //TODO 3
                            DeviceListActivity::class.java
                        )
                        startActivityForResult(
                            connectIntent,
                            REQUEST_CONNECT_DEVICE
                        )
                    }
                }
            } else if ((mScan.text == "Disconnect")) {
                val stat = findViewById<TextView>(R.id.bpstatus)
                val mScan = findViewById<Button>(R.id.Scan)
                if (mBluetoothAdapter != null) mBluetoothAdapter!!.disable()
                stat.text = ""
                stat.text = "Disconnected"
                stat.setTextColor(Color.rgb(199, 59, 59))
                mPrint.isEnabled = false
                mScan.isEnabled = true
                mScan.text = "Connect"
            }
        }
        mPrint = findViewById(R.id.mPrint)
        // TODO 4

        mPrint.setOnClickListener {
            //TODO 5
            p1()

            /* 5000 ms (5 Seconds) */
            val TIME = 10000
            Handler().postDelayed({ /* print second copy */

                //TODO 6
                p2()
                printstat = 1
            }, TIME.toLong()) // cara lain memasukan angka untuk delaymilis
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun testing(){
        //int result = context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        //return result == PackageManager.PERMISSION_GRANTED

        val hasilCheckBt = checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH)
        val hasilCheckBtAdmin= checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH_ADMIN)
        val hasilCheckBtCon = checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
        val hasilCheckBtScan = checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH_SCAN)


        val statusIzinOk = PackageManager.PERMISSION_GRANTED
        val statusIzinNo = PackageManager.PERMISSION_DENIED

        //println("uraaa "+statusIzinOk)
        //println("uraaa "+statusIzinNo)

        println("BtConn "+hasilCheckBtCon)
        println("Bt doang "+hasilCheckBt)
        println("Bt Scan "+hasilCheckBtScan)
        println("Bt Admin "+hasilCheckBtAdmin)

    }

    private fun cekBtPermission() {
        mLayout= findViewById(R.id.rootLayout)
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH)
            ==PackageManager.PERMISSION_GRANTED){
            Snackbar.make(mLayout,"Sudah diberikan izin akses Bluetooth only", Snackbar.LENGTH_LONG).show()
        }
        else{
            Snackbar.make(mLayout,"Belum diberikan izin akses Bluetooh Only",Snackbar.LENGTH_LONG).show()
            requestBluetoothPermission()
        }
    }

    private fun requestBluetoothPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.BLUETOOTH

            )){
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf((Manifest.permission.BLUETOOTH)),
                PERMISSION_REQUEST_BLUETOOTH
            )
        }else{    ActivityCompat.requestPermissions(
            this@MainActivity, arrayOf((Manifest.permission.BLUETOOTH)),
            PERMISSION_REQUEST_BLUETOOTH
        )

        }
    }

    private fun cekScanBTPermission() {
        mLayout= findViewById(R.id.rootLayout)
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_SCAN)
            ==PackageManager.PERMISSION_GRANTED){
            Snackbar.make(mLayout,"Sudah diberikan izin akses Scan Bluetooth", Snackbar.LENGTH_LONG).show()
        }
        else{
            Snackbar.make(mLayout,"Belum diberikan izin akses Scan Bluetooth",Snackbar.LENGTH_LONG).show()
            requestScanBTPermission()
        }
    }

    private fun requestScanBTPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.BLUETOOTH_SCAN

            )){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf((Manifest.permission.BLUETOOTH_SCAN)),
                    PERMISSION_REQUEST_BLUETOOTH_SCAN
                )
            }
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf((Manifest.permission.BLUETOOTH_SCAN)),
                    PERMISSION_REQUEST_BLUETOOTH_SCAN
                )
            }

        }
    }

    private fun cekAdminBTPermission() {
        mLayout= findViewById(R.id.rootLayout)
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_ADMIN)
            ==PackageManager.PERMISSION_GRANTED){
            Snackbar.make(mLayout,"Sudah diberikan izin akses Admin Bluetooth", Snackbar.LENGTH_LONG).show()
        }
        else{
            Snackbar.make(mLayout,"Belum diberikan izin akses Admin Bluetooth",Snackbar.LENGTH_LONG).show()
            requestAdmintBTPermission()
        }
    }

    private fun requestAdmintBTPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.BLUETOOTH_ADMIN

            )){
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf((Manifest.permission.BLUETOOTH_ADMIN)),
                PERMISSION_REQUEST_BLUETOOTH_ADMIN
            )
        }else{    ActivityCompat.requestPermissions(
            this@MainActivity, arrayOf((Manifest.permission.BLUETOOTH_ADMIN)),
            PERMISSION_REQUEST_BLUETOOTH_ADMIN
        )

        }
    }

    private fun cekForBTConPermision() {
        mLayout = findViewById(R.id.rootLayout)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            == PackageManager.PERMISSION_GRANTED){
            Snackbar.make(mLayout,"Sudah diberikan izin Bluetooth Connect", Snackbar.LENGTH_LONG).show()
        }
        else{
            Snackbar.make(mLayout,"Belum diberikan izin akses", Snackbar.LENGTH_LONG).show()
            requestBluetoothConnetctPermission()
        }
    }

    private fun requestBluetoothConnetctPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.BLUETOOTH_CONNECT

            )){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf((Manifest.permission.BLUETOOTH_CONNECT)),
                    PERMISSION_REQUEST_BLUETOOTH_CONNECT
                )
            }
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf((Manifest.permission.BLUETOOTH_CONNECT)),
                    PERMISSION_REQUEST_BLUETOOTH_CONNECT
                )
            }

        }
    }
    //outside main
// ini fungsi untuk menangkap input dari user
    //TODO 5
    fun p1() {
        val t: Thread = object : Thread() {
            override fun run() {
                try {
                    val os = mBluetoothSocket.outputStream
                    var header = ""
                    var he = ""
                    var blank = ""
                    var header2 = ""
                    //var BILL = ""
                    var BILL : String?
                    //var vio = ""
                    var vio : String? = null
                    var header3 = ""
                    var mvdtail = ""
                    var header4 = ""
                    var offname = ""
                    var time = ""
                    var copy = ""
                    val checktop_status = ""
                    blank = "\n\n"
                    he = "      EFULLTECH NIGERIA\n"
                    he = "$he********************************\n\n"
                    header = "FULL NAME:\n"
                    BILL = fullName!!.text.toString() + "\n"   // ini buat ngambil text nama
                    BILL = (BILL
                            + "================================\n")
                    header2 = "COMPANY'S NAME:\n"
                    vio = companyName!!.text.toString() + "\n"  // ini buat ngambil text nama company
                    vio = (vio
                            + "================================\n")
                    header3 = "AGE:\n"
                    mvdtail = age!!.text.toString() + "\n"   // ini buat ngambil text age
                    mvdtail = (mvdtail
                            + "================================\n")
                    header4 = "AGENT DETAILS:\n"
                    offname = agent_detail!!.text.toString() + "\n" // ini buat ngambil text agents detail
                    offname = (offname
                            + "--------------------------------\n")
                    time = formattedDate + "\n\n"
                    copy = "-Customer's Copy\n\n\n\n\n\n\n\n\n"
                    os!!.write(blank.toByteArray())
                    os.write(he.toByteArray())
                    os.write(header.toByteArray())
                    os.write(BILL.toByteArray())
                    os.write(header2.toByteArray())
                    os.write(vio.toByteArray())
                    os.write(header3.toByteArray())
                    os.write(mvdtail.toByteArray())
                    os.write(header4.toByteArray())
                    os.write(offname.toByteArray())
                    os.write(checktop_status.toByteArray())
                    os.write(time.toByteArray())
                    os.write(copy.toByteArray())



                    //ini bedanya dengan diatas adalah, variable valuenya sudah di isi dengan angka
                    // sehingga jika menggunakan toByteArray maka yang dibawah ini menggunakan'
                    //intToByteArray
                    // Setting height --- ini buat semacam feed kertas blank. memberi jeda tulisan kosong untuk print yg ke dua
                    val gs = 29
                    os.write(intToByteArray(gs).toInt())
                    val h = 150
                    os.write(intToByteArray(h).toInt())
                    val n = 170
                    os.write(intToByteArray(n).toInt())

                    // Setting Width
                    val gs_width = 29
                    os.write(intToByteArray(gs_width).toInt())
                    val w = 119
                    os.write(intToByteArray(w).toInt())
                    val n_width = 2
                    os.write(intToByteArray(n_width).toInt())
                } catch (e: Exception) {
                    Log.e("PrintActivity", "Exe ", e)
                }
            }
        }
        t.start()
    }

    fun p2() {
        val tt: Thread = object : Thread() {
            override fun run() {
                try {
                    val os = mBluetoothSocket
                        .getOutputStream()
                    var header = ""
                    var he = ""
                    var blank = ""
                    var header2 = ""
                    var BILL = ""
                    var vio = ""
                    var header3 = ""
                    var mvdtail = ""
                    var header4 = ""
                    var offname = ""
                    var time = ""
                    var copy = ""
                    val checktop_status = ""
                    blank = "\n\n"
                    he = "      EFULLTECH\n"
                    he = "$he********************************\n\n"
                    header = "FULL NAME:\n"
                    BILL = fullName!!.text.toString() + "\n"
                    BILL = (BILL
                            + "================================\n")
                    header2 = "COMPANY'S NAME:\n"
                    vio = companyName!!.text.toString() + "\n"
                    vio = (vio
                            + "================================\n")
                    header3 = "AGE:\n"
                    mvdtail = age!!.text.toString() + "\n"
                    mvdtail = (mvdtail
                            + "================================\n")
                    header4 = "AGENT DETAILS:\n"
                    offname = agent_detail!!.text.toString() + "\n"
                    offname = (offname
                            + "--------------------------------\n")
                    time = formattedDate + "\n\n"
                    copy = "-Agents's Copy\n\n\n\n\n\n\n"
                    os!!.write(blank.toByteArray())
                    os.write(he.toByteArray())
                    os.write(header.toByteArray())
                    os.write(BILL.toByteArray())
                    os.write(header2.toByteArray())
                    os.write(vio.toByteArray())
                    os.write(header3.toByteArray())
                    os.write(mvdtail.toByteArray())
                    os.write(header4.toByteArray())
                    os.write(offname.toByteArray())
                    os.write(checktop_status.toByteArray())
                    os.write(time.toByteArray())
                    os.write(copy.toByteArray())


                    //This is printer specific code you can comment ==== > Start

                    // Setting height
                    val gs = 29
                    os.write(intToByteArray(gs).toInt())
                    val h = 150
                    os.write(intToByteArray(h).toInt())
                    val n = 170
                    os.write(intToByteArray(n).toInt())

                    // Setting Width
                    val gs_width = 29
                    os.write(intToByteArray(gs_width).toInt())
                    val w = 119
                    os.write(intToByteArray(w).toInt())
                    val n_width = 2
                    os.write(intToByteArray(n_width).toInt())
                } catch (e: Exception) {
                    Log.e("PrintActivity", "Exe ", e)
                }
            }
        }
        tt.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        /* Terminate bluetooth connection and close all sockets opened */try {
            if (mBluetoothSocket != null) mBluetoothSocket.close()
        } catch (e: Exception) {
            Log.e("Tag", "Exe ", e)
        }
    }
//TODO 7
    public override fun onActivityResult( mRequestCode: Int, mResultCode: Int, mDataIntent: Intent)
     {
        super.onActivityResult(mRequestCode, mResultCode, mDataIntent)
        when (mRequestCode) {
            REQUEST_CONNECT_DEVICE -> if (mResultCode == RESULT_OK) {
                val mExtra = mDataIntent.extras
                val mDeviceAddress = mExtra!!.getString("DeviceAddress")
                Log.v(
                    TAG,
                    "Coming incoming address $mDeviceAddress"
                )
                mBluetoothDevice = mBluetoothAdapter
                    ?.getRemoteDevice(mDeviceAddress)!!
                mBluetoothConnectProgressDialog = ProgressDialog.show(
                    this,
                    "Connecting...", mBluetoothDevice.getName() + " : "
                            + mBluetoothDevice.getAddress(), true, false
                )
                val mBlutoothConnectThread = Thread(this)
                mBlutoothConnectThread.start()
                // pairToDevice(mBluetoothDevice); This method is replaced by
                // progress dialog with thread
            }
            REQUEST_ENABLE_BT -> if (mResultCode == RESULT_OK) {
                ListPairedDevices()
                val connectIntent = Intent(
                    this@MainActivity,
                    DeviceListActivity::class.java
                )
                startActivityForResult(connectIntent, REQUEST_CONNECT_DEVICE)
            } else {
                Toast.makeText(this@MainActivity, "Not connected to any device", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
//TODO 2
    private fun ListPairedDevices() {
        val mPairedDevices = mBluetoothAdapter?.getBondedDevices()
        if (mPairedDevices != null) {
            if (mPairedDevices.size > 0) {
                for (mDevice: BluetoothDevice in mPairedDevices) {
                    Log.v(
                        TAG, "PairedDevices: " + mDevice.name + "  "
                                + mDevice.address
                    )
                }
            }
        }
    }

    //sepertinya ini mulai pengerjaan ke printernya
    //TODO mulai ke printer??
    override fun run() {
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID)
            mBluetoothAdapter!!.cancelDiscovery()
            mBluetoothSocket.connect()
            mHandler.sendEmptyMessage(0)
        } catch (eConnectException: IOException) {
            Log.d(TAG, "CouldNotConnectToSocket", eConnectException)
            closeSocket(mBluetoothSocket)
            return
        }
    }

    private fun closeSocket(nOpenSocket: BluetoothSocket?) {
        try {
            nOpenSocket!!.close()
            Log.d(TAG, "SocketClosed")
        } catch (ex: IOException) {
            Log.d(TAG, "CouldNotCloseSocket")
        }
    }

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            stat = findViewById(R.id.bpstatus)
            mBluetoothConnectProgressDialog!!.dismiss()
            stat.text = ""
            stat!!.text = "Connected"
            stat.setTextColor(Color.rgb(97, 170, 74))
            mPrint.isEnabled = true
            mScan.text = "Disconnect"
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_BLUETOOTH_SCAN = 30
        private const val PERMISSION_REQUEST_BLUETOOTH_ADMIN = 0
        private const val PERMISSION_REQUEST_BLUETOOTH_CONNECT = 20
        private const val PERMISSION_REQUEST_BLUETOOTH = 10
        protected const val TAG = "MainActivity"
        private const val REQUEST_CONNECT_DEVICE = 1
        private const val REQUEST_ENABLE_BT = 2
        fun intToByteArray(value: Int): Byte {
            val b = ByteBuffer.allocate(4).putInt(value).array()
            for (k in b.indices) {
                println(
                    "Selva  [" + k + "] = " + "0x"
                            + UnicodeFormatter.byteToHex(b[k])
                )
            }
            return b[3]
        }
    }
}
