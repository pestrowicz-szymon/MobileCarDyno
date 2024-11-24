package com.example.mobilecardyno.measurement

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.mobilecardyno.R
import com.example.mobilecardyno.elm327.ElmAdapterHolder
import com.example.mobilecardyno.elm327.ObdCommands
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MeasurementActivity: ComponentActivity() {
    private val elmAdapter = ElmAdapterHolder.getAdapter()
    private lateinit var textView: TextView
    private lateinit var measureList: MutableList<MeasurementData>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup()
        start()
    }

    private fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                addMeasure(elmAdapter?.readParameter(ObdCommands.SPEED.code)!!.toInt())
                val textData = measureList.joinToString(separator = "\n") {
                    "Time: ${it.timestamp}, Speed: ${it.speed} km/h"
                }
                withContext(Dispatchers.Main) {
                    textView.text = textData
                }
                delay(50)
            }
        }
    }

    private fun addMeasure(speed: Int) {
        measureList.add(MeasurementData(System.currentTimeMillis(), speed))
    }

    @SuppressLint("MissingInflatedId")
    private fun setup() {
        setContentView(R.layout.activity_measurement)
        enableEdgeToEdge()
        textView = findViewById(R.id.textView2)
    }
}