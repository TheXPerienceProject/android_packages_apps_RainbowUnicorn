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

package mx.xperience.unicorn;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Surface;
import android.graphics.Color;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;
import com.android.settings.R;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.HighlightablePreferenceGroupAdapter;

public class Rainbow extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.rainbow_unicorn);

        Preference openApp = findPreference("battery_adviser");

        if (openApp != null) {
            openApp.setOnPreferenceClickListener(pref -> {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(
                        "mx.xperience.batteryadviser",
                        "mx.xperience.batteryadviser.MainActivity"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            });
        }
    }


    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        return new HighlightablePreferenceGroupAdapter(preferenceScreen, null, false) {
            @Override
            public void onBindViewHolder(PreferenceViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);

                Preference preference = getItem(position);
                if (preference instanceof RainbowHeaderPreference
                        || (preference != null
                        && "rainbow_header".equals(preference.getKey()))) {
                    // Settings' expressive preference adapter applies its rounded
                    // row background after Preference.onBindViewHolder(). Clear it
                    // here, after the adapter has finished binding the row.
                    clearAdapterBackground(holder);
                    holder.setDividerAllowedAbove(false);
                    holder.setDividerAllowedBelow(false);
                } else if (isDashboardPreference(preference)) {
                    // The expressive adapter replaces the background declared by
                    // rainbow_dashboard_preference.xml. Restore our own card after
                    // the adapter finishes binding so every dashboard row keeps
                    // its intended rounded shape, including the first one below
                    // the hero.
                    restoreDashboardBackground(holder);
                }
            }
        };
    }


    private static boolean isDashboardPreference(Preference preference) {
        if (preference == null || preference.getKey() == null) {
            return false;
        }

        switch (preference.getKey()) {
            case "statusbar_category":
            case "lockscreen":
            case "misc_category":
            case "spoofing":
            case "battery_adviser":
                return true;
            default:
                return false;
        }
    }

    private static void clearAdapterBackground(PreferenceViewHolder holder) {
        holder.itemView.setBackground(null);
        holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        holder.itemView.setElevation(0f);
        holder.itemView.setStateListAnimator(null);
    }

    private static void restoreDashboardBackground(PreferenceViewHolder holder) {
        holder.itemView.setBackgroundResource(R.drawable.rainbow_dashboard_card_background);
        holder.itemView.setElevation(0f);
        holder.itemView.setStateListAnimator(null);
        holder.setDividerAllowedAbove(false);
        holder.setDividerAllowedBelow(false);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.RAINBOW_UNICORN;
    }
}
