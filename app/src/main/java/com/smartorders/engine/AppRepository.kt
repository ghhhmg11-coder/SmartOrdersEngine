package com.smartorders.engine

import androidx.lifecycle.MutableLiveData

object AppRepository {
    val lastTrip = MutableLiveData<TripData?>(null)
    val rawScreenText = MutableLiveData<String>("")
    val sessionStats = MutableLiveData<SessionStats>(SessionStats())
    val serviceRunning = MutableLiveData<Boolean>(false)
    val tripHistory = MutableLiveData<List<TripData>>(emptyList())
    val lastAutoAcceptResult = MutableLiveData<String>("")

    fun recordTrip(trip: TripData) {
        val current = sessionStats.value ?: SessionStats()
        val newStats = if (trip.isMatched) {
            current.copy(
                detected = current.detected + 1,
                matched = current.matched + 1,
                totalEarnings = current.totalEarnings + (trip.price ?: 0.0)
            )
        } else {
            current.copy(
                detected = current.detected + 1,
                rejected = current.rejected + 1
            )
        }
        sessionStats.postValue(newStats)
        lastTrip.postValue(trip)
        val history = tripHistory.value?.toMutableList() ?: mutableListOf()
        history.add(0, trip)
        if (history.size > 50) history.removeAt(history.size - 1)
        tripHistory.postValue(history)
    }

    fun resetStats() {
        sessionStats.postValue(SessionStats())
        tripHistory.postValue(emptyList())
        lastTrip.postValue(null)
    }
}
