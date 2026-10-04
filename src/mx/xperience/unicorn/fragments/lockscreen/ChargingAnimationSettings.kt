/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 */

package mx.xperience.unicorn.fragments.lockscreen

import android.content.Context
import android.os.Bundle
import android.os.UserHandle
import android.provider.Settings

import com.android.internal.logging.nano.MetricsProto
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment

class ChargingAnimationSettings : SettingsPreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.charging_animation_settings)
    }

    override fun getMetricsCategory(): Int = MetricsProto.MetricsEvent.RAINBOW_UNICORN

    companion object {
        @JvmStatic
        fun reset(context: Context) {
            val resolver = context.contentResolver
            Settings.System.putIntForUser(resolver,
                    Settings.System.CHARGING_ANIMATION_ENABLED, 1, UserHandle.USER_CURRENT)
            Settings.System.putIntForUser(resolver,
                    Settings.System.CHARGING_ANIMATION_STYLE, 1, UserHandle.USER_CURRENT)
            Settings.System.putIntForUser(resolver,
                    Settings.System.CHARGING_COLOR_MODE, 0, UserHandle.USER_CURRENT)
            Settings.System.putIntForUser(resolver,
                    Settings.System.CHARGING_GLOW_INTENSITY, 80, UserHandle.USER_CURRENT)
            Settings.System.putIntForUser(resolver,
                    Settings.System.CHARGING_RIPPLE_OPACITY, 60, UserHandle.USER_CURRENT)
            Settings.System.putIntForUser(resolver,
                    Settings.System.CHARGING_SHOW_ON_LOCKSCREEN, 1, UserHandle.USER_CURRENT)
            Settings.System.putIntForUser(resolver,
                    Settings.System.CHARGING_SHOW_ON_AOD, 1, UserHandle.USER_CURRENT)
        }
    }
}
