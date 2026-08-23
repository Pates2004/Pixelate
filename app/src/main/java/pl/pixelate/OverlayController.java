package pl.pixelate;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;

final class OverlayController {
    // Quickstep and screen readers both restore their own focus while the Recents
    // transition is settling. Request focus after that hand-off, otherwise TalkBack
    // can immediately move it back to the current task card.
    private static final long SCREEN_READER_FOCUS_DELAY_MS = 900;

    interface Listener {
        void onClearAllRequested();
    }

    private final PixelateAccessibilityService service;
    private final Listener listener;
    private final WindowManager windowManager;
    private final Runnable screenReaderFocusRunnable = this::focusForScreenReader;

    private FrameLayout root;
    private Button button;
    private WindowManager.LayoutParams params;
    private boolean shown;

    OverlayController(PixelateAccessibilityService service, Listener listener) {
        this.service = service;
        this.listener = listener;
        this.windowManager = (WindowManager) service.getSystemService(PixelateAccessibilityService.WINDOW_SERVICE);
        createView();
    }

    void show(boolean focusForScreenReader) {
        if (shown || windowManager == null) {
            return;
        }
        try {
            updateVerticalOffset();
            windowManager.addView(root, params);
            shown = true;
            if (focusForScreenReader) {
                button.removeCallbacks(screenReaderFocusRunnable);
                button.postDelayed(screenReaderFocusRunnable, SCREEN_READER_FOCUS_DELAY_MS);
            }
        } catch (RuntimeException ignored) {
            shown = false;
        }
    }

    void hide() {
        if (button != null) {
            button.removeCallbacks(screenReaderFocusRunnable);
        }
        if (!shown || windowManager == null) {
            return;
        }
        try {
            windowManager.removeView(root);
        } catch (RuntimeException ignored) {
            // The window may already have been removed by the system.
        } finally {
            shown = false;
        }
    }

    void setBusy(boolean busy) {
        button.setEnabled(!busy);
        button.setText(busy ? R.string.overlay_clearing : R.string.overlay_clear_all);
        button.setAlpha(busy ? 0.82f : 1f);
        if (busy) {
            button.announceForAccessibility(service.getString(R.string.overlay_clearing));
        }
    }

    void announce(String message) {
        if (button != null) {
            button.announceForAccessibility(message);
        }
    }

    void onConfigurationChanged() {
        if (!shown || windowManager == null) {
            return;
        }
        updateVerticalOffset();
        try {
            windowManager.updateViewLayout(root, params);
        } catch (RuntimeException ignored) {
            // A later accessibility event will recreate the overlay if necessary.
        }
    }

    void destroy() {
        hide();
        button = null;
        root = null;
    }

    @SuppressLint("AccessibilityFocus")
    private void focusForScreenReader() {
        if (!shown || button == null || !button.isShown()) {
            return;
        }
        button.performAccessibilityAction(
                AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
    }

    private void createView() {
        root = new FrameLayout(service);
        root.setPadding(dp(2), dp(2), dp(2), dp(2));
        root.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);

        button = new Button(service);
        button.setText(R.string.overlay_clear_all);
        button.setContentDescription(service.getString(R.string.overlay_button_description));
        button.setTextColor(Color.WHITE);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setMinWidth(dp(200));
        button.setMinHeight(dp(48));
        button.setPadding(dp(18), dp(4), dp(18), dp(4));
        button.setBackground(buttonBackground());
        button.setElevation(dp(10));
        button.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        button.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setClassName(Button.class.getName());
                info.setText(service.getString(host.isEnabled()
                        ? R.string.overlay_clear_all : R.string.overlay_clearing));
            }
        });
        button.setAccessibilityPaneTitle(service.getString(R.string.overlay_window_title));

        Drawable icon = service.getDrawable(R.drawable.pixelate_smile);
        if (icon != null) {
            icon.setBounds(0, 0, dp(24), dp(24));
            button.setCompoundDrawablePadding(dp(8));
            button.setCompoundDrawables(icon, null, null, null);
        }

        button.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            listener.onClearAllRequested();
        });

        FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        root.addView(button, buttonParams);

        int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                flags,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.setTitle(service.getString(R.string.overlay_window_title));
        updateVerticalOffset();
    }

    private Drawable buttonBackground() {
        GradientDrawable background = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.rgb(109, 40, 217), Color.rgb(8, 145, 178)});
        background.setCornerRadius(dp(20));
        background.setStroke(dp(2), Color.rgb(103, 232, 249));
        return background;
    }

    @SuppressLint({"DiscouragedApi", "InternalInsetResource"})
    private void updateVerticalOffset() {
        boolean landscape = service.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
        int bottomInset = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && windowManager != null) {
            WindowMetrics metrics = windowManager.getCurrentWindowMetrics();
            Insets insets = metrics.getWindowInsets().getInsetsIgnoringVisibility(
                    WindowInsets.Type.navigationBars());
            bottomInset = insets.bottom;
        } else if (!landscape) {
            int id = service.getResources().getIdentifier(
                    "navigation_bar_height", "dimen", "android");
            if (id != 0) {
                bottomInset = service.getResources().getDimensionPixelSize(id);
            }
        }
        params.y = Math.max(bottomInset, landscape ? dp(8) : 0) + dp(2);
    }

    private int dp(int value) {
        return Math.round(value * service.getResources().getDisplayMetrics().density);
    }
}
