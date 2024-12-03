package com.example.mobilecardyno.measurement

data class CalculationData(
    val timestamp: Long,
    val speed: Int,
    val rpm: Int,
    var speedFromRPM: Double? = null,
    var speedFromRPMms: Double? = null,
    var deltaTime: Int? = null,
    var deltaSpeed: Double? = null,
    var acceleration: Double? = null,
    var force: Double? = null,
    var powerKW: Double? = null,
    var powerHP: Double? = null,
    var powerHPAVG: Double? = null,
    var torque: Double? = null,
    var negativeHP: Double? = null,
    var sumHP: Double? = null,
    var sumHPHann: Double? = null
)
