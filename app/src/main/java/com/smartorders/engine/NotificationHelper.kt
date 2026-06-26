package com.smartorders.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_TRIP = "smart_orders_trip_channel"
        const val NOTIFICATION_TRIP_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val channel = NotificationChannel(
            CHANNEL_TRIP,
            "طلبات الرحلات",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "إشعارات طلبات الرحلات المناسبة"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 150, 300)
            setSound(soundUri, audioAttr)
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun sendTripMatchNotification(trip: TripData) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = buildString {
            if (trip.price != null) append("السعر: ${trip.priceFormatted} | ")
            if (trip.pickupTimeMinutes != null) append("الوقت: ${trip.timeFormatted} | ")
            if (trip.distanceKm != null) append("المسافة: ${trip.distanceFormatted}")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_TRIP)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("طلب مناسب ✓")
            .setContentText(contentText.ifEmpty { "تم اكتشاف طلب رحلة مناسب" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 150, 300))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_TRIP_ID, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vm.defaultVibrator
                val effect = VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 300, 150, 300), -1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playSound() {
        try {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, soundUri)
            ringtone.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
