package mx.xperience.unicorn.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import android.content.pm.PackageManager.NameNotFoundException;
import mx.xperience.framework.preference.CustomSeekBarPreference;
import mx.xperience.framework.preference.SystemSettingMasterSwitchPreference;
import mx.xperience.framework.preference.SystemSettingListPreference;
import mx.xperience.framework.preference.SecureSettingSwitchPreference;
import mx.xperience.unicorn.utils.DeviceUtils;
import com.android.internal.util.xperience.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import android.provider.SearchIndexableResource;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class MiscSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
/*
    private static final String KEY_ANIMATIONS_CATEGORY = "themes_animations_category";
    private static final String KEY_UDFPS_ANIMATION = "udfps_animation";

    private PreferenceCategory mAnimationsCategory;
    private Preference mUdfpsAnimation;*/

    private static final String KEY_MISCELLANEOUS_CATEGORY = "quick_settings_miscellaneous_category";
    private static final String KEY_QS_BLUETOOTH_SHOW_DIALOG = "qs_bt_show_dialog";

    private PreferenceCategory mMiscellaneousCategory;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        ContentResolver resolver = getActivity().getContentResolver();

        addPreferencesFromResource(R.xml.misc);

		/*Resources res = null;
        Context ctx = getContext();
        float density = Resources.getSystem().getDisplayMetrics().density;

        try {
            res = ctx.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        mAnimationsCategory = (PreferenceCategory) findPreference(KEY_ANIMATIONS_CATEGORY);
        mUdfpsAnimation = (Preference) findPreference(KEY_UDFPS_ANIMATION);

        FingerprintManager fingerprintManager = (FingerprintManager)
                getActivity().getSystemService(ctx.FINGERPRINT_SERVICE);

        if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
            mAnimationsCategory.removePreference(mUdfpsAnimation);
        } else {
            if (!VoltageUtils.isPackageInstalled(ctx, "mx.xperience.udfps.animations")) {
                mAnimationsCategory.removePreference(mUdfpsAnimation);
            }
        }

        // Check if the category is now empty
        if (mAnimationsCategory.getPreferenceCount() == 0) {
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            preferenceScreen.removePreference(mAnimationsCategory);
        }*/

        mMiscellaneousCategory = (PreferenceCategory) findPreference(KEY_MISCELLANEOUS_CATEGORY);

        if (!DeviceUtils.deviceSupportsBluetooth(context)) {
            prefScreen.removePreference(mMiscellaneousCategory);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        return false;
    }


    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.RAINBOW_UNICORN;
    }

	/**
     * For Search.
     */

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

              @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.misc;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                final Resources resources = context.getResources();

                /*FingerprintManager fingerprintManager = (FingerprintManager)
                        context.getSystemService(Context.FINGERPRINT_SERVICE);

                if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
                    keys.add(KEY_UDFPS_ANIMATION);
                } else {
                    if (!VoltageUtils.isPackageInstalled(context, "mx.xperience.udfps.animations")) {
                        keys.add(KEY_UDFPS_ANIMATION);
                    }
                }*/
                if (!DeviceUtils.deviceSupportsBluetooth(context)) {
                    keys.add(KEY_QS_BLUETOOTH_SHOW_DIALOG);
                }
                return keys;
            }
    };
}
