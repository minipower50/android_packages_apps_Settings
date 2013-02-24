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

//    private static final String NAVIGATION_BAR_COLOR = "navigation_bar_color";
    private static final String KEY_NAVIGATION_CONTROLS = "navigation_controls";
    private static final String COMBINED_BAR_NAVIGATION_FORCE_MENU =
            "combined_bar_navigation_force_menu";
/*    private static final String COMBINED_BAR_NAVIGATION_COLOR = "combined_bar_navigation_color";
    private static final String COMBINED_BAR_NAVIGATION_GLOW = "combined_bar_navigation_glow";
    private static final String COMBINED_BAR_NAVIGATION_GLOW_COLOR =
            "combined_bar_navigation_glow_color";
    private static final String COMBINED_BAR_NAVIGATION_QUICK_GLOW =
            "combined_bar_navigation_quick_glow";
    private static final String HOME_BUTTON_SEARCH = "home_button_search";
*/
    private static final String KEY_NAVIGATION_BAR = "navigation_bar";
    private static final String KEY_NAVIGATION_ALIGNMENT = "nav_alignment";

    private CheckBoxPreference mNavigationControls;
    private CheckBoxPreference mCombinedBarNavigationForceMenu;
    private ListPreference mNavigationAlignment;

/*    private CheckBoxPreference mCombinedBarNavigationGlow;
    private CheckBoxPreference mCombinedBarNavigationQuickGlow;
    private Preference mCombinedBarNavigationGlowColor;
    private Preference mCombinedBarNavigationColor;
    private Preference mNavigationBarColor;

    private CheckBoxPreference mHomeButtonSearch;
*/
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
/*        mCombinedBarNavigationGlow =
                (CheckBoxPreference) prefSet.findPreference(COMBINED_BAR_NAVIGATION_GLOW);
        mCombinedBarNavigationQuickGlow =
                (CheckBoxPreference) prefSet.findPreference(COMBINED_BAR_NAVIGATION_QUICK_GLOW);
        mCombinedBarNavigationGlowColor =
                (Preference) prefSet.findPreference(COMBINED_BAR_NAVIGATION_GLOW_COLOR);
        mCombinedBarNavigationColor =
                (Preference) prefSet.findPreference(COMBINED_BAR_NAVIGATION_COLOR);*/
        mNavigationControls = (CheckBoxPreference) findPreference(KEY_NAVIGATION_CONTROLS);
        mCombinedBarNavigationForceMenu.setChecked((Settings.System.getInt(mContentResolver,
                Settings.System.TABLET_FORCE_MENU, 0) == 1));
/*        mNavigationBarColor = (Preference) prefSet.findPreference(NAVIGATION_BAR_COLOR);
        mHomeButtonSearch = (CheckBoxPreference) prefSet.findPreference(HOME_BUTTON_SEARCH);

        mCombinedBarNavigationGlow.setChecked((Settings.System.getInt(mContentResolver,
                Settings.System.COMBINED_BAR_NAVIGATION_GLOW, 1) == 1));

        mCombinedBarNavigationQuickGlow.setChecked((Settings.System.getInt(mContentResolver,
                Settings.System.COMBINED_BAR_NAVIGATION_GLOW_TIME, 0) == 1));

        mHomeButtonSearch.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.HOME_BUTTON_SEARCH, 1) == 1);
*/
        mNavigationAlignment =
                (ListPreference) prefSet.findPreference(KEY_NAVIGATION_ALIGNMENT);
        mNavigationAlignment.setOnPreferenceChangeListener(this);
        int navAlign = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.NAVIGATION_ALIGNMENT, 0);
        mNavigationAlignment.setValue(String.valueOf(navAlign));

        boolean tabletMode = Settings.System.getInt(mContentResolver,
                        Settings.System.TABLET_MODE, 0) > 0;

        mNavigationControls.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.NAVIGATION_CONTROLS, mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar) ? 1 : 0) == 1);

        if (Utils.isPhone(mContext) || !tabletMode) {
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
        }/* else if (preference == mCombinedBarNavigationGlow) {
            value = mCombinedBarNavigationGlow.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.COMBINED_BAR_NAVIGATION_GLOW, value ? 1 : 0);
            return true;
        } else if (preference == mCombinedBarNavigationQuickGlow) {
            value = mCombinedBarNavigationQuickGlow.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.COMBINED_BAR_NAVIGATION_GLOW_TIME, value ? 1 : 0);
            return true;
        } else if (preference == mCombinedBarNavigationGlowColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mGlowColorListener, Settings.System.getInt(mContentResolver,
                    Settings.System.COMBINED_BAR_NAVIGATION_GLOW_COLOR,
                    getActivity().getApplicationContext().getResources().getColor(
                    com.android.internal.R.color.holo_blue_light)));
            cp.setDefaultColor(0x00000000);
            cp.show();
            return true;
        } else if (preference == mCombinedBarNavigationColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mButtonColorListener, Settings.System.getInt(mContentResolver,
                    Settings.System.COMBINED_BAR_NAVIGATION_COLOR,
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
        } else if (preference == mHomeButtonSearch) {
            value = mHomeButtonSearch.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.HOME_BUTTON_SEARCH, value ? 1 : 0);
            return true;
        }*/
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNavigationAlignment) {
            String newVal = (String) newValue;
            int index = mNavigationAlignment.findIndexOfValue(newVal);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_ALIGNMENT, index);
        }
        return true;
    }

/*
    ColorPickerDialog.OnColorChangedListener mButtonColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.COMBINED_BAR_NAVIGATION_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };

    ColorPickerDialog.OnColorChangedListener mGlowColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.COMBINED_BAR_NAVIGATION_GLOW_COLOR, color);
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
    };*/
}
