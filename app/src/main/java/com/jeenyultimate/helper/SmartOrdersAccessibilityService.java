package com.jeenyultimate.helper;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.content.SharedPreferences;
import java.util.List;

public class SmartOrdersAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() != null &&
            event.getPackageName().toString().equals("com.android.systemui")) return;
        SharedPreferences prefs = getSharedPreferences("SmartOrdersPrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("auto_accept", false)) return;
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText("قبول العرض");
        if (nodes != null && !nodes.isEmpty()) {
            for (AccessibilityNodeInfo node : nodes) {
                if (node.getText() != null && node.getText().toString().contains("قبول العرض")) {
                    Rect bounds = new Rect();
                    node.getBoundsInScreen(bounds);
                    if (bounds.isEmpty() || bounds.width() <= 0) {
                        AccessibilityNodeInfo parent = node.getParent();
                        for (int i = 0; i < 4 && parent != null; i++) {
                            parent.getBoundsInScreen(bounds);
                            if (!bounds.isEmpty() && bounds.width() > 0) break;
                            parent = parent.getParent();
                        }
                    }
                    if (!bounds.isEmpty() && bounds.centerX() > 0 && bounds.centerY() > 0) {
                        GestureDescription.Builder gb = new GestureDescription.Builder();
                        Path p = new Path();
                        p.moveTo(bounds.centerX(), bounds.centerY());
                        gb.addStroke(new GestureDescription.StrokeDescription(p, 0, 100));
                        dispatchGesture(gb.build(), null, null);
                    }
                    node.recycle();
                }
            }
        }
        rootNode.recycle();
    }

    @Override
    public void onInterrupt() {}
}
