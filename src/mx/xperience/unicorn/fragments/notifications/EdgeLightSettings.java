/*
 * Copyright (C) 2019-2024 The Evolution X Project
 * SPDX-License-Identifier: Apache-2.0
 */

package mx.xperience.unicorn.fragments.notifications;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class EdgeLightSettings extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.edge_light_settings);
    }

    public static void reset(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.EDGE_LIGHT_ENABLED, 0, UserHandle.USER_CURRENT);
        Settings.System.putStringForUser(resolver,
                Settings.System.EDGE_LIGHT_COLOR_MODE, "accent", UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.EDGE_LIGHT_CUSTOM_COLOR, Color.WHITE, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.EDGE_LIGHT_PULSE_COUNT, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.EDGE_LIGHT_STROKE_WIDTH, 8, UserHandle.USER_CURRENT);
        Settings.System.putStringForUser(resolver,
                Settings.System.EDGE_LIGHT_STYLE, "default", UserHandle.USER_CURRENT);
        Settings.System.putStringForUser(resolver,
                Settings.System.EDGE_LIGHT_ANIMATION_EFFECT, "none", UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.RAINBOW_UNICORN;
    }
}
