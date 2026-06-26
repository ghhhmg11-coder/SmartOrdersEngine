package com.smartorders.engine

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TripData(
    val price: Double? = null,
    val pickupTimeMinutes: Int? = null,
    val distanceKm: Double? = null,
    val actionLabels: List<String> = emptyList(),
    val rawText: String = "",
    val packageName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isMatched: Boolean = false
) {
    val formattedTimestamp: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val priceFormatted: String
        get() = if (price != null) "%.2f".format(price) else "--"

    val timeFormatted: String
        get() = if (pickupTimeMinutes != null) "$pickupTimeMinutes دقيقة" else "--"

    val distanceFormatted: String
        get() = if (distanceKm != null) "%.1f كم".format(distanceKm) else "--"
}

data class SessionStats(
    val detected: Int = 0,
    val matched: Int = 0,
    val rejected: Int = 0,
    val totalEarnings: Double = 0.0
)
