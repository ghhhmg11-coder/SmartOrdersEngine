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
        /**
         * Accept button keywords — any of these visible on screen triggers auto-accept.
         * No package name check; works with any app.
         */
        private val ACCEPT_KEYWORDS = listOf(
            "قبول العرض",
            "Accept",
            "قبول",
            "تأكيد",
            "Confirm",
            "Accept Trip",
            "موافق"
        )

        /**
         * Packages to skip entirely — our own app and the Jeeny helper to avoid
         * false positives / feedback loops.
         */
        private val EXCLUDED_PACKAGES = setOf(
            "com.smartorders.engine",
            "com.jeenyultimate.helper"
        )

        /** Minimum ms between two consecutive auto-accept clicks. */
        private const val AUTO_ACCEPT_COOLDOWN_MS = 3_000L

        /** Minimum ms between processing two consecutive events. */
        private const val EVENT_THROTTLE_MS = 300L
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = PrefsManager(this)
        notificationHelper = NotificationHelper(this)

        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY
            // packageNames is NOT set → monitors all installed apps
        }

        AppRepository.serviceRunning.postValue(true)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!prefs.serviceEnabled) return

        // --- Skip excluded packages immediately ---
        val pkgName = event.packageName?.toString() ?: ""
        if (pkgName in EXCLUDED_PACKAGES) return

        // --- Throttle: ignore events that arrive too fast ---
        val now = System.currentTimeMillis()
        if (now - lastEventTime < EVENT_THROTTLE_MS) return
        lastEventTime = now

        val rootNode = rootInActiveWindow ?: return

        // --- Collect all visible text from the screen ---
        val rawText = collectAllText(rootNode)
        if (rawText.isBlank() || rawText == lastProcessedText) return
        lastProcessedText = rawText

        AppRepository.rawScreenText.postValue(rawText)

        // ----------------------------------------------------------------
        // AUTO-ACCEPT — runs on text detection alone, no TripExtractor
        // dependency. If any accept keyword is visible and the feature is
        // enabled, click the button immediately.
        // ----------------------------------------------------------------
        if (prefs.autoAcceptEnabled) {
            val hasAcceptText = ACCEPT_KEYWORDS.any { rawText.contains(it, ignoreCase = true) }
            if (hasAcceptText && now - lastAutoAcceptTime > AUTO_ACCEPT_COOLDOWN_MS) {
                val clicked = findAndClickAcceptButton(rootNode)
                if (clicked) {
                    lastAutoAcceptTime = now
                    AppRepository.lastAutoAcceptResult.postValue("✓ تم القبول التلقائي — $pkgName")
                    if (prefs.soundEnabled) notificationHelper.playSound()
                    if (prefs.vibrationEnabled) notificationHelper.vibrate()
                } else {
                    AppRepository.lastAutoAcceptResult.postValue("✗ نص القبول موجود لكن لم يُعثر على زر قابل للضغط")
                }
            }
        }

        // ----------------------------------------------------------------
        // TRIP RECORDING — still use TripExtractor for logging/history.
        // This is independent of auto-accept; a trip is logged whenever
        // the screen has recognisable price/time/distance data.
        // ----------------------------------------------------------------
        val trip = TripExtractor.parseTrip(rawText, pkgName)
        if (trip != null) {
            val isMatch = prefs.tripMatchesSettings(trip)
            val finalTrip = trip.copy(isMatched = isMatch)
            AppRepository.recordTrip(finalTrip)

            if (isMatch) {
                notificationHelper.sendTripMatchNotification(finalTrip)
            }
        }
    }

    // ----------------------------------------------------------------
    // Click helpers
    // ----------------------------------------------------------------

    /**
     * Finds the accept button node by text, resolves its screen bounds,
     * and dispatches a physical finger-tap gesture directly on its centre.
     *
     * ACTION_CLICK is intentionally skipped: Jeeny's accept button has an
     * animated ProgressBar overlay that intercepts programmatic clicks.
     * dispatchGesture() injects a raw touch event that bypasses the view
     * hierarchy and reaches the button regardless of overlays.
     */
    private fun findAndClickAcceptButton(root: AccessibilityNodeInfo): Boolean {
        for (keyword in ACCEPT_KEYWORDS) {
            val nodes = root.findAccessibilityNodeInfosByText(keyword)
            if (!nodes.isNullOrEmpty()) {
                for (node in nodes) {
                    val bounds = resolveButtonBounds(node) ?: continue
                    val tapped = dispatchTap(
                        bounds.centerX().toFloat(),
                        bounds.centerY().toFloat()
                    )
                    if (tapped) {
                        AppRepository.lastAutoAcceptResult.postValue(
                            "✓ Gesture tap → (${bounds.centerX()}, ${bounds.centerY()}) — \"$keyword\""
                        )
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Returns the best non-empty Rect for a node.
     * Walks up the parent chain (up to 4 levels) if the node's own bounds
     * are zero-sized (e.g. a text node inside a container).
     */
    private fun resolveButtonBounds(node: AccessibilityNodeInfo): Rect? {
        val bounds = Rect()

        // Try the node itself first
        node.getBoundsInScreen(bounds)
        if (bounds.width() > 0 && bounds.height() > 0) return bounds

        // Walk up the parent chain
        var ancestor: AccessibilityNodeInfo? = node.parent
        repeat(4) {
            ancestor?.getBoundsInScreen(bounds)
            if (bounds.width() > 0 && bounds.height() > 0) return bounds
            ancestor = ancestor?.parent
        }
        return null
    }

    /**
     * Dispatches a realistic finger-tap gesture at (x, y).
     * Duration of 120 ms mimics a natural press — long enough for the
     * touch system to register it, short enough not to trigger long-press.
     */
    private fun dispatchTap(x: Float, y: Float): Boolean {
        if (x <= 0f || y <= 0f) return false
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, 120L))
            .build()
        return dispatchGesture(gesture, null, null)
    }

    // ----------------------------------------------------------------
    // Text collection
    // ----------------------------------------------------------------

    /**
     * Recursively collects all visible text and content-descriptions from the
     * accessibility node tree (depth-limited to avoid stack overflows).
     */
    private fun collectAllText(root: AccessibilityNodeInfo?): String {
        val sb = StringBuilder()
        collectText(root, sb, 0)
        return sb.toString().trim()
    }

    private fun collectText(node: AccessibilityNodeInfo?, sb: StringBuilder, depth: Int) {
        if (node == null || depth > 30) return
        val text = node.text?.toString()
        val desc = node.contentDescription?.toString()
        when {
            !text.isNullOrBlank() -> sb.append(text).append('\n')
            !desc.isNullOrBlank() -> sb.append(desc).append('\n')
        }
        for (i in 0 until node.childCount) {
            collectText(node.getChild(i), sb, depth + 1)
        }
    }

    // ----------------------------------------------------------------
    // Lifecycle
    // ----------------------------------------------------------------

    override fun onInterrupt() {
        AppRepository.serviceRunning.postValue(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppRepository.serviceRunning.postValue(false)
    }
}
