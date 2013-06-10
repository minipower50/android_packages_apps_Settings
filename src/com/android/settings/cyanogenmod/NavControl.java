/*
 * Copyright (C) 2012 The CyanogenMod Project
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

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.IWindowManager;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class NavControl extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String NAVIGATION_BUTTON_COLOR = "navigation_button_color";
    private static final String KEY_NAVIGATION_CONTROLS = "navigation_controls";
    private static final String COMBINED_BAR_NAVIGATION_FORCE_MENU =
            "combined_bar_navigation_force_menu";
    private static final String NAVIGATION_BUTTON_GLOW_COLOR = "navigation_button_glow_color";
    private static final String NAVIGATION_BUTTON_GLOW_TIME =
            "navigation_button_glow_time";
    private static final String NAVIGATION_BAR_COLOR = "navigation_bar_color";
    private static final String KEY_NAVIGATION_BAR = "navigation_bar";
    private static final String KEY_NAVIGATION_ALIGNMENT = "nav_alignment";

    private CheckBoxPreference mNavigationControls;
    private CheckBoxPreference mCombinedBarNavigationForceMenu;
    private ListPreference mNavigationAlignment;
    private Preference mNavigationButtonColor;
    private Preference mNavigationButtonGlowColor;
    private SeekBarPreference mNavigationButtonGlowTime;
    private Preference mNavigationBarColor;

    private ContentResolver mContentResolver;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.nav_control);

        PreferenceScreen prefSet = getPreferenceScreen();

        mContext = getActivity().getApplicationContext();
        mContentResolver = mContext.getContentResolver();

        mCombinedBarNavigationForceMenu =
                (CheckBoxPreference) prefSet.findPreference(COMBINED_BAR_NAVIGATION_FORCE_MENU);

        mNavigationControls = (CheckBoxPreference) findPreference(KEY_NAVIGATION_CONTROLS);

        mCombinedBarNavigationForceMenu.setChecked((Settings.System.getInt(mContentResolver,
                Settings.System.TABLET_FORCE_MENU, 0) == 1));

        mNavigationBarColor = (Preference) prefSet.findPreference(NAVIGATION_BAR_COLOR);

        mNavigationAlignment =
                (ListPreference) prefSet.findPreference(KEY_NAVIGATION_ALIGNMENT);
        mNavigationAlignment.setOnPreferenceChangeListener(this);
        int navAlign = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.NAVIGATION_ALIGNMENT, 0);
        mNavigationAlignment.setValue(String.valueOf(navAlign));

        mNavigationButtonColor =
                (Preference) prefSet.findPreference(NAVIGATION_BUTTON_COLOR);

        mNavigationButtonGlowColor =
                (Preference) prefSet.findPreference(NAVIGATION_BUTTON_GLOW_COLOR);

        mNavigationButtonGlowTime =
                (SeekBarPreference) prefSet.findPreference(NAVIGATION_BUTTON_GLOW_TIME);
        mNavigationButtonGlowTime.setDefault(Settings.System.getInt(getActivity().getApplicationContext()
                .getContentResolver(), Settings.System.NAVIGATION_BUTTON_GLOW_TIME, 500));
        mNavigationButtonGlowTime.setOnPreferenceChangeListener(this);

        boolean tabletMode = Settings.System.getInt(mContentResolver,
                Settings.System.TABLET_MODE, mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_showTabletNavigationBar) ? 1 : 0) == 1;

        mNavigationControls.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.NAVIGATION_CONTROLS, mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar) ? 1 : 0) == 1);

        if (!tabletMode) {
            prefSet.removePreference(mCombinedBarNavigationForceMenu);
        } else {
            Preference naviBar = findPreference(KEY_NAVIGATION_BAR);
            prefSet.removePreference(naviBar);
            prefSet.removePreference(mNavigationAlignment);
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mNavigationControls) {
            value = mNavigationControls.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.NAVIGATION_CONTROLS,
                    value ? 1 : 0);
            return true;
        } else if (preference == mCombinedBarNavigationForceMenu) {
            value = mCombinedBarNavigationForceMenu.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.TABLET_FORCE_MENU, value ? 1 : 0);
            return true;
        } else if (preference == mNavigationButtonColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mButtonColorListener, Settings.System.getInt(mContentResolver,
                    Settings.System.NAVIGATION_BUTTON_COLOR,
                    getActivity().getApplicationContext().getResources().getColor(
                    com.android.internal.R.color.transparent)));
            cp.setDefaultColor(0x00000000);
            cp.show();
            return true;
        } else if (preference == mNavigationButtonGlowColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mGlowColorListener, Settings.System.getInt(mContentResolver,
                    Settings.System.NAVIGATION_BUTTON_GLOW_COLOR,
                    getActivity().getApplicationContext().getResources().getColor(
                    com.android.internal.R.color.transparent)));
            cp.setDefaultColor(0x00000000);
            cp.show();
            return true;
        } else if (preference == mNavigationBarColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mNavigationBarColorListener, Settings.System.getInt(getActivity()
                    .getApplicationContext()
                    .getContentResolver(), Settings.System.NAVIGATION_BAR_COLOR, 0xFF000000));
            cp.setDefaultColor(0xFF000000);
            cp.show();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNavigationAlignment) {
            String newVal = (String) newValue;
            int index = mNavigationAlignment.findIndexOfValue(newVal);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_ALIGNMENT, index);
        } else if (preference == mNavigationButtonGlowTime) {
            int value = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BUTTON_GLOW_TIME, value);
        }
        return true;
    }


    ColorPickerDialog.OnColorChangedListener mButtonColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.NAVIGATION_BUTTON_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };

    ColorPickerDialog.OnColorChangedListener mGlowColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.NAVIGATION_BUTTON_GLOW_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };

    ColorPickerDialog.OnColorChangedListener mNavigationBarColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.NAVIGATION_BAR_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };
}
