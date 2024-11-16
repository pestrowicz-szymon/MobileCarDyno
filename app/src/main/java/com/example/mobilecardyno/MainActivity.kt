package com.example.mobilecardyno

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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


    @SuppressLint("MissingPermission", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        //bluetoothImplementation.checkBluetoothAccess()
        bluetoothImplementation.checkIfBluetoothEnabled()

        val dataSet = bluetoothImplementation.pairedDevices()
        val recyclerViewAdapter = RecyclerViewAdapter(dataSet)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recyclerViewAdapter

        val bluetoothAdapter = bluetoothImplementation.getAdapter()
        val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice("00:1D:A5:68:98:8B") // Adres MAC ELM327 (zmień na odpowiedni)
        var socket: BluetoothSocket? = null

        Log.d("XD", "0")
        Log.d("XD", device.uuids[0].uuid.toString())
        socket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
        socket.connect()


        button.setOnClickListener() { test(socket) }

        //test(socket)
        //val outputStream = socket.outputStream
        //val inputStream = socket.inputStream


        Log.d("XD", "Device connected and IOStreams made")

        //val reader = BufferedReader(InputStreamReader(inputStream))

        //-----------------RESTART ELM DEVICE
        /*outputStream.write("AT Z\r".toByteArray())
        outputStream.flush()
        var response:String

        while (true)
        {
            outputStream.write("010C\r".toByteArray())
            outputStream.flush()
            reader.readLine() //code that i have sended to device
            reader.readLine() //"SEARCHING..."
            response = reader.readLine() //actual response

            if (response.length >= 5 && response.substring(0, 5) == "41 0C") {
                Log.d("RESPONSE", response)
                // Jeśli odpowiedź zaczyna się od "41 0C", wyjdź z pętli
                val rpmHexValues = response.substring(6)
                Log.d("RPM HEX VALUES", rpmHexValues)
                val a = rpmHexValues.substring(0, 2).toInt(16)
                val b = rpmHexValues.substring(3, 5).toInt(16)
                val rpm = (a * 256 + b) / 4
                Log.d("RPM VALUE", rpm.toString())
                rpmTextView.text = "Obroty: $rpm RPM"
            }
        }*/

        //var response = reader.readLine() + " NEW LINE " +  reader.readLine() + " NEW LINE " +  reader.readLine() + " NEW LINE " +  reader.readLine()
        //Log.d("RESPONSE2", response)

        //outputStream.write("010C\r".toByteArray())
        //outputStream.flush()

        //response = reader.readLine() + " NEW LINE " +  reader.readLine() + " NEW LINE " +  reader.readLine() + " NEW LINE " +  reader.readLine()
        //Log.d("RESPONSE3", response)

        //2024-11-15 21:05:16.637  9312-9312  RESPONSE2               com.example.mobilecardyno            D  AT Z NEW LINE  NEW LINE  NEW LINE ELM327 v2.1
        //2024-11-15 21:05:16.775  9312-9312  RESPONSE3               com.example.mobilecardyno            D   NEW LINE >010C NEW LINE SEARCHING... NEW LINE 41 0C 0C 0A

        // Reset device
        /*outputStream.write("AT Z\r".toByteArray())
        outputStream.flush()
        var response = reader.readLine() // Odczyt odpowiedzi od ELM327
        Log.d("XDDD", response)
        //Log.d("XDDD", resetString.substring(0,5))
        var x = 0
        while(true)
        {
            if (response != null && response.length >= 5 && response.substring(0, 5) == "41 0C") {
                // Jeśli odpowiedź zaczyna się od "41 0C", wyjdź z pętli
                break
            }

            Log.d("XD", "Przejscie $x, odpowiedz $response")
            outputStream.write("01 0C\r".toByteArray())
            outputStream.flush()
            response = reader.readLine()
            x+=1
        }

        Log.d("XD", "2")
        Log.d("XD", "3")

        for (i in 0..30) {

                Log.d("XD", "PRZEJŚCIE: $i")


                // Read RPM
                outputStream.write("010C\r".toByteArray())
                outputStream.flush()

                val rpmResponse = reader.readLine()
                Log.d("RPM RESPONSE", rpmResponse)

                // Read Speed
                outputStream.write("010D\r".toByteArray())
                outputStream.flush()

                val speedResponse = reader.readLine()
                Log.d("SPEED RESPONSE", speedResponse)

                // Read Throttle
                outputStream.write("0111\r".toByteArray())
                outputStream.flush()

                val throttleResponse = reader.readLine()
                Log.d("THROTTLE REPSPONSE", throttleResponse)


                outputStream.write("0105\r".toByteArray())
                outputStream.flush()

                val tempResponse = reader.readLine()
                Log.d("TEMP RESPONSE", tempResponse)

                //Log.d("XD", "Response: $rpmResponse")

            if (rpmResponse != null && rpmResponse.length >= 5 && rpmResponse.substring(0, 5) == "41 0C") {
                // Jeśli odpowiedź zaczyna się od "41 0C", wyjdź z pętli

                val rpmHexValues = rpmResponse.substring(4)
                Log.d("RPM HEX VALUES", rpmHexValues)
                val a = rpmHexValues.substring(0, 2).toInt(16)
                val b = rpmHexValues.substring(2, 4).toInt(16)
                val rpm = (a * 256 + b) / 4

                Log.d("XD", "RPM: $rpm")
            }
            if (speedResponse != null && speedResponse.length >= 5 && speedResponse.substring(0, 5) == "41 0D") {
                val speedHexValues = speedResponse.substring(4)
                val speed = speedHexValues.substring(0, 2).toInt(16)

                Log.d("XD", "SPEED: $speed")
            }
            if (throttleResponse != null && throttleResponse.length >= 5 && throttleResponse.substring(0, 5) == "41 11") {
                val throttleHexValues = throttleResponse.substring(4)
                val d = throttleHexValues.substring(0, 2).toInt(16)
                val throttle = d * 100 / 255

                Log.d("XD", "THRTL: $throttle")
            }
            if (tempResponse != null && tempResponse.length >= 5 && tempResponse.substring(0, 5) == "41 05") {
                val tempHexValues = tempResponse.substring(4)
                val e = tempHexValues.substring(0, 2).toInt(16)
                val temp = e - 40

                Log.d("XD", "TEMP: $temp")
            }
            }*/


    }

    private fun test(socket: BluetoothSocket) {
        val outputStream = socket.outputStream
        val inputStream = socket.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))
        outputStream.write("AT Z\r".toByteArray())
        outputStream.flush()
        var response:String

        Log.d("BUTTON", "BUTTON PRESSED")

        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                Log.d("WHILE", "PETLA WHILE")
                outputStream.write("010C\r".toByteArray())
                outputStream.flush()
                Log.d("MYCOMMAND", reader.readLine()) //code that i have sended to device
                //Log.d("SEARCHING", reader.readLine()) //"SEARCHING..."
                response = reader.readLine() //actual response
                Log.d("RESPONSE", response)
                if (response.length >= 5 && response.substring(0, 5) == "41 0C") {
                    Log.d("RESPONSE SUBSTRING 0,5", response.substring(0, 5))
                    Log.d("RESPONSE SUBSTRING 6,7", response.substring(6, 8))
                    Log.d("RESPONSE SUBSTRING 6,7", response.substring(9, 11))
                    // Jeśli odpowiedź zaczyna się od "41 0C", wyjdź z pętli
                    //val rpmHexValues = response.substring(6)
                    //Log.d("RPM HEX VALUES", rpmHexValues)
                    val a = response.substring(6, 8).toInt(16)
                    val b = response.substring(9, 11).toInt(16)
                    val rpm = (a * 256 + b) / 4
                    Log.d("RPM VALUE", rpm.toString())
                    withContext(Dispatchers.Main) {
                        rpmTextView.text = "Obroty: $rpm RPM"
                    }
                }

                delay(100)
            }
        }
    }


            // Start the process in a coroutine
        //CoroutineScope(Dispatchers.IO).launch {
        //    connectAndReadRpm()
        //}

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