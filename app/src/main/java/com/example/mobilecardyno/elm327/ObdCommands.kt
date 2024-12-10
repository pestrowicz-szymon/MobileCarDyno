package com.example.mobilecardyno.elm327

enum class ObdCommands(val code: String) {
    RESPONSE("41"),     // Odpowiedz od urzadzenia
    RESTART("AT Z"),    // Restart urzadzenia
    CLEARBUFFER("AT D"),// Wyczyszczenie buforu urzadzenia
    READ("01"),         // Prefix do czytania
    RPM("0C"),          // Obroty silnika
    SPEED("0D"),        // Prędkość pojazdu
    THROTTLE("11"),     // Pozycja przepustnicy
    COOLANT_TEMP("05"); // Temperatura płynu chłodzącego
}