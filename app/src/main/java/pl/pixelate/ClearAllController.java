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

    private final PixelateAccessibilityService service;
    private final Listener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable stepRunnable = this::step;
    private final ScrollProgressTracker scrollProgress = new ScrollProgressTracker();

    private boolean running;
    private boolean taskListVerified;
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
        taskListVerified = false;
        scrollProgress.reset();
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

        if (!taskListVerified) {
            taskListVerified = true;
            if (!OverviewDetector.hasRecentTasks(
                    windows, service.getPackageName(), service)) {
                finishFailure(true);
                return;
            }
        }

        if (timeoutMs > 0 && SystemClock.uptimeMillis() - startedAt >= timeoutMs) {
            finishFailure(false);
            return;
        }

        String progressSignature = OverviewDetector.getOverviewProgressSignature(
                windows, service.getPackageName(), service);
        scrollProgress.observe(progressSignature);

        boolean scrolled = false;
        if (scrollProgress.shouldTryNativeAction()) {
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
            scrollProgress.recordAcceptedNativeAction(progressSignature);
            schedule(135);
        } else {
            scrollProgress.useGestureFromNowOn();
            if (!dispatchSwipeTowardClearAll()) {
                schedule(230);
            }
        }
    }

    private boolean dispatchSwipeTowardClearAll() {
        WindowManager manager = (WindowManager) service.getSystemService(PixelateAccessibilityService.WINDOW_SERVICE);
        Display display = manager == null ? null : manager.getDefaultDisplay();
        if (display == null) {
            return false;
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
        boolean accepted = service.dispatchGesture(
                gesture,
                new android.accessibilityservice.AccessibilityService.GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        if (running) {
                            schedule(35);
                        }
                    }

                    @Override
                    public void onCancelled(GestureDescription gestureDescription) {
                        if (running) {
                            schedule(80);
                        }
                    }
                },
                handler);
        if (accepted) {
            // Defensive watchdog: Android normally invokes one callback, but a lost
            // callback must never leave the button busy until the service restarts.
            schedule(350);
        }
        return accepted;
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
