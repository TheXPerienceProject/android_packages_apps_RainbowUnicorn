/*
 * Copyright (C) 2017 The XPerience Project
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
package mx.xperience.rainbowunicorn.fragments;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.preference.PreferenceFragment;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class DisplaySettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String SHOW_CLEAR_ALL_RECENTS = "show_clear_all_recents";
    private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";

    private SwitchPreference mRecentsClearAll;
    private ListPreference mRecentsClearAllLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.display_settings_xpe);
        PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources res = getResources();
        ContentResolver resolver = getActivity().getContentResolver();

            mRecentsClearAll = (SwitchPreference) prefScreen.findPreference(SHOW_CLEAR_ALL_RECENTS);
            mRecentsClearAll.setChecked(Settings.System.getIntForUser(resolver,
                    Settings.System.SHOW_CLEAR_ALL_RECENTS, 1, UserHandle.USER_CURRENT) == 1);
            mRecentsClearAll.setOnPreferenceChangeListener(this);

            mRecentsClearAllLocation = (ListPreference) prefScreen.findPreference(RECENTS_CLEAR_ALL_LOCATION);
            int location = Settings.System.getIntForUser(resolver,
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
           mRecentsClearAllLocation.setValue(String.valueOf(location));
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry());
            mRecentsClearAllLocation.setOnPreferenceChangeListener(this);

    }

     public boolean onPreferenceChange(Preference preference, Object newValue) 	  {
          ContentResolver resolver = getActivity().getContentResolver();

     if (preference == mRecentsClearAll) {
                boolean show = (Boolean) newValue;
                Settings.System.putIntForUser(resolver, Settings.System.SHOW_CLEAR_ALL_RECENTS,
                        show ? 1 : 0, UserHandle.USER_CURRENT);
                return true;
            } else if (preference == mRecentsClearAllLocation) {
                int location = Integer.valueOf((String) newValue);
                int index = mRecentsClearAllLocation.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.RECENTS_CLEAR_ALL_LOCATION,
                        location, UserHandle.USER_CURRENT);
                mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
                return true;

              }

            return false;
  }
    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.RAINBOW_UNICORN;
    }

}
