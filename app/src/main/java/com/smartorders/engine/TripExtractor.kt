package com.smartorders.engine

import android.view.accessibility.AccessibilityNodeInfo

object TripExtractor {

    private val PRICE_REGEX = Regex(
        """(?:[\$€£﷼]|SAR|EGP|AED|USD|ريال|جنيه|درهم)?\s*(\d+(?:[.,]\d{1,2})?)\s*(?:[\$€£﷼]|SAR|EGP|AED|USD|ريال|جنيه|درهم)?""",
        RegexOption.IGNORE_CASE
    )
    private val TIME_REGEX = Regex(
        """(\d+)\s*(?:min|دقيقة|دقائق|mins|minute|minutes|دق)""",
        RegexOption.IGNORE_CASE
    )
    private val DISTANCE_REGEX = Regex(
        """(\d+(?:[.,]\d{1,2})?)\s*(?:km|كم|كيلو|كيلومتر|mile|mi)""",
        RegexOption.IGNORE_CASE
    )
    private val ACCEPT_LABELS = setOf(
        "accept", "قبول", "قبل", "Accept Trip", "موافق", "تأكيد", "Confirm"
    )

    fun extractFromNodes(root: AccessibilityNodeInfo?): Pair<String, TripData?> {
        val allText = StringBuilder()
        collectText(root, allText, 0)
        val raw = allText.toString().trim()
        val trip = parseTrip(raw, "")
        return Pair(raw, trip)
    }

    fun parseTrip(rawText: String, packageName: String): TripData? {
        if (rawText.isBlank()) return null

        val price = extractPrice(rawText)
        val time = extractTime(rawText)
        val distance = extractDistance(rawText)
        val actions = extractActions(rawText)

        if (price == null && time == null && distance == null) return null

        return TripData(
            price = price,
            pickupTimeMinutes = time,
            distanceKm = distance,
            actionLabels = actions,
            rawText = rawText,
            packageName = packageName
        )
    }

    private fun extractPrice(text: String): Double? {
        return PRICE_REGEX.findAll(text)
            .mapNotNull { match ->
                match.groupValues[1].replace(",", ".").toDoubleOrNull()
            }
            .filter { it in 1.0..9999.0 }
            .firstOrNull()
    }

    private fun extractTime(text: String): Int? {
        return TIME_REGEX.find(text)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun extractDistance(text: String): Double? {
        return DISTANCE_REGEX.find(text)
            ?.groupValues?.get(1)
            ?.replace(",", ".")
            ?.toDoubleOrNull()
    }

    private fun extractActions(text: String): List<String> {
        return ACCEPT_LABELS.filter { label ->
            text.contains(label, ignoreCase = true)
        }
    }

    private fun collectText(node: AccessibilityNodeInfo?, sb: StringBuilder, depth: Int) {
        if (node == null || depth > 30) return
        val text = node.text?.toString()
        val desc = node.contentDescription?.toString()
        if (!text.isNullOrBlank()) sb.append(text).append("\n")
        else if (!desc.isNullOrBlank()) sb.append(desc).append("\n")
        for (i in 0 until node.childCount) {
            collectText(node.getChild(i), sb, depth + 1)
        }
    }
}
