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

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.android.internal.view.RotationPolicy;

public class DisplaySettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "DisplayRotation";

    private static final String ROTATION_0_PREF = "display_rotation_0";
    private static final String ROTATION_90_PREF = "display_rotation_90";
    private static final String ROTATION_180_PREF = "display_rotation_180";
    private static final String ROTATION_270_PREF = "display_rotation_270";

    private CheckBoxPreference mRotation0Pref;
    private CheckBoxPreference mRotation90Pref;
    private CheckBoxPreference mRotation180Pref;
    private CheckBoxPreference mRotation270Pref;

    public static final int ROTATION_0_MODE = 1;
    public static final int ROTATION_90_MODE = 2;
    public static final int ROTATION_180_MODE = 4;
    public static final int ROTATION_270_MODE = 8;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.xperience_display);

        PreferenceScreen prefSet = getPreferenceScreen();

        mRotation0Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_0_PREF);
        mRotation90Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_90_PREF);
        mRotation180Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_180_PREF);
        mRotation270Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_270_PREF);

        int mode = Settings.System.getInt(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION_ANGLES,
                ROTATION_0_MODE | ROTATION_90_MODE | ROTATION_270_MODE);

        mRotation0Pref.setChecked((mode & ROTATION_0_MODE) != 0);
        mRotation90Pref.setChecked((mode & ROTATION_90_MODE) != 0);
        mRotation180Pref.setChecked((mode & ROTATION_180_MODE) != 0);
        mRotation270Pref.setChecked((mode & ROTATION_270_MODE) != 0);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {

        return false;
    }

    private int getRotationBitmask() {
        int mode = 0;
        if (mRotation0Pref.isChecked()) {
            mode |= ROTATION_0_MODE;
        }
        if (mRotation90Pref.isChecked()) {
            mode |= ROTATION_90_MODE;
        }
        if (mRotation180Pref.isChecked()) {
            mode |= ROTATION_180_MODE;
        }
        if (mRotation270Pref.isChecked()) {
            mode |= ROTATION_270_MODE;
        }
        return mode;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
      if (preference == mRotation0Pref ||
          preference == mRotation90Pref ||
          preference == mRotation180Pref ||
          preference == mRotation270Pref) {
              int mode = getRotationBitmask();
              if (mode == 0) {
                  mode |= ROTATION_0_MODE;
                  mRotation0Pref.setChecked(true);
              }
              Settings.System.putInt(getActivity().getContentResolver(),
                      Settings.System.ACCELEROMETER_ROTATION_ANGLES, mode);
                return true;
            }
              return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.RAINBOW_UNICORN;
    }

}
