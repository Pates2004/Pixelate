package pl.pixelate;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.inputmethod.EditorInfo;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public final class MainActivity extends Activity {
    private static final long TIMEOUT_AUTO_SAVE_DELAY_MS = 900;

    private final Handler preferencesHandler = new Handler(Looper.getMainLooper());
    private final Runnable timeoutSaveRunnable = () -> persistTimeout(false);
    private final Runnable themeApplyRunnable = this::recreate;
    private TextView statusText;
    private View statusCard;
    private Button testButton;
    private Switch focusButtonSwitch;
    private EditText timeoutInput;
    private RadioButton themeSystemButton;
    private RadioButton themeLightButton;
    private RadioButton themeDarkButton;
    private boolean dark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dark = PixelatePreferences.shouldUseDarkTheme(this);
        setTheme(dark ? R.style.AppThemeDark : R.style.AppThemeLight);
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        if (!DeviceSupport.isSupportedDevice()) {
            setContentView(buildUnsupportedContent());
            return;
        }
        setContentView(buildContent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    @Override
    protected void onPause() {
        persistTimeout(false);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        preferencesHandler.removeCallbacks(timeoutSaveRunnable);
        preferencesHandler.removeCallbacks(themeApplyRunnable);
        super.onDestroy();
    }

    private View buildContent() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(dark ? Color.rgb(8, 13, 24) : Color.rgb(248, 250, 252));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(24), dp(28), dp(24), dp(40));
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.pixelate_smile);
        icon.setContentDescription(getString(R.string.icon_description));
        icon.setPadding(dp(12), dp(12), dp(12), dp(12));
        icon.setBackground(rounded(Color.rgb(18, 5, 43), 24));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(120), dp(120));
        iconParams.bottomMargin = dp(18);
        content.addView(icon, iconParams);

        TextView title = text(getString(R.string.screen_title), 32, true);
        title.setGravity(Gravity.CENTER);
        content.addView(title, matchWrap(dp(2)));

        TextView subtitle = text(getString(R.string.screen_subtitle), 17, false);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTextColor(dark ? Color.rgb(203, 213, 225) : Color.rgb(71, 85, 105));
        content.addView(subtitle, matchWrap(dp(14)));

        TextView detectedModel = text(getString(R.string.device_model, Build.MODEL), 16, true);
        detectedModel.setGravity(Gravity.CENTER);
        detectedModel.setTextColor(dark ? Color.rgb(103, 232, 249) : Color.rgb(8, 145, 178));
        content.addView(detectedModel, matchWrap(dp(28)));

        statusCard = card();
        LinearLayout statusContent = (LinearLayout) statusCard;
        statusContent.addView(sectionHeading(getString(R.string.status_heading)), matchWrap(dp(10)));
        statusText = text("", 17, true);
        statusText.setGravity(Gravity.CENTER_VERTICAL);
        statusContent.addView(statusText, matchWrap(dp(18)));

        Button settingsButton = primaryButton(getString(R.string.enable_service));
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
        statusContent.addView(settingsButton, matchWrap(dp(12)));

        testButton = secondaryButton(getString(R.string.test_button));
        testButton.setOnClickListener(v -> testRecents());
        statusContent.addView(testButton, matchWrap(0));
        content.addView(statusCard, matchWrap(dp(18)));

        LinearLayout preferencesCard = (LinearLayout) card();
        preferencesCard.addView(
                sectionHeading(getString(R.string.settings_heading)), matchWrap(dp(12)));
        preferencesCard.addView(body(getString(R.string.settings_intro)), matchWrap(dp(12)));

        TextView themeHeading = text(getString(R.string.theme_heading), 17, true);
        preferencesCard.addView(themeHeading, matchWrap(dp(4)));

        RadioGroup themeGroup = new RadioGroup(this);
        themeGroup.setOrientation(RadioGroup.VERTICAL);
        themeGroup.setContentDescription(getString(R.string.theme_heading));
        themeSystemButton = themeOption(R.string.theme_system);
        themeLightButton = themeOption(R.string.theme_light);
        themeDarkButton = themeOption(R.string.theme_dark);
        themeGroup.addView(themeSystemButton);
        themeGroup.addView(themeLightButton);
        themeGroup.addView(themeDarkButton);
        String selectedTheme = PixelatePreferences.getAppTheme(this);
        if (PixelatePreferences.THEME_LIGHT.equals(selectedTheme)) {
            themeLightButton.setChecked(true);
        } else if (PixelatePreferences.THEME_DARK.equals(selectedTheme)) {
            themeDarkButton.setChecked(true);
        } else {
            themeSystemButton.setChecked(true);
        }
        themeGroup.setOnCheckedChangeListener((group, checkedId) -> saveThemeAndApply());
        preferencesCard.addView(themeGroup, matchWrap(dp(4)));
        preferencesCard.addView(body(getString(R.string.theme_explanation)), matchWrap(dp(18)));

        focusButtonSwitch = new Switch(this);
        focusButtonSwitch.setText(R.string.focus_button_setting);
        focusButtonSwitch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        focusButtonSwitch.setTextColor(dark ? Color.WHITE : Color.rgb(15, 23, 42));
        focusButtonSwitch.setMinHeight(dp(56));
        focusButtonSwitch.setChecked(PixelatePreferences.shouldFocusClearButton(this));
        focusButtonSwitch.setOnCheckedChangeListener((button, checked) ->
                PixelatePreferences.setFocusClearButton(this, checked));
        preferencesCard.addView(focusButtonSwitch, matchWrap(dp(6)));
        preferencesCard.addView(
                body(getString(R.string.focus_button_explanation)), matchWrap(dp(18)));

        TextView timeoutLabel = text(getString(R.string.timeout_label), 17, true);
        timeoutInput = new EditText(this);
        timeoutInput.setId(View.generateViewId());
        timeoutLabel.setLabelFor(timeoutInput.getId());
        timeoutInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        timeoutInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        timeoutInput.setSingleLine(true);
        timeoutInput.setSelectAllOnFocus(true);
        timeoutInput.setText(String.valueOf(
                PixelatePreferences.getSearchTimeoutSeconds(this)));
        timeoutInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        timeoutInput.setContentDescription(getString(R.string.timeout_input_description));
        timeoutInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence value, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence value, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable value) {
                preferencesHandler.removeCallbacks(timeoutSaveRunnable);
                preferencesHandler.postDelayed(
                        timeoutSaveRunnable, TIMEOUT_AUTO_SAVE_DELAY_MS);
            }
        });
        timeoutInput.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                persistTimeout(true);
            }
        });
        timeoutInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                persistTimeout(true);
            }
            return false;
        });
        preferencesCard.addView(timeoutLabel, matchWrap(dp(4)));
        preferencesCard.addView(timeoutInput, matchWrap(dp(6)));
        preferencesCard.addView(body(getString(R.string.timeout_explanation)), matchWrap(dp(18)));
        preferencesCard.addView(body(getString(R.string.battery_explanation)), matchWrap(0));
        content.addView(preferencesCard, matchWrap(dp(18)));

        LinearLayout howCard = (LinearLayout) card();
        howCard.addView(sectionHeading(getString(R.string.how_it_works_heading)), matchWrap(dp(12)));
        howCard.addView(body(getString(R.string.how_it_works_body)), matchWrap(0));
        content.addView(howCard, matchWrap(dp(18)));

        LinearLayout privacyCard = (LinearLayout) card();
        privacyCard.addView(sectionHeading(getString(R.string.privacy_heading)), matchWrap(dp(12)));
        privacyCard.addView(body(getString(R.string.privacy_body)), matchWrap(0));
        content.addView(privacyCard, matchWrap(0));

        return scroll;
    }

    private View buildUnsupportedContent() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(dark ? Color.rgb(8, 13, 24) : Color.rgb(248, 250, 252));
        scroll.setAccessibilityPaneTitle(getString(R.string.unsupported_heading));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(dp(28), dp(40), dp(28), dp(40));
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.pixelate_smile);
        icon.setContentDescription(getString(R.string.icon_description));
        icon.setPadding(dp(12), dp(12), dp(12), dp(12));
        icon.setBackground(rounded(Color.rgb(18, 5, 43), 24));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(120), dp(120));
        iconParams.bottomMargin = dp(24);
        content.addView(icon, iconParams);

        TextView heading = text(getString(R.string.unsupported_heading), 26, true);
        heading.setGravity(Gravity.CENTER);
        heading.setAccessibilityHeading(true);
        content.addView(heading, matchWrap(dp(18)));

        TextView message = body(getString(
                R.string.unsupported_message, Build.MANUFACTURER, Build.MODEL));
        message.setGravity(Gravity.CENTER);
        content.addView(message, matchWrap(dp(24)));

        Button close = primaryButton(getString(R.string.close_app));
        close.setOnClickListener(v -> finishAndRemoveTask());
        content.addView(close, matchWrap(0));
        return scroll;
    }

    private void updateStatus() {
        if (statusText == null) {
            return;
        }
        boolean enabled = isPixelateEnabled(this);
        boolean connected = PixelateAccessibilityService.isConnected();
        if (connected) {
            statusText.setText(R.string.status_connected);
            statusText.setTextColor(dark ? Color.rgb(103, 232, 249) : Color.rgb(8, 145, 178));
            statusCard.setContentDescription(getString(R.string.status_connected));
        } else if (enabled) {
            statusText.setText(R.string.status_enabled);
            statusText.setTextColor(dark ? Color.rgb(253, 224, 71) : Color.rgb(161, 98, 7));
            statusCard.setContentDescription(getString(R.string.status_enabled));
        } else {
            statusText.setText(R.string.status_disabled);
            statusText.setTextColor(dark ? Color.rgb(252, 165, 165) : Color.rgb(185, 28, 28));
            statusCard.setContentDescription(getString(R.string.status_disabled));
        }
        testButton.setEnabled(connected);
        testButton.setAlpha(connected ? 1f : 0.55f);
    }

    private void testRecents() {
        if (!PixelateAccessibilityService.openRecentsForTest()) {
            Toast.makeText(this, R.string.test_requires_service, Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }
    }

    private void saveThemeAndApply() {
        String oldTheme = PixelatePreferences.getAppTheme(this);
        String newTheme = selectedTheme();
        if (oldTheme.equals(newTheme)) {
            return;
        }
        PixelatePreferences.setAppTheme(this, newTheme);
        preferencesHandler.removeCallbacks(themeApplyRunnable);
        preferencesHandler.postDelayed(themeApplyRunnable, 250);
    }

    private void persistTimeout(boolean showEmptyError) {
        if (timeoutInput == null) {
            return;
        }
        preferencesHandler.removeCallbacks(timeoutSaveRunnable);
        String value = timeoutInput.getText().toString().trim();
        if (value.isEmpty()) {
            if (showEmptyError) {
                timeoutInput.setError(getString(R.string.timeout_invalid));
            }
            return;
        }
        int timeout;
        try {
            timeout = Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            timeoutInput.setError(getString(R.string.timeout_invalid));
            return;
        }
        if (timeout < 0 || timeout > PixelatePreferences.MAX_SEARCH_TIMEOUT_SECONDS) {
            timeoutInput.setError(getString(R.string.timeout_range));
            return;
        }
        PixelatePreferences.setSearchTimeoutSeconds(this, timeout);
        timeoutInput.setError(null);
    }

    static boolean isPixelateEnabled(Context context) {
        AccessibilityManager manager =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager != null) {
            List<android.accessibilityservice.AccessibilityServiceInfo> services =
                    manager.getEnabledAccessibilityServiceList(
                            AccessibilityServiceInfoCompat.ALL_FEEDBACK_TYPES);
            String expected = new ComponentName(context, PixelateAccessibilityService.class)
                    .flattenToString();
            for (android.accessibilityservice.AccessibilityServiceInfo service : services) {
                if (service.getResolveInfo() != null && service.getResolveInfo().serviceInfo != null) {
                    ComponentName component = new ComponentName(
                            service.getResolveInfo().serviceInfo.packageName,
                            service.getResolveInfo().serviceInfo.name);
                    if (expected.equalsIgnoreCase(component.flattenToString())) {
                        return true;
                    }
                }
            }
        }

        String enabled = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (TextUtils.isEmpty(enabled)) {
            return false;
        }
        String expected = new ComponentName(context, PixelateAccessibilityService.class)
                .flattenToString();
        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabled);
        while (splitter.hasNext()) {
            if (expected.equalsIgnoreCase(splitter.next())) {
                return true;
            }
        }
        return false;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(20), dp(20), dp(20));
        card.setBackground(rounded(
                dark ? Color.rgb(21, 30, 48) : Color.WHITE, 20));
        card.setElevation(dp(2));
        return card;
    }

    private TextView sectionHeading(String value) {
        TextView view = text(value, 20, true);
        view.setAccessibilityHeading(true);
        return view;
    }

    private TextView body(String value) {
        TextView view = text(value, 16, false);
        view.setTextColor(dark ? Color.rgb(203, 213, 225) : Color.rgb(51, 65, 85));
        view.setLineSpacing(0, 1.18f);
        return view;
    }

    private TextView text(String value, float sp, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        view.setTextColor(dark ? Color.WHITE : Color.rgb(15, 23, 42));
        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return view;
    }

    private Button primaryButton(String value) {
        Button button = baseButton(value);
        button.setTextColor(Color.WHITE);
        button.setBackground(rounded(Color.rgb(109, 40, 217), 16));
        return button;
    }

    private Button secondaryButton(String value) {
        Button button = baseButton(value);
        button.setTextColor(dark ? Color.rgb(103, 232, 249) : Color.rgb(8, 145, 178));
        button.setBackground(rounded(
                dark ? Color.rgb(30, 41, 59) : Color.rgb(236, 254, 255), 16));
        return button;
    }

    private RadioButton themeOption(int stringId) {
        RadioButton button = new RadioButton(this);
        button.setId(View.generateViewId());
        button.setText(stringId);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        button.setTextColor(dark ? Color.WHITE : Color.rgb(15, 23, 42));
        button.setMinHeight(dp(48));
        return button;
    }

    private String selectedTheme() {
        if (themeLightButton.isChecked()) {
            return PixelatePreferences.THEME_LIGHT;
        }
        if (themeDarkButton.isChecked()) {
            return PixelatePreferences.THEME_DARK;
        }
        return PixelatePreferences.THEME_SYSTEM;
    }

    private Button baseButton(String value) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(56));
        button.setPadding(dp(18), dp(10), dp(18), dp(10));
        return button;
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private LinearLayout.LayoutParams matchWrap(int bottomMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = bottomMargin;
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    /** Keeps the framework constant in one place without an AndroidX dependency. */
    private static final class AccessibilityServiceInfoCompat {
        static final int ALL_FEEDBACK_TYPES = -1;
    }
}
