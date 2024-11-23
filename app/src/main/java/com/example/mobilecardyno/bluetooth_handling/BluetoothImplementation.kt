package com.example.mobilecardyno.bluetooth_handling

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
    private val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter

    @SuppressLint("MissingPermission")
    fun pairedDevices(): List<BluetoothDeviceInfo> {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        val deviceList = mutableListOf<BluetoothDeviceInfo>()
        if (!pairedDevices.isNullOrEmpty()) {
            for (device in pairedDevices) {
                val deviceName = device.name // Nazwa urządzenia
                val deviceMac = device.address // Adres MAC
                deviceList.add(BluetoothDeviceInfo(deviceName, deviceMac))
            }
        }
        return deviceList
    }

    fun connectSocket(macAddress: String): Result<BluetoothSocket> {
        return try {
            val socket = connectSocket2(macAddress)
            Result.success(socket)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectSocket2(macAddress: String): BluetoothSocket {
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