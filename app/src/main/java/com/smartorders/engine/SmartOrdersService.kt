package com.smartorders.engine

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class SmartOrdersService : AccessibilityService() {

    private lateinit var prefs: PrefsManager
    private lateinit var notificationHelper: NotificationHelper
    private var lastProcessedText = ""
    private var lastEventTime = 0L
    private var lastAutoAcceptTime = 0L

    companion object {
        private val ACCEPT_KEYWORDS = listOf(
            "قبول العرض", "Accept", "قبول", "تأكيد", "Confirm", "Accept Trip"
        )
        private val TRIP_TRIGGER_KEYWORDS = listOf(
            "قبول العرض", "Accept", "مشوار", "رحلة", "Trip", "طلب"
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = PrefsManager(this)
        notificationHelper = NotificationHelper(this)

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY
        }
        serviceInfo = info

        AppRepository.serviceRunning.postValue(true)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!prefs.serviceEnabled) return

        val now = System.currentTimeMillis()
        if (now - lastEventTime < 300) return
        lastEventTime = now

        val rootNode = rootInActiveWindow ?: return
        val (rawText, trip) = TripExtractor.extractFromNodes(rootNode)

        if (rawText.isBlank() || rawText == lastProcessedText) return
        lastProcessedText = rawText

        AppRepository.rawScreenText.postValue(rawText)

        if (trip != null) {
            val pkgName = event.packageName?.toString() ?: ""
            val tripWithPkg = trip.copy(packageName = pkgName)
            val isMatch = prefs.tripMatchesSettings(tripWithPkg)
            val finalTrip = tripWithPkg.copy(isMatched = isMatch)

            AppRepository.recordTrip(finalTrip)

            if (isMatch) {
                if (prefs.soundEnabled) notificationHelper.playSound()
                if (prefs.vibrationEnabled) notificationHelper.vibrate()
                notificationHelper.sendTripMatchNotification(finalTrip)

                if (prefs.autoAcceptEnabled) {
                    val screenText = rawText
                    val isTripScreen = TRIP_TRIGGER_KEYWORDS.any { screenText.contains(it) }
                    if (isTripScreen && now - lastAutoAcceptTime > 3000) {
                        val clicked = findAndClickAcceptButton(rootNode)
                        if (clicked) {
                            lastAutoAcceptTime = now
                            AppRepository.lastAutoAcceptResult.postValue("✓ تم القبول التلقائي")
                        } else {
                            AppRepository.lastAutoAcceptResult.postValue("✗ لم يُعثر على زر القبول")
                        }
                    }
                }
            }
        }
    }

    private fun findAndClickAcceptButton(root: AccessibilityNodeInfo): Boolean {
        for (keyword in ACCEPT_KEYWORDS) {
            val nodes = root.findAccessibilityNodeInfosByText(keyword)
            if (!nodes.isNullOrEmpty()) {
                val node = nodes[0]
                val clicked = executeClickOrGesture(node)
                if (clicked) return true
            }
        }
        return false
    }

    private fun executeClickOrGesture(node: AccessibilityNodeInfo): Boolean {
        if (node.isClickable) {
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }

        val parent = node.parent
        if (parent != null && parent.isClickable) {
            return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }

        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val centerX = bounds.centerX().toFloat()
        val centerY = bounds.centerY().toFloat()

        if (centerX > 0f && centerY > 0f) {
            val clickPath = Path().apply { moveTo(centerX, centerY) }
            val gestureSpec = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(clickPath, 0, 40))
                .build()
            return dispatchGesture(gestureSpec, null, null)
        }
        return false
    }

    override fun onInterrupt() {
        AppRepository.serviceRunning.postValue(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppRepository.serviceRunning.postValue(false)
    }
}
