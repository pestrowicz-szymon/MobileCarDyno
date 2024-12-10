package com.example.mobilecardyno.measurement

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.isVisible
import com.example.mobilecardyno.R
import com.example.mobilecardyno.elm327.ElmAdapterHolder
import com.example.mobilecardyno.elm327.ObdCommands
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MeasurementActivity: ComponentActivity() {
    private val elmAdapter = ElmAdapterHolder.getAdapter()
    private lateinit var maxTextView: TextView
    private lateinit var measureList: MutableList<MeasurementData>
    private lateinit var calculationList: MutableList<CalculationData>
    private lateinit var lineChart: LineChart
    //private lateinit var buttonLoadNext: Button
    private var loadID = 0
    private var carWeight: Float = 0.0f
    private var job: Job? = null
    private lateinit var button: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        carWeight = intent.getStringExtra("carWeight")!!.toFloat()
        setup()
        start()
    }

    private fun start() {
        elmAdapter!!.restartDevice()
        job = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                addMeasure(elmAdapter.readParameter(ObdCommands.SPEED.code).toInt(), elmAdapter.readParameter(ObdCommands.RPM.code).toInt())
            }
        }
    }

    private fun stop() {
        job?.cancel()
        job = null
        calculationList = calcAll(carWeight)
        createChart(calculationList)
    }

    private fun saveToFile(): File? {
        val csvHeader = "Timestamp,Speed,RPM\n"
        val csvData = measureList.joinToString(separator = "\n") {
            "${it.timestamp},${it.speed},${it.rpm}"
        }
        val csvContent = csvHeader + csvData
        // Tworzymy plik w katalogu publicznym urządzenia (np. Downloads)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "measurements_$timeStamp.csv"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        return try {
            FileWriter(file).use { writer ->
                writer.write(csvContent)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveCalculationsToFile(): File? {
        val csvHeader = "RPM,Force,baseHP,HP,Torque\n"
        val csvData = calculationList.joinToString(separator = "\n") {
            "${it.rpm},${it.sumHPHann},${it.torque}"
        }
        val csvContent = csvHeader + csvData
        // Tworzymy plik w katalogu publicznym urządzenia (np. Downloads)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "calculations_$timeStamp.csv"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        return try {
            FileWriter(file).use { writer ->
                writer.write(csvContent)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun loadData(){
        measureList.clear()
        calculationList.clear()
        val resourceName = "measurements_$loadID"
        val resourceID = resources.getIdentifier(resourceName, "raw", packageName)
        val inputStream = this.resources.openRawResource(resourceID)
        val reader = inputStream.bufferedReader()
        return reader.useLines { lines ->
            lines.drop(1).forEach { line ->
                val tokens = line.split(",")
                if (tokens.size >= 3) {
                    loadMeasure(tokens[1].trim().toInt(), tokens[2].trim().toInt(), tokens[0].trim().toLong())
                }
            }
        }
    }

    private fun addMeasure(speed: Int, rpm: Int) {
        measureList.add(MeasurementData(System.currentTimeMillis(), speed, rpm))
    }

    private fun loadMeasure(speed: Int, rpm: Int, timestamp: Long) {
        measureList.add(MeasurementData(timestamp, speed, rpm))
    }

    private fun calcAll(carWeight: Float): MutableList<CalculationData> { return CalculationClass(measureList, carWeight).getResult() }

    @SuppressLint("SetTextI18n")
    private fun createChart(
        powerData: MutableList<CalculationData>
    ) {
        //for (item in powerData) {
        //    Log.d("RESULT_DATA", "${item.rpm} ; POWERHPAVG:${item.powerHPAVG} ; SUMHP${item.sumHP} ; ${item.sumHPHann} ; ${item.torque}")
        //}
        val torqueEntries = powerData.map { Entry(it.rpm.toFloat(), it.torque!!.toFloat() ) }
        val sumHPEntries = powerData.map { Entry(it.rpm.toFloat(), it.sumHPHann!!.toFloat() ) }

        val maxTorque = torqueEntries.maxByOrNull { it.y }
        val maxPower = sumHPEntries.maxByOrNull { it.y }

        val torqueDataSet = LineDataSet(torqueEntries, "Moment obrotowy [nM]")
        torqueDataSet.setDrawCircles(false)
        val sumHPDataSet = LineDataSet(sumHPEntries, "Moc [km]")
        sumHPDataSet.setDrawCircles(false)

        lineChart.isVisible = true

        maxTextView.text = "Maksymalna moc:${maxPower!!.y} @ ${maxPower.x} \nMaksymalny moment obrotowy:${maxTorque!!.y} @ ${maxTorque.x}"

        torqueDataSet.color = Color.RED
        sumHPDataSet.color = Color.BLUE

        val lineData = LineData(torqueDataSet, sumHPDataSet)
        lineChart.data = lineData
        lineChart.invalidate()
    }


    @SuppressLint("MissingInflatedId")
    private fun setup() {
        setContentView(R.layout.activity_measurement)
        enableEdgeToEdge()
        maxTextView = findViewById(R.id.textViewMaxValues)
        lineChart = findViewById(R.id.lineChart)
        button = findViewById(R.id.button4)
        //buttonLoadNext = findViewById(R.id.button8)
        lineChart.isVisible = false
        button.setOnClickListener() {
            stop()
            saveToFile()
            saveCalculationsToFile()
        }
        /*buttonLoadNext.setOnClickListener() {
            loadData()
            buttonLoadNext.text = "Current: $loadID"
            createChart(calcAll(carWeight))
            loadID += 1
            if (loadID == 13) { loadID = 0 }
        }*/
        measureList = mutableListOf()
        calculationList = mutableListOf()
    }
}