package com.example.mobilecardyno

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.mobilecardyno.bluetooth_handling.BluetoothDevicePicker
import com.example.mobilecardyno.bluetooth_handling.BluetoothImplementation
import com.example.mobilecardyno.elm327.ElmAdapter
import com.example.mobilecardyno.elm327.ObdCommands
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var rpmTextView: TextView
    private lateinit var speedTextView: TextView
    private lateinit var throttleTextView: TextView
    private lateinit var tempTextView: TextView
    private lateinit var button: Button
    private lateinit var button2: Button
    private val REQUEST_BLUETOOTH_ENABLED = 1
    private val REQUEST_BLUETOOTH_PERMISSION = 2
    private lateinit var bluetoothImplementation: BluetoothImplementation
    private lateinit var elmAdapter: ElmAdapter
    private lateinit var socket: BluetoothSocket

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup()
    }

    fun getBluetoothImplementation(): BluetoothImplementation { return bluetoothImplementation }

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
                    //rpmTextView.text = "Obroty: $responseRPM RPM"
                    //speedTextView.text = "Predkosc: $responseSpeed km/h"
                    //tempTextView.text = "Temperatura plynu chlodzacego: $responseTemp C"
                    //throttleTextView.text = "Przepustnica: $responseThrottle %"
                }
                delay(50)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setup() {
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        setupVisuals()
        setupButtons()

        BluetoothImplementation(this, this).checkIfBluetoothEnabled()
    }

    private fun setupButtons() {
        button = findViewById<Button>(R.id.button)
        button2 = findViewById<Button>(R.id.button2)

        val devicePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                button.text = "Zacznij pomiar"
            }
        }

        button.setOnClickListener() { test() }
        button2.setOnClickListener() {
            val intent = Intent(this, BluetoothDevicePicker::class.java)
            devicePickerLauncher.launch(intent)
        }
    }

    private fun setupVisuals() {
        rpmTextView = findViewById<TextView>(R.id.rpmTextView)
        speedTextView = findViewById<TextView>(R.id.speedTextView)
        throttleTextView = findViewById<TextView>(R.id.throttleTextView)
        tempTextView = findViewById<TextView>(R.id.tempTextView)
        rpmTextView.text = "RPM"
        speedTextView.text = "speed"
        throttleTextView.text = "throttle"
        tempTextView.text = "temp"
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