package pl.pixelate;

import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Display;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.view.WindowManager;

import java.util.List;

final class ClearAllController {
    interface Listener {
        void onClearSucceeded();

        void onClearFailed(boolean noTasks);
    }

    private static final int GESTURE_FALLBACK_AFTER = 20;

    private final PixelateAccessibilityService service;
    private final Listener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable stepRunnable = this::step;

    private boolean running;
    private int attempts;
    private long startedAt;
    private long timeoutMs;

    ClearAllController(PixelateAccessibilityService service, Listener listener) {
        this.service = service;
        this.listener = listener;
    }

    void start() {
        if (running) {
            return;
        }
        running = true;
        attempts = 0;
        startedAt = SystemClock.uptimeMillis();
        timeoutMs = PixelatePreferences.getSearchTimeoutSeconds(service) * 1000L;
        schedule(40);
    }

    boolean isRunning() {
        return running;
    }

    void cancel() {
        running = false;
        handler.removeCallbacks(stepRunnable);
    }

    private void step() {
        if (!running) {
            return;
        }

        List<AccessibilityWindowInfo> windows = service.getWindows();
        AccessibilityNodeInfo clear = OverviewDetector.findClearAllButton(
                windows, service.getPackageName(), service);
        if (clear != null) {
            boolean clicked;
            try {
                clicked = OverviewDetector.performClickWithAncestor(clear);
            } finally {
                clear.recycle();
            }
            if (clicked) {
                running = false;
                handler.removeCallbacks(stepRunnable);
                listener.onClearSucceeded();
                return;
            }
        }

        if (!OverviewDetector.isOverviewVisible(windows, service.getPackageName(), service)) {
            finishFailure(false);
            return;
        }

        if (attempts == 0 && !OverviewDetector.hasRecentTasks(
                windows, service.getPackageName(), service)) {
            finishFailure(true);
            return;
        }

        if (timeoutMs > 0 && SystemClock.uptimeMillis() - startedAt >= timeoutMs) {
            finishFailure(false);
            return;
        }

        attempts++;

        boolean scrolled = false;
        if (attempts < GESTURE_FALLBACK_AFTER) {
            AccessibilityNodeInfo scroller = OverviewDetector.findOverviewScroller(
                    windows, service.getPackageName(), service);
            if (scroller != null) {
                try {
                    scrolled = scroller.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                    if (!scrolled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        scrolled = scroller.performAction(
                                AccessibilityNodeInfo.AccessibilityAction.ACTION_PAGE_RIGHT.getId());
                    }
                    if (!scrolled) {
                        scrolled = scroller.performAction(
                                AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_RIGHT.getId());
                    }
                } finally {
                    scroller.recycle();
                }
            }
        }

        if (scrolled) {
            schedule(135);
        } else {
            dispatchSwipeTowardClearAll();
            schedule(230);
        }
    }

    private void dispatchSwipeTowardClearAll() {
        WindowManager manager = (WindowManager) service.getSystemService(PixelateAccessibilityService.WINDOW_SERVICE);
        Display display = manager == null ? null : manager.getDefaultDisplay();
        if (display == null) {
            return;
        }
        Point size = new Point();
        display.getRealSize(size);
        float y = size.y * 0.46f;
        Path path = new Path();
        // In the LTR Quickstep carousel ClearAllButton is the last page on the right,
        // so a physical right-to-left swipe advances toward it.
        path.moveTo(size.x * 0.84f, y);
        path.lineTo(size.x * 0.18f, y);
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, 190))
                .build();
        service.dispatchGesture(gesture, null, handler);
    }

    private void finishFailure(boolean noTasks) {
        running = false;
        handler.removeCallbacks(stepRunnable);
        listener.onClearFailed(noTasks);
    }

    private void schedule(long delayMs) {
        handler.removeCallbacks(stepRunnable);
        handler.postDelayed(stepRunnable, delayMs);
    }
}
