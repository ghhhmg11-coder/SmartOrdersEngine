package com.jeenyultimate.helper;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.content.SharedPreferences;

public class FloatingButtonService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private boolean isMenuVisible = false;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_layout, null);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 100;
        windowManager.addView(floatingView, params);
        Button btnFloatingHead = floatingView.findViewById(R.id.btn_floating_head);
        LinearLayout layoutMenu = floatingView.findViewById(R.id.layout_menu);
        Switch switchAutoAccept = floatingView.findViewById(R.id.switch_auto_accept);
        SharedPreferences prefs = getSharedPreferences("SmartOrdersPrefs", MODE_PRIVATE);
        switchAutoAccept.setChecked(prefs.getBoolean("auto_accept", false));
        btnFloatingHead.setOnClickListener(v -> {
            layoutMenu.setVisibility(isMenuVisible ? View.GONE : View.VISIBLE);
            isMenuVisible = !isMenuVisible;
        });
        switchAutoAccept.setOnCheckedChangeListener((b, isChecked) ->
            prefs.edit().putBoolean("auto_accept", isChecked).apply());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) windowManager.removeView(floatingView);
    }
}
