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

import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
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

public class StatusBarClock extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
    private static final String STATUS_BAR_CLOCK = "status_bar_show_clock";
    private static final String STATUS_BAR_CLOCK_COLOR = "status_bar_clock_color";
    private static final String STATUS_BAR_CLOCK_CLICK = "status_bar_clock_click";

    private ListPreference mStatusBarAmPm;
    private CheckBoxPreference mStatusBarClock;
    private Preference mStatusBarClockColor;
    private CheckBoxPreference mClockClick;

    private ContentResolver mContentResolver;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar_clock);

        PreferenceScreen prefSet = getPreferenceScreen();

        mContext = getActivity().getApplicationContext();
        mContentResolver = mContext.getContentResolver();

        mStatusBarClock = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_CLOCK);
        mStatusBarAmPm = (ListPreference) prefSet.findPreference(STATUS_BAR_AM_PM);
        mStatusBarClockColor = (Preference) prefSet.findPreference(STATUS_BAR_CLOCK_COLOR);
        mClockClick = (CheckBoxPreference) findPreference(STATUS_BAR_CLOCK_CLICK);

        mStatusBarAmPm.setOnPreferenceChangeListener(this);
    }

    public void onResume() {
        super.onResume();

        PreferenceScreen prefSet = getPreferenceScreen();

        mStatusBarClock.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.STATUS_BAR_CLOCK, 1) == 1);

        try {
            if (Settings.System.getInt(mContentResolver,
                    Settings.System.TIME_12_24) == 24) {
                mStatusBarAmPm.setEnabled(false);
                mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
            }
        } catch (SettingNotFoundException e ) {
        }

        int statusBarAmPm = Settings.System.getInt(mContentResolver,
                Settings.System.STATUS_BAR_AM_PM, 2);
        mStatusBarAmPm.setValue(String.valueOf(statusBarAmPm));
        mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntry());


        mClockClick.setChecked(Settings.System.getInt(mContentResolver,
                        Settings.System.EXPANDED_CLOCK_ONCLICK, 0) == 1);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mStatusBarAmPm) {
            int statusBarAmPm = Integer.valueOf((String) newValue);
            int index = mStatusBarAmPm.findIndexOfValue((String) newValue);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUS_BAR_AM_PM, statusBarAmPm);
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntries()[index]);
            return true;
        }
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mStatusBarClock) {
            value = mStatusBarClock.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUS_BAR_CLOCK, value ? 1 : 0);
            return true;
        } else if (preference == mStatusBarClockColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mColorListener, Settings.System.getInt(mContentResolver,
                    Settings.System.STATUS_BAR_CLOCK_COLOR, 0xFF33B5E5));
            cp.setDefaultColor(0xFF33B5E5);
            cp.show();
            return true;
        } else if (preference == mClockClick) {
            value = mClockClick.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.EXPANDED_CLOCK_ONCLICK, value ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    ColorPickerDialog.OnColorChangedListener mColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.STATUS_BAR_CLOCK_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };
}
