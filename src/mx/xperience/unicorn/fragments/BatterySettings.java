/*
 *     SPDX-FileCopyrightText: 2011-2026 The XPerience Project
 *     SPDX-License-Identifier: Apache-2.0
 *
*/
package mx.xperience.unicorn.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import android.provider.SearchIndexableResource;

import java.util.ArrayList;
import java.util.List;

import mx.xperience.framework.preference.SystemSettingListPreference;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class BatterySettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "BatterySettings";

    /*private static final String KEY_BATTERY_STYLE =
            Settings.System.STATUS_BAR_BATTERY_STYLE;

    private static final String KEY_BATTERY_PERCENT =
            Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT;*/

    private SystemSettingListPreference mBatteryStyle;
    private SystemSettingListPreference mBatteryPercent;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.battery_styles);

       /* mBatteryStyle = (SystemSettingListPreference) findPreference(KEY_BATTERY_STYLE);
        mBatteryPercent = (SystemSettingListPreference) findPreference(KEY_BATTERY_PERCENT);

        Log.d(TAG, "mBatteryStyle = " + mBatteryStyle);
        Log.d(TAG, "mBatteryPercent = " + mBatteryPercent);

        if (mBatteryStyle != null) {
            mBatteryStyle.setOnPreferenceChangeListener(this);
        }

        if (mBatteryPercent != null) {
            mBatteryPercent.setOnPreferenceChangeListener(this);
        }

        int currentStyle = Settings.System.getIntForUser(
                getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_BATTERY_STYLE,
                0,
                UserHandle.USER_CURRENT);

        int currentPercent = Settings.System.getIntForUser(
                getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT,
                0,
                UserHandle.USER_CURRENT);

        Log.d(TAG, "Initial style = " + currentStyle);
        Log.d(TAG, "Initial percent = " + currentPercent);

        enableStatusBarBatteryDependents(currentStyle);*/
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
/*
        if (preference == mBatteryStyle) {
            int value = Integer.parseInt(newValue.toString());
            Log.d(TAG, "Battery style changed -> " + value);
            enableStatusBarBatteryDependents(value);
        }

        if (preference == mBatteryPercent) {
            Log.d(TAG, "Battery percent changed -> " + newValue);
        }*/

        return true;
    }

    private void enableStatusBarBatteryDependents(int style) {
/*
        if (mBatteryPercent == null) {
            Log.e(TAG, "Battery percent preference is NULL");
            return;
        }

        Log.d(TAG, "Current battery style = " + style);

        // disable percentage if the style is ‘text’
        boolean enablePercent = style != 2;

        mBatteryPercent.setEnabled(enablePercent);

        Log.d(TAG, "Battery percent enabled = " + enablePercent);*/
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.RAINBOW_UNICORN;
    }

    /**
     *  Search Index
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {

                    final ArrayList<SearchIndexableResource> result =
                            new ArrayList<>();

                    final SearchIndexableResource sir =
                            new SearchIndexableResource(context);

                    sir.xmlResId = R.xml.battery_styles;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    return super.getNonIndexableKeys(context);
                }
            };
}
