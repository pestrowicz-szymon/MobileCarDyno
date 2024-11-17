package com.example.mobilecardyno

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class BluetoothImplementation(
    private val context: Context,
    private val activity: Activity
) {
    private val REQUEST_BLUETOOTH_ENABLED = 1
    private val REQUEST_BLUETOOTH_PERMISSION = 2

    //Getting info about device bluetooth adapter
    private val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter

    fun getAdapter(): BluetoothAdapter {return bluetoothAdapter}
    fun getManager(): BluetoothManager {return bluetoothManager}

    @SuppressLint("MissingPermission")
    fun pairedDevices(): Array<String> {
        return bluetoothAdapter.bondedDevices?.map { device ->
            val deviceName = device.name ?: "Nieznane urządzenie"
            val macAddress = device.address
            "$deviceName: $macAddress"
        }?.toTypedArray() ?: emptyArray()
    }

    fun connectSocket(macAddress: String): BluetoothSocket {
        val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress) // Adres MAC ELM327 (zmień na odpowiedni)
        val socket: BluetoothSocket? = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
        socket?.connect()
        return socket!!
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun checkIfBluetoothEnabled() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            activity.startActivityForResult(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                REQUEST_BLUETOOTH_ENABLED
            )
        } else {
            checkBluetoothAccess()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkBluetoothAccess() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                ),
                REQUEST_BLUETOOTH_PERMISSION
            )
        }
    }
}