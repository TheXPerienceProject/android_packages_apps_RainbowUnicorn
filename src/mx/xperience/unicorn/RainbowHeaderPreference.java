/*
 * Copyright (C) 2026 The XPerience Project
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

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

/**
 * Non-interactive dashboard header used by Rainbow Unicorn.
 *
 * Keep the preference row itself fully transparent so Settings does not draw
 * its normal card/background behind the hero layout.
 */
public class RainbowHeaderPreference extends Preference {

    public RainbowHeaderPreference(Context context) {
        super(context);
        init();
    }

    public RainbowHeaderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RainbowHeaderPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayoutResource(R.layout.rainbow_dashboard_header);
        setSelectable(false);
        setPersistent(false);
        setShouldDisableView(false);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        final View item = holder.itemView;

        // Remove the item's background
        item.setBackground(null);
        item.setBackgroundColor(Color.TRANSPARENT);
        item.setPadding(0, 0, 0, 0);
        item.setClickable(false);
        item.setFocusable(false);
        item.setLongClickable(false);
        item.setElevation(0f);

        if (item instanceof ViewGroup) {
            ((ViewGroup) item).setClipToPadding(false);
            ((ViewGroup) item).setClipChildren(false);
        }

        holder.setDividerAllowedAbove(false);
        holder.setDividerAllowedBelow(false);
    }
}