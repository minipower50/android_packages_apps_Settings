/*
 * Copyright (C) 2013 The CyanogenMod Project
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

package com.android.settings.cyanogenmod;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

/**
 * Performance Settings
 */
public class PerformanceSettings extends SettingsPreferenceFragment {
    private static final String TAG = "PerformanceSettings";

    private static final String PERF_PROFILE_PREF = "performance_profile";
    private static final String USE_16BPP_ALPHA_PREF = "pref_use_16bpp_alpha";

    private static final String USE_16BPP_ALPHA_PROP = "persist.sys.use_16bpp_alpha";

    private static final String DISABLE_BOOTANIMATION_PREF = "pref_disable_bootanimation";

    private static final String DISABLE_BOOTANIMATION_PERSIST_PROP = "persist.sys.nobootanimation";

    private static final String DISABLE_BOOTANIMATION_DEFAULT = "0";

    private static final String DISABLE_WALLPAPER_PREF = "pref_disable_wallpaperservice";

    private static final String DISABLE_WALLPAPER_PERSIST_PROP = "persist.sys.wallpaperservice";

    private static final String DISABLE_WALLPAPER_DEFAULT = "1";

    private Preference mPerfProfilePref;
    private CheckBoxPreference mUse16bppAlphaPref;

    private CheckBoxPreference mDisableBootanimPref;

    private CheckBoxPreference mDisableWallpaperPref;

    private AlertDialog alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.performance_settings);

        final Resources res = getResources();
        PreferenceScreen prefSet = getPreferenceScreen();

        mPerfProfilePref = prefSet.findPreference(PERF_PROFILE_PREF);
        String perfProfileProp = getString(R.string.config_perf_profile_prop);
        if (mPerfProfilePref != null && TextUtils.isEmpty(perfProfileProp)) {
            prefSet.removePreference(mPerfProfilePref);
        }

        mUse16bppAlphaPref = (CheckBoxPreference) prefSet.findPreference(USE_16BPP_ALPHA_PREF);
        String use16bppAlpha = SystemProperties.get(USE_16BPP_ALPHA_PROP, "0");
        mUse16bppAlphaPref.setChecked("1".equals(use16bppAlpha));

        mDisableBootanimPref = (CheckBoxPreference) prefSet
                .findPreference(DISABLE_BOOTANIMATION_PREF);
        String disableBootanimation = SystemProperties.get(DISABLE_BOOTANIMATION_PERSIST_PROP,
                DISABLE_BOOTANIMATION_DEFAULT);
        mDisableBootanimPref.setChecked("1".equals(disableBootanimation));

        mDisableWallpaperPref = (CheckBoxPreference) prefSet
                .findPreference(DISABLE_WALLPAPER_PREF);
        String disableWallpaper = SystemProperties.get(DISABLE_WALLPAPER_PERSIST_PROP,
                DISABLE_WALLPAPER_DEFAULT);
        mDisableWallpaperPref.setChecked("0".equals(disableWallpaper));

        /* Display the warning dialog */
        alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(R.string.performance_settings_warning_title);
        alertDialog.setMessage(getResources().getString(R.string.performance_settings_warning));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getResources().getString(com.android.internal.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                PerformanceSettings.this.finish();
            }
        });
        alertDialog.show();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mUse16bppAlphaPref) {
            SystemProperties.set(USE_16BPP_ALPHA_PROP,
                    mUse16bppAlphaPref.isChecked() ? "1" : "0");
        } else if (preference == mDisableBootanimPref) {
            SystemProperties.set(DISABLE_BOOTANIMATION_PERSIST_PROP,
                    mDisableBootanimPref.isChecked() ? "1" : "0");
        } else if (preference == mDisableWallpaperPref) {
            SystemProperties.set(DISABLE_WALLPAPER_PERSIST_PROP,
                    mDisableWallpaperPref.isChecked() ? "0" : "1");
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }
}
