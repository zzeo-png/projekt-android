package com.example.projektapp

import android.location.Location

data class RoadData(
    val startLAT: Double,
    val startLNG: Double,
    val endLAT: Double,
    val endLNG: Double,
    val condition: Int
)
