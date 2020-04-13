package com.example.bluetoothspplibrarytest

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList
import com.mazenrashed.printooth.Printooth
import com.mazenrashed.printooth.ui.ScanningActivity


class MainActivity : AppCompatActivity() {
    private val TAG: String = "Main"

    private var bt: BluetoothSPP? = null
//    private var bcl: BluetoothSPP.BluetoothConnectionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "onCreate")

        bt = BluetoothSPP(this)
        Printooth.init(this)

        // 블루투스 사용가능여부 체크
        if(bt?.isBluetoothAvailable == false) {
            Log.d(TAG, "Bluetooth is not available")
            finish()
        }

        Log.d(TAG, "Bluetooth is available")

        bt?.setOnDataReceivedListener { data, message ->
            // 데이터 수신
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }


        bt?.setBluetoothConnectionListener(object : BluetoothConnectionListener {
            // 연결됐을 때
            override fun onDeviceConnected(name: String, address: String) {
                Toast.makeText(applicationContext
                        , "Connected to $name\n$address"
                        , Toast.LENGTH_SHORT).show()
            }

            // 연결해제
            override fun onDeviceDisconnected() {
                Toast.makeText(applicationContext
                        , "Connection lost", Toast.LENGTH_SHORT).show()
            }

            // 연결실패
            override fun onDeviceConnectionFailed() {
                Toast.makeText(applicationContext
                        , "Unable to connect", Toast.LENGTH_SHORT).show()
            }
        })

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        btnConnect.setOnClickListener {
            when (it.id) {
                R.id.btnConnect -> {
                    if(bt?.serviceState == BluetoothState.STATE_CONNECTED) {
                        bt?.disconnect()
                    } else {
                        val intent: Intent = Intent(applicationContext, DeviceList::class.java)
                        startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE)
                    }
                }
            }
        }

        val btnConnectPrinter = findViewById<Button>(R.id.btnConnectPrinter)
        btnConnectPrinter.setOnClickListener {
            when (it.id) {
                R.id.btnConnectPrinter -> {
                    startActivityForResult(Intent(this, ScanningActivity::class.java), ScanningActivity.SCANNING_FOR_PRINTER)
                }
            }
        }

//        startActivityForResult(Intent(this, ScanningActivity::class.java), ScanningActivity.SCANNING_FOR_PRINTER)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")

        bt?.stopService()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")

        if (bt?.isBluetoothEnabled == false) { //
            val i: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(i, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (bt?.isServiceAvailable == false) {
                    bt?.setupService();
                    bt?.startService(BluetoothState.DEVICE_OTHER);
                    setup();
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult")

        if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode == Activity.RESULT_OK) {
            Log.d("onActivityResult", "success")
        }

        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK) {
                bt?.connect(data)
            }
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt?.setupService()
                bt?.startService(BluetoothState.DEVICE_OTHER)
                setup()
            }
        } else {
            Log.d(TAG, "Bluetooth was not enabled")
            finish()
        }
    }

    fun setup() {
        val btnSend = findViewById<Button>(R.id.btnSend); //데이터 전송
        btnSend.setOnClickListener{
            when(it.id) {
                R.id.btnSend -> {
                    bt?.send("Text", true)
                }
            }
        }
    }
}
