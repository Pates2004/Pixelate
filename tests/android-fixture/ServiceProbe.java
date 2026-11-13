import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.UiAutomation;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

/** Shell-only observer; does not suppress the actual screen reader services. */
@SuppressWarnings("deprecation")
public final class ServiceProbe {
    private static UiAutomation automation;

    private static AccessibilityNodeInfo findButton(AccessibilityNodeInfo node) {
        if (node == null) return null;
        if ("pl.pixelate".contentEquals(node.getPackageName() == null ? "" : node.getPackageName())
                && "android.widget.Button".contentEquals(node.getClassName())) {
            return node;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo result = findButton(node.getChild(i));
            if (result != null) { node.recycle(); return result; }
        }
        node.recycle();
        return null;
    }

    private static AccessibilityNodeInfo overlayButton() {
        for (AccessibilityWindowInfo window : automation.getWindows()) {
            if (window.getType() != AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY) continue;
            AccessibilityNodeInfo result = findButton(window.getRoot());
            if (result != null) return result;
        }
        return null;
    }

    private static AccessibilityNodeInfo findEditor(AccessibilityNodeInfo node) {
        if (node == null) return null;
        if ("android.widget.EditText".contentEquals(node.getClassName())) return node;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo result = findEditor(node.getChild(i));
            if (result != null) { node.recycle(); return result; }
        }
        node.recycle();
        return null;
    }

    private static void describe(AccessibilityNodeInfo node) {
        if (node == null) return;
        if (node.getText() != null) System.out.println("TEXT enabled=" + node.isEnabled() + " " + node.getText());
        for (int i = 0; i < node.getChildCount(); i++) describe(node.getChild(i));
        node.recycle();
    }

    private static boolean isOverlayPresent() {
        AccessibilityNodeInfo button = overlayButton();
        if (button == null) return false;
        button.recycle();
        return true;
    }

    private static boolean isRecentsTree(AccessibilityNodeInfo node) {
        if (node == null) return false;
        try {
            String id = node.getViewIdResourceName();
            if (node.isVisibleToUser() && id != null
                    && (id.endsWith(":id/overview_panel") || id.endsWith(":id/recents_view"))) return true;
            for (int i = 0; i < node.getChildCount(); i++) if (isRecentsTree(node.getChild(i))) return true;
            return false;
        } finally { node.recycle(); }
    }

    private static boolean isRecentsPresent() {
        for (AccessibilityWindowInfo window : automation.getWindows()) {
            if (window.getType() == AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY) continue;
            if ((window.isActive() || window.isFocused()) && isRecentsTree(window.getRoot())) return true;
        }
        return false;
    }

    private static void inspectTargets(AccessibilityNodeInfo node, int scrollAction) {
        if (node == null) return;
        try {
            String id = node.getViewIdResourceName();
            if (id != null && (id.contains("clear") || id.endsWith(":id/overview_panel"))) {
                System.out.println("TARGET " + node);
                if (scrollAction != 0 && id.endsWith(":id/overview_panel")) {
                    System.out.println("SCROLL action=" + scrollAction + " accepted=" + node.performAction(scrollAction));
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) inspectTargets(node.getChild(i), scrollAction);
        } finally { node.recycle(); }
    }

    private static AccessibilityNodeInfo awaitEnabled(long timeout) {
        long end = SystemClock.uptimeMillis() + timeout;
        do {
            AccessibilityNodeInfo button = overlayButton();
            if (button != null) {
                if (button.isEnabled()) return button;
                button.recycle();
            }
            SystemClock.sleep(40);
        } while (SystemClock.uptimeMillis() < end);
        throw new IllegalStateException("Pixelate button missing or disabled");
    }

    public static void main(String[] args) throws Exception {
        Looper.prepareMainLooper();
        HandlerThread thread = new HandlerThread("PixelateServiceProbe");
        thread.start();
        Class<?> connection = Class.forName("android.app.IUiAutomationConnection");
        automation = (UiAutomation) UiAutomation.class.getConstructor(Looper.class, connection)
                .newInstance(thread.getLooper(), Class.forName("android.app.UiAutomationConnection")
                        .getConstructor().newInstance());
        UiAutomation.class.getMethod("connect", int.class).invoke(
                automation, UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        AccessibilityServiceInfo info = automation.getServiceInfo();
        info.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
                | AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
                | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        automation.setServiceInfo(info);
        try {
            String mode = args.length == 0 ? "inspect" : args[0];
            if ("set-timeout".equals(mode)) {
                AccessibilityNodeInfo editor = null;
                long end = SystemClock.uptimeMillis() + 3000;
                do {
                    for (AccessibilityWindowInfo window : automation.getWindows()) {
                        if (window.getType() != AccessibilityWindowInfo.TYPE_APPLICATION) continue;
                        AccessibilityNodeInfo root = window.getRoot();
                        if (root != null && "pl.pixelate".contentEquals(root.getPackageName())) editor = findEditor(root);
                        else if (root != null) root.recycle();
                        if (editor != null) break;
                    }
                    if (editor == null) SystemClock.sleep(50);
                } while (editor == null && SystemClock.uptimeMillis() < end);
                if (editor == null) throw new IllegalStateException("Timeout editor missing");
                android.os.Bundle values = new android.os.Bundle();
                values.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, args[1]);
                boolean accepted = editor.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, values);
                editor.recycle();
                if (!accepted) throw new IllegalStateException("Timeout edit rejected");
                SystemClock.sleep(1100);
                System.out.println("PASS timeout edited through app UI");
            } else if ("click-text".equals(mode)) {
                boolean clicked = false;
                for (AccessibilityWindowInfo window : automation.getWindows()) {
                    if (!window.isActive() && !window.isFocused()) continue;
                    AccessibilityNodeInfo root = window.getRoot();
                    if (root == null) continue;
                    for (AccessibilityNodeInfo node : root.findAccessibilityNodeInfosByText(args[1])) {
                        if (!clicked && node.isClickable()) clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        node.recycle();
                    }
                    root.recycle();
                }
                if (!clicked) throw new IllegalStateException("Requested text control not found");
                System.out.println("PASS text control activated");
            } else if ("targets".equals(mode) || "scroll-forward".equals(mode) || "scroll-backward".equals(mode)) {
                int action = "scroll-forward".equals(mode) ? AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                        : "scroll-backward".equals(mode) ? AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD : 0;
                for (AccessibilityWindowInfo window : automation.getWindows()) {
                    if (window.isActive() || window.isFocused()) inspectTargets(window.getRoot(), action);
                }
            } else if ("recents".equals(mode)) {
                long end = SystemClock.uptimeMillis() + 3000;
                while (!isRecentsPresent() && SystemClock.uptimeMillis() < end) SystemClock.sleep(50);
                if (!isRecentsPresent()) throw new IllegalStateException("Test precondition: Recents did not open");
                System.out.println("PASS actual Recents tree present");
            } else if ("activity".equals(mode)) {
                for (AccessibilityWindowInfo window : automation.getWindows()) {
                    AccessibilityNodeInfo root = window.getRoot();
                    if (root != null && "pl.pixelate".contentEquals(root.getPackageName())) describe(root);
                    else if (root != null) root.recycle();
                }
            } else if ("empty".equals(mode)) {
                SystemClock.sleep(1000);
                if (isOverlayPresent()) throw new IllegalStateException("Unexpected overlay");
                System.out.println("PASS empty overlay=false");
            } else if ("await".equals(mode)) {
                AccessibilityNodeInfo button = awaitEnabled(5000);
                System.out.println("PASS await enabled=" + button.isEnabled()
                        + " focus=" + button.isAccessibilityFocused()
                        + " text=" + button.getText() + " label=" + button.getContentDescription());
                button.recycle();
            } else if ("inspect".equals(mode)) {
                System.out.println("overlay=" + isOverlayPresent());
            } else {
                AccessibilityNodeInfo button = awaitEnabled(5000);
                long start = SystemClock.uptimeMillis();
                boolean clicked = button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                button.recycle();
                if (!clicked) throw new IllegalStateException("Click rejected");
                if ("busy".equals(mode) || "unlimited".equals(mode)) {
                    SystemClock.sleep("unlimited".equals(mode) ? 1500 : 100);
                    button = overlayButton();
                    if (button == null || button.isEnabled()) throw new IllegalStateException("Operation not busy");
                    System.out.println("BUSY text=" + button.getText() + " label=" + button.getContentDescription());
                    button.recycle();
                    automation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    SystemClock.sleep(500);
                    if (isOverlayPresent()) throw new IllegalStateException("Overlay remained after Back");
                    System.out.println("PASS back cancelled busy operation");
                } else if ("back".equals(mode)) {
                    SystemClock.sleep(args.length > 1 ? Long.parseLong(args[1]) : 0);
                    automation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    SystemClock.sleep(700);
                    automation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                    button = awaitEnabled(5000);
                    button.recycle();
                    System.out.println("PASS back buttonRecovered=true");
                } else if ("timeout".equals(mode)) {
                    SystemClock.sleep(200);
                    button = awaitEnabled(5000);
                    button.recycle();
                    System.out.println("PASS timeout recoveredMs=" + (SystemClock.uptimeMillis() - start));
                } else {
                    long end = start + 25000;
                    while (isOverlayPresent() && SystemClock.uptimeMillis() < end) SystemClock.sleep(30);
                    if (isOverlayPresent()) throw new IllegalStateException("Clear did not finish");
                    System.out.println("PASS clear elapsedMs=" + (SystemClock.uptimeMillis() - start));
                }
            }
        } finally {
            UiAutomation.class.getMethod("disconnect").invoke(automation);
            thread.quitSafely();
        }
        System.exit(0);
    }
}
