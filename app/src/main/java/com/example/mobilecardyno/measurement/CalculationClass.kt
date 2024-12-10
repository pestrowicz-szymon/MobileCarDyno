package com.example.mobilecardyno.measurement

import kotlin.math.pow

class CalculationClass(
    private val measures: MutableList<MeasurementData>,
    private val carWeight: Float
) {
    private var calculatedData: MutableList<CalculationData> = mutableListOf()
    private var negativeHPList: MutableList<CalculationData> = mutableListOf()
    private var result: MutableList<CalculationData> = mutableListOf()

    init {
        copyMeasures()
        calcSpeedFromRPM()
        calcDeltas()
        calcPhysicalValues()
        calcAverages()
        divideIntoTwoLists()
        calcSum()
        calcHann()
    }

    fun getResult(): MutableList<CalculationData> { return result }

    private fun copyMeasures() {
        for (item in measures) {
            calculatedData.add(CalculationData(item.timestamp, item.speed, item.rpm))
        }
    }

    private fun calcSpeedFromRPM () {
        val avgSpeed = calculatedData.map {it.speed}.average()
        val avgRPM = calculatedData.map {it.rpm}.average()
        val speedPerRPM = avgSpeed/avgRPM
        for (item in calculatedData) {
            item.speedFromRPM = item.rpm * speedPerRPM // prob useless
            item.speedFromRPMms = item.speedFromRPM!! * 1000 / 3600
        }
    }

    private fun calcDeltas() {
        for (i in calculatedData.indices) {
            if (i == 0) {
                calculatedData[i].deltaTime = 0
                calculatedData[i].deltaSpeed = 0.0
            } else {
                val deltaTime = (calculatedData[i].timestamp - calculatedData[i-1].timestamp).toInt()
                val deltaSpeed = (calculatedData[i].speedFromRPMms!! - calculatedData[i-1].speedFromRPMms!!)
                calculatedData[i].deltaTime = deltaTime
                calculatedData[i].deltaSpeed = deltaSpeed
            }
        }
    }

    private fun calcPhysicalValues() {
        for (item in calculatedData) {
            item.acceleration = item.deltaSpeed!! / (item.deltaTime!!/1000.0)
            item.force = item.acceleration!! * carWeight
            item.powerKW = item.force!! * item.speedFromRPMms!! / 1000
            item.powerHP = item.powerKW!! * 1.36
        }
    }

    private fun calcAverages() {
        calculatedData.removeAt(0)
        for (i in calculatedData.indices) {
            if ( i < 3 ) {
                continue
            } else if ( i > calculatedData.size - 4 ) {
                continue
            } else  {
                val sum = calculatedData[i-3].powerHP!! + calculatedData[i-2].powerHP!! + calculatedData[i-1].powerHP!! + calculatedData[i].powerHP!! + calculatedData[i+1].powerHP!! + calculatedData[i+2].powerHP!! + calculatedData[i+3].powerHP!!
                calculatedData[i].powerHPAVG = (sum/7.0)
            }
        }
        calculatedData.removeAt(0)
        calculatedData.removeAt(0)
        calculatedData.removeAt(0)
        calculatedData.removeAt(calculatedData.size-1)
        calculatedData.removeAt(calculatedData.size-1)
        calculatedData.removeAt(calculatedData.size-1)
    }

    private fun divideIntoTwoLists() {
        for (item in calculatedData) {
            if (item.powerHPAVG!! > 0) {
                result.add(item)
            } else {
                negativeHPList.add(item)
            }
        }
    }

    private fun calcSum() {
        for (item in result) {
            item.negativeHP = calculateRegression(item.rpm, negativeHPList)
            item.sumHP = item.powerHPAVG!! - item.negativeHP!!
            //item.torque = item.sumHP!! * 0.7355 * 9550 / item.rpm
        }
    }

    private fun calcHann() {
        for (i in result.indices) {
            if (i < 2) {
                continue
            } else if (i > result.size - 3) {
                continue
            } else {
                result[i].sumHPHann = result[i-2].sumHP!! * 0.2 + result[i-1].sumHP!! * 0.4 +result[i].sumHP!! * 0.2 +result[i+2].sumHP!! * 0.2
                result[i].torque = result[i].sumHPHann!! / 1.36 * 9550 / result[i].rpm
            }
        }
        result.removeAt(0)
        result.removeAt(0)
        result.removeAt(result.size-1)
        result.removeAt(result.size-1)
    }

    private fun calculateRegression(x: Int, calculationList: List<CalculationData>): Double {
        // Filtrujemy dane na podstawie warunku abs(C - A4) <= 100
        val filteredXData = calculationList
            .filter { kotlin.math.abs(it.rpm - x) <= 100 }
            .map { it.rpm.toDouble() } // Kolumna C (lub rpm)

        val filteredYData = calculationList
            .filter { kotlin.math.abs(it.rpm - x) <= 100 }
            .map { it.powerHPAVG ?: 0.0 } // Kolumna M (lub powerHPAVG)


        val result = forecast(x, filteredXData, filteredYData)
        if (result.isNaN()) {
            return 0.0
        } else {
            return result
        }
    }

    //----------FORECAST FUNCTIONS ------------------
    private fun mean(data: List<Double>): Double {
        return data.sum() / data.size
    }

    private fun covariance(x: List<Double>, y: List<Double>): Double {
        val meanX = mean(x)
        val meanY = mean(y)
        return x.indices.sumOf { (x[it] - meanX) * (y[it] - meanY) } / x.size
    }

    private fun variance(data: List<Double>): Double {
        val meanData = mean(data)
        return data.sumOf { (it - meanData).pow(2) } / data.size
    }

    private fun forecast(x: Int, xData: List<Double>, yData: List<Double>): Double {
        val slope = covariance(xData, yData) / variance(xData)
        val intercept = mean(yData) - slope * mean(xData)
        return slope * x + intercept
    }
    //----------------------------------------------
}