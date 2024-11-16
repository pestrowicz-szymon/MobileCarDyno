package com.example.mobilecardyno

enum class ObdCommands(val code: String) {
    RPM("010C"),          // Obroty silnika
    SPEED("010D"),        // Prędkość pojazdu
    THROTTLE("0111"),     // Pozycja przepustnicy
    COOLANT_TEMP("0105"); // Temperatura płynu chłodzącego
}