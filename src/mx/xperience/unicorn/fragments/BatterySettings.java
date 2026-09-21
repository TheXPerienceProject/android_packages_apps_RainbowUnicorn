/*
 *     SPDX-FileCopyrightText: 2011-2026 The XPerience Project
 *     SPDX-License-Identifier: Apache-2.0
 *
 */
package mx.xperience.unicorn.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

import mx.xperience.framework.preference.SystemSettingListPreference;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class BatterySettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_BATTERY_STYLE =
            Settings.System.STATUS_BAR_BATTERY_STYLE;
    private static final String KEY_BATTERY_PERCENT =
            Settings.System.SHOW_BATTERY_PERCENT;

    private static final int BATTERY_STYLE_TEXT = 2;
    private static final int BATTERY_STYLE_AOSPA = 4;
    private static final int BATTERY_PERCENT_HIDDEN = 0;
    private static final int BATTERY_PERCENT_INSIDE = 1;
    private static final int BATTERY_PERCENT_NEXT = 2;

    private static final String KEY_AOSPA_BATTERY_PERCENT = "aospa_battery_percent";

    private SystemSettingListPreference mBatteryStyle;
    private SystemSettingListPreference mBatteryPercent;
    private SwitchPreferenceCompat mAospaBatteryPercent;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.battery_styles);

        mBatteryStyle = findPreference(KEY_BATTERY_STYLE);
        mBatteryPercent = findPreference(KEY_BATTERY_PERCENT);
        mAospaBatteryPercent = findPreference(KEY_AOSPA_BATTERY_PERCENT);

        if (mBatteryStyle != null) {
            mBatteryStyle.setOnPreferenceChangeListener(this);
        }

        if (mBatteryPercent != null) {
            mBatteryPercent.setOnPreferenceChangeListener(this);
        }

        if (mAospaBatteryPercent != null) {
            mAospaBatteryPercent.setOnPreferenceChangeListener(this);
        }

        final int currentStyle = Settings.System.getIntForUser(
                requireContext().getContentResolver(),
                Settings.System.STATUS_BAR_BATTERY_STYLE,
                BATTERY_STYLE_AOSPA,
                UserHandle.USER_CURRENT);

        updateBatteryPercentAvailability(currentStyle);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBatteryStyle) {
            final int style = Integer.parseInt(newValue.toString());
            updateBatteryPercentAvailability(style);
            return true;
        }

        if (preference == mAospaBatteryPercent) {
            final int percent = (Boolean) newValue
                    ? BATTERY_PERCENT_NEXT
                    : BATTERY_PERCENT_HIDDEN;

            // AOSPA has a solid center dot, so percentage can only be shown next to it.
            if (mBatteryPercent != null) {
                mBatteryPercent.setValue(Integer.toString(percent));
            } else {
                Settings.System.putIntForUser(
                        requireContext().getContentResolver(),
                        Settings.System.SHOW_BATTERY_PERCENT,
                        percent,
                        UserHandle.USER_CURRENT);
            }
            return true;
        }

        // SystemSettingListPreference persists the selected value to Settings.System.
        return true;
    }

    private void updateBatteryPercentAvailability(int style) {
        if (mBatteryPercent == null || mAospaBatteryPercent == null) {
            return;
        }

        // Text style already represents the battery level itself.
        if (style == BATTERY_STYLE_TEXT) {
            mBatteryPercent.setVisible(false);
            mAospaBatteryPercent.setVisible(false);
            return;
        }

        int percent = Settings.System.getIntForUser(
                requireContext().getContentResolver(),
                Settings.System.SHOW_BATTERY_PERCENT,
                BATTERY_PERCENT_HIDDEN,
                UserHandle.USER_CURRENT);

        if (style == BATTERY_STYLE_AOSPA) {
            // Preserve a visible percentage when moving from an inside-capable style.
            if (percent == BATTERY_PERCENT_INSIDE) {
                percent = BATTERY_PERCENT_NEXT;
                mBatteryPercent.setValue(Integer.toString(percent));
            }

            mBatteryPercent.setVisible(false);
            mAospaBatteryPercent.setChecked(percent == BATTERY_PERCENT_NEXT);
            mAospaBatteryPercent.setVisible(true);
            return;
        }

        // AOSP, Circle and Dotted support all three percentage placements.
        mAospaBatteryPercent.setVisible(false);
        mBatteryPercent.setValue(Integer.toString(percent));
        mBatteryPercent.setVisible(true);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.RAINBOW_UNICORN;
    }

    /**
     * Search Index
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final ArrayList<SearchIndexableResource> result = new ArrayList<>();
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.battery_styles;
                    result.add(sir);
                    return result;
                }
            };
}
