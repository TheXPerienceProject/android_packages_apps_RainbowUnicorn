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

package mx.xperience.unicorn.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintSensorPropertiesInternal;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.Surface;

import java.util.List;

public class DeviceUtils {

    public static boolean isPackageInstalled(Context context, String pkg, boolean ignoreState) {
        if (pkg != null) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkg, 0);
                if (!packageInfo.applicationInfo.enabled && !ignoreState) {
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
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    public static boolean deviceSupportsNfc(Context context) {
        return NfcAdapter.getDefaultAdapter(context) != null;
    }

    public static boolean deviceSupportsFlashLight(Context context) {
        CameraManager cameraManager = context.getSystemService(CameraManager.class);
        try {
            String[] ids = cameraManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics characteristics =
                        cameraManager.getCameraCharacteristics(id);
                Boolean flashAvailable =
                        characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (flashAvailable != null
                        && flashAvailable
                        && lensFacing != null
                        && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    return true;
                }
            }
        } catch (ArrayIndexOutOfBoundsException | CameraAccessException | AssertionError e) {
            // Ignore.
        }
        return false;
    }

    public static boolean isCurrentlySupportedPixel() {
        return SystemProperties.get("ro.product.model").matches("Pixel [3-9][a-zA-Z ]*");
    }

    public static boolean deviceSupportsBluetooth(Context context) {
        BluetoothManager bluetoothManager =
                context.getSystemService(BluetoothManager.class);
        return bluetoothManager != null && bluetoothManager.getAdapter() != null;
    }

    /** Returns whether the device has a centered display cutout. */
    public static boolean hasCenteredCutout(Context context) {
        Display display = context.getDisplay();
        if (display == null) {
            return false;
        }

        DisplayCutout cutout = display.getCutout();
        if (cutout == null) {
            return false;
        }

        Point realSize = new Point();
        display.getRealSize(realSize);
        switch (display.getRotation()) {
            case Surface.ROTATION_0: {
                Rect rect = cutout.getBoundingRectTop();
                return !(rect.left <= 0 || rect.right >= realSize.x);
            }
            case Surface.ROTATION_90: {
                Rect rect = cutout.getBoundingRectLeft();
                return !(rect.top <= 0 || rect.bottom >= realSize.y);
            }
            case Surface.ROTATION_180: {
                Rect rect = cutout.getBoundingRectBottom();
                return !(rect.left <= 0 || rect.right >= realSize.x);
            }
            case Surface.ROTATION_270: {
                Rect rect = cutout.getBoundingRectRight();
                return !(rect.top <= 0 || rect.bottom >= realSize.y);
            }
            default:
                return false;
        }
    }

    /**
     * Checks whether the device has an under-display fingerprint sensor.
     *
     * @param context context used to obtain {@link FingerprintManager}
     * @return {@code true} when a UDFPS sensor is present
     */
    public static boolean hasUDFPS(Context context) {
        FingerprintManager fingerprintManager =
                context.getSystemService(FingerprintManager.class);
        if (fingerprintManager == null) {
            return false;
        }

        List<FingerprintSensorPropertiesInternal> properties =
                fingerprintManager.getSensorPropertiesInternal();
        return properties != null
                && properties.size() == 1
                && properties.get(0).isAnyUdfpsType();
    }
}
