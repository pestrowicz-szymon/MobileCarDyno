package com.example.mobilecardyno

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mobilecardyno.ui.theme.MobileCarDynoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    private lateinit var rpmTextView: TextView
    private val REQUEST_BLUETOOTH_ENABLED = 1
    private val REQUEST_BLUETOOTH_PERMISSION = 2


    private val REQUEST_CODE_BLUETOOTH_CONNECT = 1
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        rpmTextView = findViewById<TextView>(R.id.rpmTextView)

        checkBluetooth()
    }

    private fun checkBluetooth() {
        //Getting info about device bluetooth adapter
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        //Checking if bluetooth in device is enabled
        //If not then requesting for enable
        if (!bluetoothAdapter.isEnabled) {
            //Checking if app have permission to enable bluetooth
            //If not requesting for bluetooth permissions, then requesting enabled bluetooth
            //If yes requesting enabled bluetooth
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    REQUEST_BLUETOOTH_PERMISSION
                )
            } else {
                startActivityForResult(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_BLUETOOTH_ENABLED
                )
            }
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


/*


                        rpmTextView.text = "2137"
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {

                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(
                                    Manifest.permission.BLUETOOTH,
                                    Manifest.permission.BLUETOOTH_ADMIN,
                                    Manifest.permission.BLUETOOTH_SCAN,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ),
                                REQUEST_CODE_BLUETOOTH
                            )
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return
                        }
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                    }


        //rpmTextView.text = "420"

        /if (bluetoothAdapter == null) {
            Toast.makeText(this, "Urządzenie nie obsługuje Bluetooth", Toast.LENGTH_SHORT).show()
        } else {
            // Sprawdź, czy Bluetooth jest włączony
            if (!bluetoothAdapter.isEnabled) {
                Toast.makeText(
                    this,
                    "Włącz Bluetooth, aby wyświetlić sparowane urządzenia",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                //var pairedDevices = setOf(bluetoothAdapter?.bondedDevices)
                //var deviceNames = listOf(pairedDevices)
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_CODE_BLUETOOTH_CONNECT)
                }
                val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
                if (pairedDevices.isNotEmpty()) {
                    for (device in pairedDevices) {
                        val deviceName = device.name
                        val deviceAdress = device.address
                        println("Sparowane urządzenie: $deviceName : $deviceAdress")
                        Log.d("XD", "Sparowane urządzenie: $deviceName : $deviceAdress")
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Nie wykryto sparowanych urządzeń bluetooth",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        setContent {
            MobileCarDynoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        rpmTextView.text = "RPM: "
        CoroutineScope(Dispatchers.IO).launch {
            connectAndReadRpm()
        }

    private fun checkBluetoothPermissions() {

    }

    private suspend fun connectAndReadRpm() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice("00:1D:A5:68:98:8B") // Adres MAC ELM327 (zmień na odpowiedni)

        rpmTextView.text = "XDDD"

        var socket: BluetoothSocket? = null
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            socket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
            socket?.connect()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val outputStream = socket?.outputStream
        val inputStream = socket?.inputStream

        rpmTextView.text = "LOL"

// Wysyłanie komendy „AT Z” (reset urządzenia)
        outputStream?.write("AT Z\r".toByteArray())
        outputStream?.flush()

// Przykład odczytu odpowiedzi
        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = reader.readLine()

        outputStream?.write("010C\r".toByteArray())
        outputStream?.flush()

        val rpmResponse = reader.readLine()

        val hexValues = rpmResponse.substring(4) // Pomiń kod kontrolny i PID
        val a = hexValues.substring(0, 2).toInt(16)
        val b = hexValues.substring(2, 4).toInt(16)
        val rpm = (a * 256 + b) / 4

        rpmTextView.text = "Obroty: $rpm RPM"
    }
}
*/
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MobileCarDynoTheme {
        Greeting("Android")
    }
}