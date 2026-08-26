package com.andrewvox.mega3splitrescue;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;

import java.lang.ref.WeakReference;

public final class SplitAccessibilityService extends AccessibilityService {
    private static WeakReference<SplitAccessibilityService> current = new WeakReference<>(null);

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            info.eventTypes = 0;
            setServiceInfo(info);
        }
        current = new WeakReference<>(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Deliberately empty: this service does not inspect screen content or events.
    }

    @Override
    public void onInterrupt() {
        // No continuous feedback to interrupt.
    }

    @Override
    public void onDestroy() {
        current.clear();
        super.onDestroy();
    }

    public static boolean isReady() {
        return current.get() != null;
    }

    public static boolean toggleSplitScreen() {
        SplitAccessibilityService service = current.get();
        return service != null
                && service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN);
    }
}
