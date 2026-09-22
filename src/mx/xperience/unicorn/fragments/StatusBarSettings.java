/*
 * Copyright (C) 2024 The XPerience Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mx.xperience.unicorn.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

import mx.xperience.framework.preference.SystemSettingSwitchPreference;
import mx.xperience.unicorn.utils.DeviceUtils;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class StatusBarSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String PREF_SHOW_REFRESH_RATE = "show_refresh_rate";
    private static final String KEY_QS_IOS_CONTROL_PANEL = "qs_ios_control_panel";
    private static final String KEY_DYNAMIC_ISLAND_CATEGORY = "dynamic_island_category";
    private static final String KEY_DYNAMIC_ISLAND = "status_bar_dynamic_island";

    private SwitchPreferenceCompat mShowRefreshRatePref;
    private SystemSettingSwitchPreference mQsIosControlPanel;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.statusbar);

        ContentResolver resolver = requireContext().getContentResolver();

        mShowRefreshRatePref = findPreference(PREF_SHOW_REFRESH_RATE);
        if (mShowRefreshRatePref != null) {
            mShowRefreshRatePref.setOnPreferenceChangeListener(this);
            boolean refreshRateEnabled = Settings.System.getIntForUser(
                    resolver,
                    Settings.System.SHOW_REFRESH_RATE,
                    0,
                    UserHandle.USER_CURRENT) == 1;
            mShowRefreshRatePref.setChecked(refreshRateEnabled);
        }

        mQsIosControlPanel = findPreference(KEY_QS_IOS_CONTROL_PANEL);
        if (mQsIosControlPanel != null) {
            mQsIosControlPanel.setOnPreferenceChangeListener(this);
        }

        if (!DeviceUtils.hasCenteredCutout(requireContext())) {
            PreferenceCategory islandCategory = findPreference(KEY_DYNAMIC_ISLAND_CATEGORY);
            if (islandCategory != null) {
                getPreferenceScreen().removePreference(islandCategory);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = requireContext().getContentResolver();

        if (preference == mShowRefreshRatePref) {
            boolean enabled = (Boolean) newValue;
            Settings.System.putIntForUser(
                    resolver,
                    Settings.System.SHOW_REFRESH_RATE,
                    enabled ? 1 : 0,
                    UserHandle.USER_CURRENT);
            return true;
        }

        if (preference == mQsIosControlPanel) {
            boolean enabled = (Boolean) newValue;
            Settings.Secure.putIntForUser(
                    resolver,
                    "qs_show_brightness_slider",
                    enabled ? 0 : 1,
                    UserHandle.USER_CURRENT);
            return true;
        }

        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.RAINBOW_UNICORN;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    List<SearchIndexableResource> result = new ArrayList<>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.statusbar;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    if (!DeviceUtils.hasCenteredCutout(context)) {
                        keys.add(KEY_DYNAMIC_ISLAND_CATEGORY);
                        keys.add(KEY_DYNAMIC_ISLAND);
                    }
                    return keys;
                }
            };
}
