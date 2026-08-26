package pl.pixelate;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

final class PixelatePreferences {
    static final int DEFAULT_SEARCH_TIMEOUT_SECONDS = 20;
    static final int MAX_SEARCH_TIMEOUT_SECONDS = 300;
    static final String THEME_SYSTEM = "system";
    static final String THEME_LIGHT = "light";
    static final String THEME_DARK = "dark";

    private static final String FILE_NAME = "pixelate_preferences";
    private static final String KEY_FOCUS_CLEAR_BUTTON = "focus_clear_button";
    private static final String KEY_SEARCH_TIMEOUT_SECONDS = "search_timeout_seconds";
    private static final String KEY_APP_THEME = "app_theme";

    private PixelatePreferences() {
    }

    static boolean shouldFocusClearButton(Context context) {
        return preferences(context).getBoolean(KEY_FOCUS_CLEAR_BUTTON, true);
    }

    static int getSearchTimeoutSeconds(Context context) {
        int value = preferences(context).getInt(
                KEY_SEARCH_TIMEOUT_SECONDS, DEFAULT_SEARCH_TIMEOUT_SECONDS);
        return Math.max(0, Math.min(MAX_SEARCH_TIMEOUT_SECONDS, value));
    }

    static String getAppTheme(Context context) {
        String value = preferences(context).getString(KEY_APP_THEME, THEME_SYSTEM);
        if (THEME_LIGHT.equals(value) || THEME_DARK.equals(value)) {
            return value;
        }
        return THEME_SYSTEM;
    }

    static boolean shouldUseDarkTheme(Context context) {
        String theme = getAppTheme(context);
        if (THEME_DARK.equals(theme)) {
            return true;
        }
        if (THEME_LIGHT.equals(theme)) {
            return false;
        }
        return (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    static void setFocusClearButton(Context context, boolean focusClearButton) {
        preferences(context).edit()
                .putBoolean(KEY_FOCUS_CLEAR_BUTTON, focusClearButton)
                .apply();
    }

    static void setSearchTimeoutSeconds(Context context, int timeoutSeconds) {
        int safeTimeout = Math.max(0, Math.min(MAX_SEARCH_TIMEOUT_SECONDS, timeoutSeconds));
        preferences(context).edit()
                .putInt(KEY_SEARCH_TIMEOUT_SECONDS, safeTimeout)
                .apply();
    }

    static void setAppTheme(Context context, String appTheme) {
        String safeTheme = THEME_LIGHT.equals(appTheme) || THEME_DARK.equals(appTheme)
                ? appTheme : THEME_SYSTEM;
        preferences(context).edit()
                .putString(KEY_APP_THEME, safeTheme)
                .apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }
}
