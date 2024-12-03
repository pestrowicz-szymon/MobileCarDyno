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
import com.example.mobilecardyno.R
import com.example.mobilecardyno.elm327.ElmAdapterHolder
import com.example.mobilecardyno.elm327.ObdCommands
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow

class MeasurementActivity: ComponentActivity() {
    private val elmAdapter = ElmAdapterHolder.getAdapter()
    private lateinit var textView: TextView
    private lateinit var maxTextView: TextView
    private lateinit var measureList: MutableList<MeasurementData>
    private lateinit var calculationList: MutableList<CalculationData>
    private var job: Job? = null
    private lateinit var button: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup()
        start()
        //loadData()
        ///val calculatedData = calcAll()
        //createChart(calculatedData)
        //start()
    }

    private fun start() {
        elmAdapter!!.restartDevice()
        textView.text = "Przyspiesz wykorzystując jak największy zakres obrotów a następnie odpuść gaz i \"hamując silnikiem\" zwolnij do prędkości początkowej. Po wykonaniu pomiaru wciśnij guzik"
        job = CoroutineScope(Dispatchers.IO).launch {
            //while(true) {
            //    if (elmAdapter.readParameter(ObdCommands.THROTTLE.code).toInt() > 90) { break }
            //}
            textView.text = "Pomiar w trakcie"
            while (true) {
                addMeasure(elmAdapter.readParameter(ObdCommands.SPEED.code).toInt(), elmAdapter.readParameter(ObdCommands.RPM.code).toInt())
            }
        }
    }

    private fun stop() {
        textView.text = "Obliczanie wyników"
        job?.cancel()
        job = null
        createChart(calcAll())
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

    private fun loadData(){
        val inputStream = this.resources.openRawResource(R.raw.measurements_4)
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

    private fun calcAll(): MutableList<CalculationData> { return CalculationClass(measureList).getResult() }

    @SuppressLint("SetTextI18n")
    private fun createChart(
        powerData: MutableList<CalculationData>
    ) {
        for (item in powerData) {
            Log.d("RESULT_DATA", "${item.rpm} ; ${item.sumHPHann} ; ${item.torque}")
        }
        val lineChart: LineChart = findViewById(R.id.lineChart)
        val torqueEntries = powerData.map { Entry(it.rpm.toFloat(), it.torque!!.toFloat() ) }
        val sumHPEntries = powerData.map { Entry(it.rpm.toFloat(), it.sumHPHann!!.toFloat() ) }

        val maxTorque = torqueEntries.maxByOrNull { it.y }
        val maxPower = sumHPEntries.maxByOrNull { it.y }

        val torqueDataSet = LineDataSet(torqueEntries, "Torque")
        torqueDataSet.setDrawCircles(false)
        val sumHPDataSet = LineDataSet(sumHPEntries, "Sum HP")
        sumHPDataSet.setDrawCircles(false)

        maxTextView.text = "Max HP:${maxPower!!.y} @ ${maxPower.x} \nMax Torque:${maxTorque!!.y} @ ${maxTorque.x}"

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
        textView = findViewById(R.id.textView2)
        maxTextView = findViewById(R.id.textViewMaxValues)
        button = findViewById(R.id.button4)
        button.setOnClickListener() {
            stop()
            saveToFile()
        }
        measureList = mutableListOf()
        calculationList = mutableListOf()
    }
}