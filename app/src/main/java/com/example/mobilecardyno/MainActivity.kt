package com.example.mobilecardyno

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.example.mobilecardyno.bluetooth_handling.BluetoothDevicePicker
import com.example.mobilecardyno.bluetooth_handling.BluetoothImplementation
import com.example.mobilecardyno.measurement.MeasurementActivity

class MainActivity : ComponentActivity() {
    private lateinit var button: Button
    private lateinit var button2: Button
    private lateinit var weightInput: EditText
    private lateinit var weightInputTip: TextView

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
        button = findViewById(R.id.button)
        button2 = findViewById(R.id.button2)
        weightInput = findViewById(R.id.inputWeight)
        weightInputTip = findViewById(R.id.weight_input_tip)

        val devicePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                weightInput.isVisible = true
                weightInputTip.isVisible = true
            }
        }

        weightInput.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                button.isVisible = weightInput.text.isNotEmpty()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        button.isVisible = false
        weightInput.isVisible = false
        weightInputTip.isVisible = false

        button.setOnClickListener() {
            val intent = Intent(this, MeasurementActivity::class.java)
            intent.putExtra("carWeight", weightInput.text.toString())
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