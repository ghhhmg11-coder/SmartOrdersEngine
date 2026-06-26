package com.smartorders.engine

import android.os.Handler
import android.os.Looper
import kotlin.random.Random

class DemoModeManager(
    private val onTripDetected: (TripData) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    private val demoScenarios = listOf(
        Triple(12.50, 3, 1.2),
        Triple(25.00, 5, 3.5),
        Triple(8.75, 2, 0.8),
        Triple(45.00, 8, 7.0),
        Triple(18.00, 4, 2.3),
        Triple(60.00, 10, 9.5),
        Triple(7.00, 1, 0.5),
        Triple(33.50, 6, 4.8)
    )

    fun start() {
        if (running) return
        running = true
        scheduleNext()
    }

    fun stop() {
        running = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun scheduleNext() {
        if (!running) return
        val delay = Random.nextLong(3000, 8000)
        handler.postDelayed({
            if (running) {
                generateDemoTrip()
                scheduleNext()
            }
        }, delay)
    }

    private fun generateDemoTrip() {
        val scenario = demoScenarios.random()
        val actions = listOf("Accept", "قبول")
        val rawText = buildString {
            appendLine("طلب رحلة جديد")
            appendLine("السعر: ${scenario.first} ريال")
            appendLine("وقت الانتظار: ${scenario.second} دقيقة")
            appendLine("المسافة: ${scenario.third} كم")
            appendLine(actions.random())
        }

        val trip = TripData(
            price = scenario.first,
            pickupTimeMinutes = scenario.second,
            distanceKm = scenario.third,
            actionLabels = actions,
            rawText = rawText,
            packageName = "com.demo.mode"
        )
        onTripDetected(trip)
    }
}
