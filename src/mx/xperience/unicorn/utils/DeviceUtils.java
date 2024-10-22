/*
 * Copyright (C) 2024 The XPerience Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mx.xperience.unicorn.utils;

import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_2BUTTON;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintSensorPropertiesInternal;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Surface;

import java.util.List;
import java.util.Arrays;

public class DeviceUtils {

    private static final String DEVICE = "ro.xpe.model";


    public static boolean isPackageInstalled(Context context, String pkg, boolean ignoreState) {
        if (pkg != null) {
            try {
                PackageInfo pi = context.getPackageManager().getPackageInfo(pkg, 0);
                if (!pi.applicationInfo.enabled && !ignoreState) {
                    return false;
                }
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDozeAvailable(Context context) {
        String name = Build.IS_DEBUGGABLE ? SystemProperties.get("debug.doze.component") : null;
        if (TextUtils.isEmpty(name)) {
            name = context.getResources().getString(
                    com.android.internal.R.string.config_dozeComponent);
        }
        return !TextUtils.isEmpty(name);
    }


    public static boolean deviceSupportsBluetooth() {
        return (BluetoothAdapter.getDefaultAdapter() != null);
    }

    public static boolean deviceSupportsNfc(Context ctx) {
        return NfcAdapter.getDefaultAdapter(ctx) != null;
    }

    public static boolean deviceSupportsFlashLight(Context context) {
        CameraManager cameraManager = context.getSystemService(CameraManager.class);
        try {
            String[] ids = cameraManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                if (flashAvailable != null
                        && flashAvailable
                        && lensFacing != null
                        && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    return true;
                }
            }
        } catch (ArrayIndexOutOfBoundsException | CameraAccessException | AssertionError e) {
            // Ignore
        }
        return false;
    }

    private static final String[] currentlySupportedPixels = {
            "rango",
            "mustang",
            "blazer",
            "frankel",
            "komodo",
            "caiman",
            "tokay",
            "comet",
            "akita",
            "husky",
            "shiba",
            "felix",
            "tangorpro",
            "lynx",
            "cheetah",
            "panther",
            "bluejay",
            "oriole",
            "raven",
            "barbet"
    };
    public static boolean isCurrentlySupportedPixel() {
        String deviceCodename = SystemProperties.get(DEVICE);
        return Arrays.asList(currentlySupportedPixels).contains(deviceCodename);
    }

     /**
     * Checks if the device has udfps
     * @param context context for getting FingerprintManager
     * @return true is udfps is present
     */
    public static boolean hasUDFPS(Context context) {
        final FingerprintManager fingerprintManager =
                context.getSystemService(FingerprintManager.class);
        final List<FingerprintSensorPropertiesInternal> props =
                fingerprintManager.getSensorPropertiesInternal();
        return props != null && props.size() == 1 && props.get(0).isAnyUdfpsType();

    }

}
