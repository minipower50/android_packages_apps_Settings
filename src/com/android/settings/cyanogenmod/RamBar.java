/*
 * Copyright (C) 2012 Slimroms Project
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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.util.Date;

public class RamBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "RamBar";

    private static final String RAM_BAR_MODE = "ram_bar_mode";
    private static final String RAM_BAR_COLOR_APP_MEM = "ram_bar_color_app_mem";
    private static final String RAM_BAR_COLOR_CACHE_MEM = "ram_bar_color_cache_mem";
    private static final String RAM_BAR_COLOR_TOTAL_MEM = "ram_bar_color_total_mem";

    static final int DEFAULT_MEM_COLOR = 0xff8d8d8d;
    static final int DEFAULT_CACHE_COLOR = 0xff00aa00;
    static final int DEFAULT_ACTIVE_APPS_COLOR = 0xff33b5e5;

    private ListPreference mRamBarMode;

    private Preference mRamBarAppMemColor;
    private Preference mRamBarCacheMemColor;
    private Preference mRamBarTotalMemColor;

    private ContentResolver mContentResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int intColor;
        String hexColor;

        addPreferencesFromResource(R.xml.ram_bar);

        PreferenceScreen prefSet = getPreferenceScreen();

        mContentResolver = getContentResolver();

        mRamBarMode = (ListPreference) prefSet.findPreference(RAM_BAR_MODE);
        int ramBarMode = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.RECENTS_RAM_BAR_MODE, 0);
        mRamBarMode.setValue(String.valueOf(ramBarMode));
        mRamBarMode.setSummary(mRamBarMode.getEntry());
        mRamBarMode.setOnPreferenceChangeListener(this);

        mRamBarAppMemColor = prefSet.findPreference(RAM_BAR_COLOR_APP_MEM);
        mRamBarCacheMemColor = prefSet.findPreference(RAM_BAR_COLOR_CACHE_MEM);
        mRamBarTotalMemColor = prefSet.findPreference(RAM_BAR_COLOR_TOTAL_MEM);

        updateRamBarOptions();
    }

    ColorPickerDialog.OnColorChangedListener mRamBarAppMemColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(mContentResolver,
                        Settings.System.RECENTS_RAM_BAR_ACTIVE_APPS_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };

    ColorPickerDialog.OnColorChangedListener mRamBarCacheMemColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(mContentResolver,
                        Settings.System.RECENTS_RAM_BAR_CACHE_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };

    ColorPickerDialog.OnColorChangedListener mRamBarTotalMemColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(mContentResolver,
                        Settings.System.RECENTS_RAM_BAR_MEM_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };

    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference == mRamBarMode) {
            int ramBarMode = Integer.valueOf((String) newValue);
            int index = mRamBarMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.RECENTS_RAM_BAR_MODE, ramBarMode);
            mRamBarMode.setSummary(mRamBarMode.getEntries()[index]);
            updateRamBarOptions();
            return true;
        }
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mRamBarAppMemColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mRamBarAppMemColorListener, Settings.System.getInt(getContentResolver(),
                    Settings.System.RECENTS_RAM_BAR_ACTIVE_APPS_COLOR, DEFAULT_ACTIVE_APPS_COLOR));
            cp.setDefaultColor(DEFAULT_ACTIVE_APPS_COLOR);
            cp.show();
            return true;
        } else if (preference == mRamBarCacheMemColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mRamBarCacheMemColorListener, Settings.System.getInt(getContentResolver(),
                    Settings.System.RECENTS_RAM_BAR_CACHE_COLOR, DEFAULT_CACHE_COLOR));
            cp.setDefaultColor(DEFAULT_CACHE_COLOR);
            cp.show();
            return true;
        } else if (preference == mRamBarTotalMemColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mRamBarTotalMemColorListener, Settings.System.getInt(getContentResolver(),
                    Settings.System.RECENTS_RAM_BAR_MEM_COLOR, DEFAULT_MEM_COLOR));
            cp.setDefaultColor(DEFAULT_MEM_COLOR);
            cp.show();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void updateRamBarOptions() {
        int ramBarMode = Settings.System.getInt(getActivity().getContentResolver(),
               Settings.System.RECENTS_RAM_BAR_MODE, 0);
        if (ramBarMode == 0) {
            mRamBarAppMemColor.setEnabled(false);
            mRamBarCacheMemColor.setEnabled(false);
            mRamBarTotalMemColor.setEnabled(false);
        } else if (ramBarMode == 1) {
            mRamBarAppMemColor.setEnabled(true);
            mRamBarCacheMemColor.setEnabled(false);
            mRamBarTotalMemColor.setEnabled(false);
        } else if (ramBarMode == 2) {
            mRamBarAppMemColor.setEnabled(true);
            mRamBarCacheMemColor.setEnabled(true);
            mRamBarTotalMemColor.setEnabled(false);
        } else {
            mRamBarAppMemColor.setEnabled(true);
            mRamBarCacheMemColor.setEnabled(true);
            mRamBarTotalMemColor.setEnabled(true);
        }
    }

}
