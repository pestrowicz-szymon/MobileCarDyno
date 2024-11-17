package com.example.mobilecardyno

import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

class ElmAdapter(
    private val socket: BluetoothSocket
) {
    val outputStream: OutputStream
    val inputStream: InputStream
    val reader: BufferedReader

    init {
        outputStream = socket.outputStream
        inputStream = socket.inputStream
        reader = BufferedReader(InputStreamReader(inputStream))
        outputStream.write("${ObdCommands.RESTART.code}\r".toByteArray())
        outputStream.flush()
    }

    fun readParameter(param: String): String {
        outputStream.write("${ObdCommands.READ.code}$param\r".toByteArray())
        outputStream.flush()
        var response = reader.readLine()
        while(true) {
            if (response.length > 5 && response.substring(0,2) == ObdCommands.RESPONSE.code) {
                break
            }
            response = reader.readLine()
        }
        when (response.substring(0,5)) {
            "${ObdCommands.RESPONSE.code} ${ObdCommands.RPM.code}" -> return calcRPM(response.substring(6,11))
            "${ObdCommands.RESPONSE.code} ${ObdCommands.SPEED.code}" -> return calcSpeed(response.substring(6,8))
            "${ObdCommands.RESPONSE.code} ${ObdCommands.COOLANT_TEMP.code}" -> return calcTemp(response.substring(6,8))
            "${ObdCommands.RESPONSE.code} ${ObdCommands.THROTTLE.code}" -> return calcThrottle(response.substring(6,8))
            else -> return "CALCERROR"
        }
    }

    private fun calcRPM(hexValue: String): String {
        val a = hexValue.substring(0,2).toInt(16)
        val b = hexValue.substring(3,5).toInt(16)
        return ((a * 256 + b) / 4).toString()
    }

    private fun calcSpeed(hexValue: String): String {
        val a = hexValue.substring(0,2).toInt(16)
        return a.toString()
    }

    private fun calcTemp(hexValue: String): String {
        val a = hexValue.substring(0,2).toInt(16)
        return (a - 40).toString()
    }

    private fun calcThrottle(hexValue: String): String {
        val a = hexValue.substring(0,2).toInt(16)
        return (a * 100 / 255).toString()
    }
}