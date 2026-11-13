package pl.pixelate;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;

public final class PixelateAccessibilityService extends AccessibilityService
        implements OverlayController.Listener, ClearAllController.Listener {
    private static final long OVERVIEW_STABLE_MS = 300;
    private static final long WINDOW_RECOVERY_MS = 2500;
    private static final long WINDOW_RETRY_MS = 250;
    private static final int IDLE_EVENT_TYPES = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            | AccessibilityEvent.TYPE_WINDOWS_CHANGED;
    private static final int OVERVIEW_EVENT_TYPES = IDLE_EVENT_TYPES
            | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
    private static WeakReference<PixelateAccessibilityService> current = new WeakReference<>(null);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable evaluateRunnable = () -> {
        evaluationScheduled = false;
        evaluateOverview();
    };

    private OverlayController overlay;
    private ClearAllController clearer;
    private boolean connected;
    private boolean evaluationScheduled;
    private long evaluationDueAt;
    private long windowRecoveryUntil;
    private boolean eventMonitoringInitialized;
    private boolean monitoringOverviewContent;
    private boolean suppressUntilOverviewExit;
    private long overviewVisibleSince;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        if (!DeviceSupport.isSupportedDevice()) {
            Toast.makeText(this, R.string.unsupported_service_disabled, Toast.LENGTH_LONG).show();
            disableSelf();
            stopSelf();
            return;
        }
        connected = true;
        current = new WeakReference<>(this);
        overlay = new OverlayController(this, this);
        clearer = new ClearAllController(this, this);
        updateEventMonitoring(false);
        windowRecoveryUntil = SystemClock.uptimeMillis() + WINDOW_RECOVERY_MS;
        scheduleEvaluation(250);
        MainActivity.onServiceConnectionChanged();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!connected) {
            return;
        }
        if ((event.getEventType() & IDLE_EVENT_TYPES) != 0
                && !getPackageName().contentEquals(
                        event.getPackageName() == null ? "" : event.getPackageName())) {
            windowRecoveryUntil = SystemClock.uptimeMillis() + WINDOW_RECOVERY_MS;
        }
        scheduleEvaluation(80);
    }

    @Override
    public void onInterrupt() {
        cancelWork();
        updateEventMonitoring(false);
        if (overlay != null) {
            overlay.hide();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (overlay != null) {
            overlay.onConfigurationChanged();
        }
        scheduleEvaluation(200);
    }

    @Override
    public void onDestroy() {
        connected = false;
        if (current.get() == this) {
            current.clear();
        }
        MainActivity.onServiceConnectionChanged();
        cancelWork();
        if (overlay != null) {
            overlay.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onClearAllRequested() {
        if (clearer != null && !clearer.isRunning()) {
            overlay.setBusy(true);
            clearer.start();
        }
    }

    @Override
    public void onClearSucceeded() {
        suppressUntilOverviewExit = true;
        overlay.setBusy(false);
        overlay.hide();
        announceResult(R.string.clear_success);
        scheduleEvaluation(500);
    }

    @Override
    public void onClearFailed(boolean noTasks) {
        overlay.setBusy(false);
        announceResult(noTasks ? R.string.clear_no_tasks : R.string.clear_failure);
        scheduleEvaluation(250);
    }

    private void evaluateOverview() {
        if (!connected || overlay == null) {
            return;
        }
        List<AccessibilityWindowInfo> windows = getWindows();
        boolean overview = OverviewDetector.isOverviewVisible(
                windows, getPackageName(), this);
        updateEventMonitoring(overview);
        boolean tasks = overview && OverviewDetector.hasRecentTasks(
                windows, getPackageName(), this);
        if (!overview) {
            overviewVisibleSince = 0;
            suppressUntilOverviewExit = false;
            cancelClearOperation();
            overlay.hide();
        } else if (suppressUntilOverviewExit) {
            overlay.hide();
        } else {
            long now = SystemClock.uptimeMillis();
            if (overviewVisibleSince == 0) {
                overviewVisibleSince = now;
                overlay.hide();
                scheduleEvaluation(OVERVIEW_STABLE_MS);
            } else {
                long remaining = OVERVIEW_STABLE_MS - (now - overviewVisibleSince);
                if (remaining > 0) {
                    overlay.hide();
                    scheduleEvaluation(remaining);
                } else if (!tasks) {
                    overlay.hide();
                } else {
                    if (clearer == null || !clearer.isRunning()) {
                        overlay.setBusy(false);
                    }
                    overlay.show(PixelatePreferences.shouldFocusClearButton(this));
                }
            }
        }
        // A window event can precede its accessibility tree. Only retry during a
        // short transition window, and only while a launcher (or missing tree)
        // could still become Recents. Ordinary apps never start a polling loop.
        long retryRemaining = windowRecoveryUntil - SystemClock.uptimeMillis();
        if ((!overview || !tasks) && retryRemaining > 0
                && OverviewDetector.mayBeOverviewPending(windows, getPackageName())) {
            scheduleEvaluation(Math.min(WINDOW_RETRY_MS, retryRemaining));
        }
    }

    private void scheduleEvaluation(long delayMs) {
        long dueAt = SystemClock.uptimeMillis() + delayMs;
        if (evaluationScheduled && evaluationDueAt <= dueAt) {
            return;
        }
        handler.removeCallbacks(evaluateRunnable);
        evaluationScheduled = true;
        evaluationDueAt = dueAt;
        handler.postDelayed(evaluateRunnable, delayMs);
    }

    private void updateEventMonitoring(boolean overviewVisible) {
        if (eventMonitoringInitialized && monitoringOverviewContent == overviewVisible) {
            return;
        }
        AccessibilityServiceInfo info = getServiceInfo();
        if (info == null) {
            return;
        }
        eventMonitoringInitialized = true;
        monitoringOverviewContent = overviewVisible;
        info.eventTypes = overviewVisible ? OVERVIEW_EVENT_TYPES : IDLE_EVENT_TYPES;
        setServiceInfo(info);
    }

    private void cancelWork() {
        handler.removeCallbacks(evaluateRunnable);
        evaluationScheduled = false;
        windowRecoveryUntil = 0;
        cancelClearOperation();
    }

    private void cancelClearOperation() {
        if (clearer != null) {
            clearer.cancel();
        }
        if (overlay != null) {
            overlay.setBusy(false);
        }
    }

    private void announceResult(int stringId) {
        if (overlay != null) {
            overlay.announce(getString(stringId));
        }
        Toast.makeText(this, stringId, Toast.LENGTH_SHORT).show();
    }

    static boolean isConnected() {
        PixelateAccessibilityService service = current.get();
        return service != null && service.connected;
    }

    static boolean openRecentsForTest() {
        PixelateAccessibilityService service = current.get();
        if (service == null || !service.connected) {
            return false;
        }
        boolean accepted = service.performGlobalAction(GLOBAL_ACTION_RECENTS);
        if (accepted) {
            service.scheduleEvaluation(500);
        }
        return accepted;
    }
}
