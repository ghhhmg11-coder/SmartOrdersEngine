package com.smartorders.engine

import android.content.Context
import android.content.SharedPreferences

class PrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREF_NAME = "smart_orders_prefs"
        const val KEY_SERVICE_ENABLED = "service_enabled"
        const val KEY_MIN_PRICE = "min_price"
        const val KEY_MAX_PRICE = "max_price"
        const val KEY_MIN_TIME = "min_time"
        const val KEY_MAX_TIME = "max_time"
        const val KEY_MIN_DISTANCE = "min_distance"
        const val KEY_MAX_DISTANCE = "max_distance"
        const val KEY_SOUND_ENABLED = "sound_enabled"
        const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        const val KEY_DEMO_MODE = "demo_mode"
        const val KEY_AUTO_ACCEPT = "auto_accept_enabled"
    }

    var serviceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_ENABLED, value).apply()

    var minPrice: Float
        get() = prefs.getFloat(KEY_MIN_PRICE, 5f)
        set(value) = prefs.edit().putFloat(KEY_MIN_PRICE, value).apply()

    var maxPrice: Float
        get() = prefs.getFloat(KEY_MAX_PRICE, 100f)
        set(value) = prefs.edit().putFloat(KEY_MAX_PRICE, value).apply()

    var minPickupTime: Int
        get() = prefs.getInt(KEY_MIN_TIME, 0)
        set(value) = prefs.edit().putInt(KEY_MIN_TIME, value).apply()

    var maxPickupTime: Int
        get() = prefs.getInt(KEY_MAX_TIME, 10)
        set(value) = prefs.edit().putInt(KEY_MAX_TIME, value).apply()

    var minDistance: Float
        get() = prefs.getFloat(KEY_MIN_DISTANCE, 0f)
        set(value) = prefs.edit().putFloat(KEY_MIN_DISTANCE, value).apply()

    var maxDistance: Float
        get() = prefs.getFloat(KEY_MAX_DISTANCE, 20f)
        set(value) = prefs.edit().putFloat(KEY_MAX_DISTANCE, value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()

    var demoMode: Boolean
        get() = prefs.getBoolean(KEY_DEMO_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DEMO_MODE, value).apply()

    var autoAcceptEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_ACCEPT, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_ACCEPT, value).apply()

    fun tripMatchesSettings(trip: TripData): Boolean {
        val priceOk = trip.price?.let { it >= minPrice && it <= maxPrice } ?: false
        val timeOk = trip.pickupTimeMinutes?.let { it >= minPickupTime && it <= maxPickupTime } ?: false
        val distanceOk = trip.distanceKm?.let { it >= minDistance && it <= maxDistance } ?: false
        return priceOk && timeOk && distanceOk
    }
}
