package mx.xperience.unicorn.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Context;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreferenceCompat;
import android.provider.Settings;
import com.android.settings.R;
import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import mx.xperience.framework.preference.CustomSeekBarPreference;
import mx.xperience.framework.preference.SystemSettingSeekBarPreference;
import mx.xperience.framework.preference.SystemSettingListPreference;
import mx.xperience.framework.preference.SystemSettingSwitchPreference;
import mx.xperience.framework.preference.SystemSettingSeekBarPreference;

import mx.xperience.unicorn.utils.DeviceUtils;
import com.android.settings.Utils;
import com.android.internal.util.xperience.XperienceUtils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import android.provider.SearchIndexableResource;
import android.util.Log;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class StatusBarSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String KEY_QUICK_PULLDOWN = "qs_quick_pulldown";
    private static final String PREF_NEW_STATUS_BAR_ICONS = "new_status_bar_icons";
    private static final String PREF_CLOCK_CHIP = "statusbar_clock_chip";
    private static final String PREF_SHOW_REFRESH_RATE = "show_refresh_rate";

    private static final int PULLDOWN_DIR_NONE = 0;
    private static final int PULLDOWN_DIR_RIGHT = 1;
    private static final int PULLDOWN_DIR_LEFT = 2;
    private static final int PULLDOWN_DIR_BOTH = 3;

    private SystemSettingListPreference mQuickPulldown;
    private SystemSettingListPreference mClockChipPref;
    private SwitchPreferenceCompat mNewStatusBarIconsPref;
    private SwitchPreferenceCompat mShowRefreshRatePref;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.statusbar);
		ContentResolver resolver = getActivity().getContentResolver();

        PreferenceScreen prefSet = getPreferenceScreen();

        mQuickPulldown =
                (SystemSettingListPreference) findPreference(KEY_QUICK_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        updateQuickPulldownSummary(mQuickPulldown.getIntValue(0));

        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            mQuickPulldown.setEntries(R.array.status_bar_quick_pull_down_entries_rtl);
            mQuickPulldown.setEntryValues(R.array.status_bar_quick_pull_down_values_rtl);
        }

        /**  new icons config*/
        mNewStatusBarIconsPref = findPreference(PREF_NEW_STATUS_BAR_ICONS);
        mNewStatusBarIconsPref.setOnPreferenceChangeListener(this);

        boolean newIconsEnabled = Settings.System.getIntForUser(
            resolver, "new_status_bar_icons_enabled", 1, UserHandle.USER_CURRENT) == 1;
        mNewStatusBarIconsPref.setChecked(newIconsEnabled);
        updatePreferenceStates(newIconsEnabled);

        mClockChipPref = (SystemSettingListPreference) findPreference(PREF_CLOCK_CHIP);
        if (mClockChipPref != null) {
            mClockChipPref.setOnPreferenceChangeListener(this);
            int currentVal = Settings.System.getIntForUser(
                resolver,
                "statusbar_clock_chip",
                0,//default
                UserHandle.USER_CURRENT);
            mClockChipPref.setValue(String.valueOf(currentVal));
        }

        mShowRefreshRatePref = (SwitchPreferenceCompat) findPreference(PREF_SHOW_REFRESH_RATE);
        if (mShowRefreshRatePref != null) {
            mShowRefreshRatePref.setOnPreferenceChangeListener(this);
            boolean refreshRateEnabled = Settings.System.getIntForUser(
                resolver, 
                Settings.System.SHOW_REFRESH_RATE, 
                0, 
                UserHandle.USER_CURRENT) == 1;
            mShowRefreshRatePref.setChecked(refreshRateEnabled);
        }
    }

    /**
     * This is the helper method that enables or disables all incompatible preferences.
     * @param newIconsEnabled true if the new UI is on, which means old options should be disabled.
     */
    private void updatePreferenceStates(boolean newIconsEnabled) {
        // We use !newIconsEnabled because if the new UI is ON (true),
        // the old preferences should be DISABLED (false).
        final boolean oldPrefsEnabled = !newIconsEnabled;

        findPreference("systemui_tuner_statusbar").setEnabled(oldPrefsEnabled);
        //findPreference("network_traffic_settings").setEnabled(oldPrefsEnabled);
        //findPreference("ongoing_progress_settings").setEnabled(oldPrefsEnabled);
        //findPreference("show_fourg_icon").setEnabled(oldPrefsEnabled);
        findPreference("statusbar_clock_chip").setEnabled(oldPrefsEnabled);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        final Context context = getContext();

        if (preference == mQuickPulldown) {
            int value = Integer.parseInt((String) objValue);
            updateQuickPulldownSummary(value);
            return true;
        }

        if (preference == mNewStatusBarIconsPref) {
            boolean value = (Boolean) objValue;

            updatePreferenceStates(value);
            Settings.System.putIntForUser(resolver, "status_bar_root_modernization_enabled",
                    value ? 1 : 0, UserHandle.USER_CURRENT);
            Settings.System.putIntForUser(resolver, "new_status_bar_icons_enabled",
                    value ? 1 : 0, UserHandle.USER_CURRENT);
            XperienceUtils.showSystemUiRestartDialog(context);
            return true;
        }

        if (preference == mClockChipPref) {
            int value = Integer.parseInt((String) objValue);
            Settings.System.putIntForUser(resolver,
                "statusbar_clock_chip",
                value,
                UserHandle.USER_CURRENT);

            // Whenever the clock chip style changes, request a SystemUI restart.
            XperienceUtils.showSystemUiRestartDialog(context);
            return true;
        }

        if (preference == mShowRefreshRatePref) {
            boolean value = (Boolean) objValue;
            Settings.System.putIntForUser(resolver, 
                Settings.System.SHOW_REFRESH_RATE, 
                value ? 1 : 0, 
                UserHandle.USER_CURRENT);
            
            // Reiniciar SystemUI para aplicar cambios
            //XperienceUtils.showSystemUiRestartDialog(context);
            return true;
        }

        return false;
    }

    private void updateQuickPulldownSummary(int value) {
        String summary = "";
        switch (value) {
            case PULLDOWN_DIR_NONE:
                summary = getResources().getString(
                    R.string.status_bar_quick_pull_down_off);
                break;
            case PULLDOWN_DIR_RIGHT:
            case PULLDOWN_DIR_LEFT:
            case PULLDOWN_DIR_BOTH:
                summary = getResources().getString(
                    R.string.status_bar_quick_pull_down_summary,
                    getResources().getString(
                        value == PULLDOWN_DIR_RIGHT
                            ? R.string.status_bar_quick_pull_down_right
                            : value == PULLDOWN_DIR_LEFT
                                ? R.string.status_bar_quick_pull_down_left
                                : R.string.status_bar_quick_pull_down_both
                    )
                );
                break;
        }
        mQuickPulldown.setSummary(summary);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.RAINBOW_UNICORN;
    }

	/**
     * For Search.
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.statusbar;
                    result.add(sir);
                    return result;
                }
                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };
}
