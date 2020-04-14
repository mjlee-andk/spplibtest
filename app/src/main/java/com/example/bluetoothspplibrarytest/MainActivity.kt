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
import com.mazenrashed.printooth.data.printable.Printable
import com.mazenrashed.printooth.data.printable.TextPrintable
import com.mazenrashed.printooth.data.printer.DefaultPrinter
import com.mazenrashed.printooth.ui.ScanningActivity
import com.mazenrashed.printooth.utilities.PrintingCallback
import java.net.URLEncoder


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
            Log.d(TAG, "data received")
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

        // Listen to your printing order state:
        Printooth.printer().printingCallback = object : PrintingCallback {
            override fun connectingWithPrinter() { }

            override fun printingOrderSentSuccessfully() {
                Log.d(TAG, "Print success")
            }  //printer was received your printing order successfully.

            override fun connectionFailed(error: String) {
                Log.d(TAG, "Connection fail")
            }

            override fun onError(error: String) {
                Log.d(TAG, "Connection error")
            }

            override fun onMessage(message: String) { }
        }

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

        // Printing
        val btnPrint = findViewById<Button>(R.id.btnPrint)
        btnPrint.setOnClickListener{
            when(it.id) {
                R.id.btnPrint -> {
                    var testStr = URLEncoder.encode("가나다라마바사", "euc-kr")
                    var printables = ArrayList<Printable>()
                    var printable = TextPrintable.Builder()
                        .setText(testStr) //The text you want to print
                        .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
                        .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_BOLD) //Bold or normal
                        .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
                        .setUnderlined(DefaultPrinter.UNDERLINED_MODE_OFF) // Underline on/off
                        .setCharacterCode(DefaultPrinter.CHARCODE_PC437) // Character code to support languages
                        .setLineSpacing(DefaultPrinter.LINE_SPACING_60)
                        .setNewLinesAfter(3) // To provide n lines after sentence
                        .build()
                    printables.add(printable)
                    Printooth.printer().print(printables)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")

        bt?.stopService()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")

        // 블루투스 꺼져있을 때
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
            Log.d(TAG, "printooth")
            var pairedPrinter = Printooth.getPairedPrinter()
            return
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
