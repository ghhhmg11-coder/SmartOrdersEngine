# Smart Orders Engine — Native Android App

محرك الطلبات الذكي — تطبيق Android أصلي بـ Kotlin

---

## How to Open in Android Studio

1. Extract this folder
2. Open Android Studio → **File → Open** → select the `smart-orders-engine/` folder
3. Wait for Gradle sync to complete
4. Connect a device or start an emulator
5. Press **Run (▶)**

> **Note:** The first Gradle sync will download ~300 MB of dependencies. Make sure you have an internet connection.

---

## Project Structure

```
app/src/main/
├── java/com/smartorders/engine/
│   ├── MainActivity.kt           — Main activity with bottom navigation
│   ├── SmartOrdersService.kt     — Accessibility Service (core engine)
│   ├── TripExtractor.kt          — Regex-based text parser
│   ├── DashboardFragment.kt      — Dashboard UI
│   ├── DebugFragment.kt          — Debug/diagnostic screen
│   ├── SettingsFragment.kt       — Settings screen
│   ├── PrefsManager.kt           — SharedPreferences wrapper
│   ├── AppRepository.kt          — Shared live data repository
│   ├── NotificationHelper.kt     — Notifications + vibration + sound
│   ├── DemoModeManager.kt        — Trip simulation engine
│   └── TripData.kt               — Data models
├── res/
│   ├── xml/accessibility_service_config.xml  — Accessibility service config
│   ├── navigation/nav_graph.xml              — Navigation graph
│   ├── layout/                               — UI layouts
│   ├── values/colors.xml                     — Dark theme colors
│   └── values/strings.xml                    — Arabic strings
└── AndroidManifest.xml
```

---

## Setup Instructions

### Step 1 — Enable Accessibility Service
1. Open the app
2. Tap **"فتح إعدادات الإمكانية"** (Open Accessibility Settings)
3. Find **Smart Orders Engine** in the list
4. Enable it

### Step 2 — Configure Settings
- Go to the **Settings** tab
- Set your price, time, and distance ranges
- Enable sound and vibration alerts
- Toggle the helper on

### Step 3 — Test with Demo Mode
- On the Dashboard, toggle **وضع العرض التجريبي** (Demo Mode)
- The app will simulate trip requests every 3–8 seconds
- Matched trips will trigger notifications, sound, and vibration

---

## Features

| Feature | Status |
|---------|--------|
| Accessibility Service (TYPE_WINDOW_CONTENT_CHANGED, TYPE_WINDOW_STATE_CHANGED, TYPE_VIEW_TEXT_CHANGED) | ✅ |
| Recursive AccessibilityNodeInfo reading | ✅ |
| Price extraction (multi-currency regex) | ✅ |
| Pickup time extraction (Arabic + English) | ✅ |
| Distance extraction (km/كم) | ✅ |
| Action label detection (Accept/قبول) | ✅ |
| Debug screen with raw text | ✅ |
| Settings with SharedPreferences | ✅ |
| Sound notification on match | ✅ |
| Vibration on match | ✅ |
| Local notification "طلب مناسب" | ✅ |
| Open Accessibility Settings button | ✅ |
| Arabic RTL dark theme | ✅ |
| Dashboard (detected/matched/rejected/earnings) | ✅ |
| Demo Mode (simulates trips) | ✅ |

---

## Requirements

- Android 8.0+ (API 26+)
- Android Studio Hedgehog (2023.1.1) or newer
- Android Gradle Plugin 8.2.2
- Gradle 8.4
- Kotlin 1.9.22
