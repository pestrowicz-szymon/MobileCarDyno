package com.example.mobilecardyno.bluetooth_handling

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilecardyno.elm327.ElmAdapter
import com.example.mobilecardyno.elm327.ElmAdapterHolder
import com.example.mobilecardyno.R

class BluetoothDevicePicker : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var bluetoothImplementation: BluetoothImplementation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup()
    }

    private fun setup() {
        setContentView(R.layout.bluetooth_device_picker)
        enableEdgeToEdge()
        recyclerView = findViewById(R.id.recyclerViewBluetoothDevices)
        recyclerView.layoutManager = LinearLayoutManager(this)
        bluetoothImplementation = BluetoothImplementation(this, this)
        recyclerView.adapter = BluetoothRecyclerViewAdapter(bluetoothImplementation.pairedDevices(), this) { macAddress ->
            onDeviceSelected(macAddress)
        }
    }

    private fun onDeviceSelected(macAddress: String) {
        Toast.makeText(this, "${getString(R.string.PL_bluetooth_connecting)}: $macAddress", Toast.LENGTH_SHORT).show()
        val result = bluetoothImplementation.connectSocket(macAddress)
        result.onSuccess {
            Toast.makeText(this, getString(R.string.PL_bluetooth_connecting_successful), Toast.LENGTH_SHORT).show()
            ElmAdapterHolder.setAdapter(ElmAdapter(result.getOrNull()))
            val resultIntent = Intent()
            setResult(RESULT_OK, resultIntent)
            finish()
        }.onFailure {
            Toast.makeText(this, getString(R.string.PL_bluetooth_connecting_failed), Toast.LENGTH_SHORT).show()
        }
    }
}