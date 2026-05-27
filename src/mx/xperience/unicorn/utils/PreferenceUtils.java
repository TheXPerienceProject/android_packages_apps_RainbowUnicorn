/*
 * SPDX-FileCopyrightText: Evolution X
 * SPDX-License-Identifier: Apache-2.0
 */

package mx.xperience.unicorn.utils;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;

import mx.xperience.unicorn.preferences.GlobalSettingPrimarySwitchPreference;
import mx.xperience.unicorn.preferences.SecureSettingPrimarySwitchPreference;
import mx.xperience.unicorn.preferences.SystemSettingPrimarySwitchPreference;

import java.util.ArrayList;
import java.util.List;

public class PreferenceUtils {

    public static void reloadCustomPrimarySwitches(PreferenceGroup group) {
        reloadCustomPrimarySwitches(getAllPreferences(group));
    }

    public static void reloadCustomPrimarySwitches(List<Preference> prefs) {
        for (Preference p : prefs) {
            switch (p) {
                case SecureSettingPrimarySwitchPreference psp -> psp.loadValue();
                case SystemSettingPrimarySwitchPreference psp -> psp.loadValue();
                case GlobalSettingPrimarySwitchPreference psp -> psp.loadValue();
                default -> {}
            }
        }
    }

    public static List<Preference> getAllPreferences(PreferenceGroup group) {
        List<Preference> preferences = new ArrayList<>();
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            Preference pref = group.getPreference(i);
            preferences.add(pref);
            if (pref instanceof PreferenceGroup) {
                preferences.addAll(getAllPreferences((PreferenceGroup) pref));
            }
        }
        return preferences;
    }
}
