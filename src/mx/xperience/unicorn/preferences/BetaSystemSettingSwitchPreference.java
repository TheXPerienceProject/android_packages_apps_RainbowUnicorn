/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 */

package mx.xperience.unicorn.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

import mx.xperience.framework.preference.SystemSettingSwitchPreference;

/**
 * SystemSettingSwitchPreference featuring an interactive Material 3 BETA Chip indicator.
 */
public class BetaSystemSettingSwitchPreference extends SystemSettingSwitchPreference {

    public BetaSystemSettingSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public BetaSystemSettingSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BetaSystemSettingSwitchPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        setLayoutResource(R.layout.preference_switch_beta);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View betaChip = holder.findViewById(R.id.beta_chip);
        if (betaChip != null) {
            betaChip.setOnClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.beta_feature_title)
                        .setMessage(R.string.beta_feature_desc)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            });
        }
    }
}
