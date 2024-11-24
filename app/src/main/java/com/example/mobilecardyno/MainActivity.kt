package com.example.mobilecardyno

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.mobilecardyno.bluetooth_handling.BluetoothDevicePicker
import com.example.mobilecardyno.bluetooth_handling.BluetoothImplementation
import com.example.mobilecardyno.measurement.MeasurementActivity

class MainActivity : ComponentActivity() {
    private lateinit var button: Button
    private lateinit var button2: Button

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setup() {
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        setupButtons()

        BluetoothImplementation(this, this).checkIfBluetoothEnabled()
    }

    private fun setupButtons() {
        button = findViewById<Button>(R.id.button)
        button2 = findViewById<Button>(R.id.button2)

        button.isEnabled = false

        val devicePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                button.text = "Zacznij pomiar"
                button.isEnabled = true
            }
        }

        button.setOnClickListener() {
            val intent = Intent(this, MeasurementActivity::class.java)
            startActivity(intent)
        }
        button2.setOnClickListener() {
            val intent = Intent(this, BluetoothDevicePicker::class.java)
            devicePickerLauncher.launch(intent)
        }
    }

    @SuppressLint("MissingPermission")
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