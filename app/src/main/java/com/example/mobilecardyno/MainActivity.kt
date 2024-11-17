package com.example.mobilecardyno

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    private lateinit var rpmTextView: TextView
    private lateinit var speedTextView: TextView
    private lateinit var throttleTextView: TextView
    private lateinit var tempTextView: TextView
    private lateinit var button: Button
    private val REQUEST_BLUETOOTH_ENABLED = 1
    private val REQUEST_BLUETOOTH_PERMISSION = 2
    private lateinit var bluetoothImplementation: BluetoothImplementation
    private lateinit var elmAdapter: ElmAdapter

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup()
    }

    private fun test() {
        Log.d("BUTTON", "BUTTON PRESSED")
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                //Log.d("WHILE", "PETLA WHILE")
                val responseRPM = elmAdapter.readParameter(ObdCommands.RPM.code)
                val responseSpeed = elmAdapter.readParameter(ObdCommands.SPEED.code)
                val responseTemp = elmAdapter.readParameter(ObdCommands.COOLANT_TEMP.code)
                val responseThrottle = elmAdapter.readParameter(ObdCommands.THROTTLE.code)
                //Log.d("RPM VALUE", response)
                withContext(Dispatchers.Main) {
                    rpmTextView.text = "Obroty: $responseRPM RPM"
                    speedTextView.text = "Predkosc: $responseSpeed km/h"
                    tempTextView.text = "Temperatura plynu chlodzacego: $responseTemp C"
                    throttleTextView.text = "Przepustnica: $responseThrottle %"
                }
                delay(50)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setup() {
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        rpmTextView = findViewById<TextView>(R.id.rpmTextView)
        speedTextView = findViewById<TextView>(R.id.speedTextView)
        throttleTextView = findViewById<TextView>(R.id.throttleTextView)
        tempTextView = findViewById<TextView>(R.id.tempTextView)
        button = findViewById<Button>(R.id.button)
        rpmTextView.text = "RPM"
        speedTextView.text = "speed"
        throttleTextView.text = "throttle"
        tempTextView.text = "temp"
        bluetoothImplementation = BluetoothImplementation(this, this)
        bluetoothImplementation.checkIfBluetoothEnabled()
        val socket = bluetoothImplementation.connectSocket("00:1D:A5:68:98:8B")
        elmAdapter = ElmAdapter(socket)
        button.setOnClickListener() { test() }
/*
//View all paired BT devices in RecyclerView
        val dataSet = bluetoothImplementation.pairedDevices()
        val recyclerViewAdapter = RecyclerViewAdapter(dataSet)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recyclerViewAdapter
*/

    }

    @SuppressLint("MissingPermission")
    private suspend fun connectAndReadRpm() {
        val bluetoothAdapter = bluetoothImplementation.getAdapter()
        val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice("00:1D:A5:68:98:8B") // Adres MAC ELM327 (zmień na odpowiedni)
        var socket: BluetoothSocket? = null

        try {
            Log.d("XD", "0")
            socket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
            socket.connect()
            val outputStream = socket.outputStream
            val inputStream = socket.inputStream

            Log.d("XD", "1")

            // Reset device
            withContext(Dispatchers.IO) {
                outputStream.write("AT Z\r".toByteArray())
                outputStream.flush()
            }

            Log.d("XD", "2")

            delay(100)  // wait a moment

            Log.d("XD", "3")

            val reader = BufferedReader(InputStreamReader(inputStream))

            // Read RPM
            withContext(Dispatchers.IO) {
                outputStream.write("010C\r".toByteArray())
                outputStream.flush()
            }
            val rpmResponse = withContext(Dispatchers.IO) {
                reader.readLine()
            }

            // Read Speed
            withContext(Dispatchers.IO) {
                outputStream.write("010D\r".toByteArray())
                outputStream.flush()
            }
            val speedResponse = withContext(Dispatchers.IO) {
                reader.readLine()
            }

            // Read Throttle
            withContext(Dispatchers.IO) {
                outputStream.write("0111\r".toByteArray())
                outputStream.flush()
            }
            val throttleResponse = withContext(Dispatchers.IO) {
                reader.readLine()
            }

            // Read Coolant temperature
            withContext(Dispatchers.IO) {
                outputStream.write("0105\r".toByteArray())
                outputStream.flush()
            }
            val tempResponse = withContext(Dispatchers.IO) {
                reader.readLine()
            }

            Log.d("XD", "Response: $rpmResponse")

            val rpmHexValues = rpmResponse.substring(4)
            val a = rpmHexValues.substring(0, 2).toInt(16)
            val b = rpmHexValues.substring(2, 4).toInt(16)
            val rpm = (a * 256 + b) / 4

            Log.d("XD", "RPM: $rpm")

            val speedHexValues = speedResponse.substring(4)
            val speed = speedHexValues.substring(0, 2).toInt(16)

            Log.d("XD", "SPEED: $speed")

            val throttleHexValues = throttleResponse.substring(4)
            val d = throttleHexValues.substring(0, 2).toInt(16)
            val throttle = d * 100 / 255

            Log.d("XD", "THRTL: $throttle")

            val tempHexValues = tempResponse.substring(4)
            val e = tempHexValues.substring(0, 2).toInt(16)
            val temp = e - 40

            Log.d("XD", "TEMP: $temp")

            withContext(Dispatchers.Main) {
                rpmTextView.text = "Obroty: $rpm RPM"
                speedTextView.text = "Predkosc: $speed km/h"
                throttleTextView.text = "Przepustnica: $throttle %"
                tempTextView.text = "Chlodnica: $temp C"
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            socket?.close()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Jeśli wszystkie wymagane uprawnienia zostały przyznane, włączamy Bluetooth
                startActivityForResult(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_BLUETOOTH_ENABLED
                )
            } else {
                // Obsługa przypadku, gdy użytkownik odmówił przyznania uprawnień
                Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}