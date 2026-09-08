package pl.pixelate.testlauncher;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.LinearLayout;

/** Controlled launcher-shaped UI for testing the real Pixelate service. Never shipped. */
public class FixtureActivity extends Activity {
    private boolean revealed;
    private int reveals;
    private int scrolls;
    private String mode;
    private Button clear;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        mode = getIntent().getStringExtra("mode");
        setTitle("Recent apps");
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setId(R.id.overview_panel);
        root.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setClassName("android.widget.ListView");
                info.setScrollable(true);
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
            }
            @Override public boolean performAccessibilityAction(View host, int action, Bundle args) {
                if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
                    scrolls++;
                    if (!"stuck".equals(mode)) reveal();
                    Log.i("PixelateFixture", "SCROLL mode=" + mode + " count=" + scrolls);
                    return true;
                }
                return super.performAccessibilityAction(host, action, args);
            }
        });
        Button task = new Button(this);
        task.setId(R.id.task);
        task.setText("Fixture task");
        root.addView(task, new LinearLayout.LayoutParams(-1, 500));
        clear = new Button(this);
        clear.setId(R.id.clear_all);
        clear.setText("Clear all");
        clear.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setVisibleToUser(revealed);
                info.setClickable(revealed);
                if (!revealed) info.removeAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);
                if ("unsupported".equals(mode)) info.removeAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SHOW_ON_SCREEN);
            }
            @Override public boolean performAccessibilityAction(View host, int action, Bundle args) {
                if (action == AccessibilityNodeInfo.AccessibilityAction.ACTION_SHOW_ON_SCREEN.getId()) {
                    reveals++;
                    Log.i("PixelateFixture", "REVEAL mode=" + mode + " count=" + reveals);
                    if ("reject".equals(mode)) return false;
                    if ("success".equals(mode)) reveal();
                    return true;
                }
                return super.performAccessibilityAction(host, action, args);
            }
        });
        clear.setOnClickListener(v -> {
            Log.i("PixelateFixture", "CLICK mode=" + mode + " reveals=" + reveals + " scrolls=" + scrolls + " visible=" + revealed);
            finishAndRemoveTask();
        });
        root.addView(clear, new LinearLayout.LayoutParams(-1, 150));
        setContentView(root);
    }

    private void reveal() {
        revealed = true;
        clear.sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }
}
