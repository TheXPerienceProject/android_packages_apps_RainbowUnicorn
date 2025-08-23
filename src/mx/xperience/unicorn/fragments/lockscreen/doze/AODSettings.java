/*
 * Copyright (C) 2023 crDroid Android Project
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
package mx.xperience.unicorn.fragments.lockscreen.doze;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import com.android.internal.logging.nano.MetricsProto;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.utils.ImageUtils;

public class AODSettings extends SettingsPreferenceFragment {

    private static final String CUSTOM_IMAGE_REQUEST_CODE_KEY = "lockscreen_custom_image";
    private static final int CUSTOM_IMAGE_REQUEST_CODE = 1001;

    private Preference mCustomImagePreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.aod_settings);
        mCustomImagePreference = findPreference(CUSTOM_IMAGE_REQUEST_CODE_KEY);
        int clockStyle = Settings.Secure.getIntForUser(getContext().getContentResolver(), "clock_style", 0, UserHandle.USER_CURRENT);
        String imagePath = Settings.System.getString(getContext().getContentResolver(), "custom_aod_image_uri");
        if (imagePath != null && clockStyle > 0) {
            mCustomImagePreference.setSummary(imagePath);
            mCustomImagePreference.setEnabled(true);
        } else if (clockStyle == 0) {
            mCustomImagePreference.setSummary(getContext().getString(R.string.custom_aod_image_not_supported));
            mCustomImagePreference.setEnabled(false);
        }
    }
    
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mCustomImagePreference) {
            try {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, CUSTOM_IMAGE_REQUEST_CODE);
            } catch(Exception e) {
                Toast.makeText(getContext(), R.string.qs_header_needs_gallery, Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

@Override
public void onActivityResult(int requestCode, int resultCode, Intent result) {
    super.onActivityResult(requestCode, resultCode, result);
    
    if (requestCode == CUSTOM_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && result != null) {
        Context context = getContext();
        if (context == null) return;
        
        Uri imgUri = result.getData();
        if (imgUri != null) {
            try {
                // Limpiar archivos antiguos primero
                ImageUtils.cleanupOldFiles(context, "lockscreen_aod_image", "LOCKSCREEN_CUSTOM_AOD_IMAGE");
                
                String savedImagePath = ImageUtils.saveImageToInternalStorage(
                    context, imgUri, "lockscreen_aod_image", "LOCKSCREEN_CUSTOM_AOD_IMAGE");
                
                if (savedImagePath != null) {
                    ContentResolver resolver = context.getContentResolver();
                    Settings.System.putStringForUser(resolver, "custom_aod_image_uri", 
                        savedImagePath, UserHandle.USER_CURRENT);
                    
                    mCustomImagePreference.setSummary(savedImagePath);
                    Toast.makeText(context, R.string.custom_aod_image_saved, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.custom_aod_image_save_failed, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(context, R.string.custom_aod_image_save_error, Toast.LENGTH_LONG).show();
            }
        }
    }
}

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.VIEW_UNKNOWN;
    }
}