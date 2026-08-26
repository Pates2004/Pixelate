package pl.pixelate;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public final class PixelateAccessibilityService extends AccessibilityService
        implements OverlayController.Listener, ClearAllController.Listener {
    private static final long OVERVIEW_STABLE_MS = 300;
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
        scheduleEvaluation(250);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!connected) {
            return;
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
        boolean overview = OverviewDetector.isOverviewVisible(
                getWindows(), getPackageName(), this);
        updateEventMonitoring(overview);
        boolean tasks = overview && OverviewDetector.hasRecentTasks(
                getWindows(), getPackageName(), this);
        if (!overview) {
            overviewVisibleSince = 0;
            suppressUntilOverviewExit = false;
            if (clearer != null) {
                clearer.cancel();
            }
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
                    overlay.show(PixelatePreferences.shouldFocusClearButton(this));
                }
            }
        }
    }

    private void scheduleEvaluation(long delayMs) {
        if (evaluationScheduled) {
            return;
        }
        evaluationScheduled = true;
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
        if (clearer != null) {
            clearer.cancel();
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
