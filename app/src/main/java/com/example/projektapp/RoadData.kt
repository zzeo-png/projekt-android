package com.example.projektapp

import android.location.Location

data class RoadData(
    var startLAT: Double,
    var startLNG: Double,
    var endLAT: Double,
    var endLNG: Double,
    // add sensor data
)
