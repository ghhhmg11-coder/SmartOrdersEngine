package com.jeenyultimate.helper;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            startActivityForResult(
                new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())), 101);
        } else {
            startService(new Intent(this, FloatingButtonService.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101 &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            Settings.canDrawOverlays(this)) {
            startService(new Intent(this, FloatingButtonService.class));
        }
    }
}
