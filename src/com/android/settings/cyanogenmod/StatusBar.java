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

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class StatusBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
    private static final String STATUS_BAR_BATTERY = "status_bar_battery";
    private static final String STATUS_BAR_CLOCK = "status_bar_show_clock";
    private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
    private static final String STATUS_BAR_SIGNAL = "status_bar_signal";
    private static final String STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
    private static final String STATUS_BAR_CATEGORY_GENERAL = "status_bar_general";
    private static final String KEY_TABLET_UI = "tablet_ui";
    private static final String KEY_TABLET_FLIPPED = "tablet_flipped";
    private static final String KEY_TABLET_SCALED_ICONS = "tablet_scaled_icons";
    private static final String KEY_STATUS_BAR_LIGHTS_OUT = "status_bar_lights_out";

    private ListPreference mStatusBarAmPm;
    private ListPreference mStatusBarBattery;
    private ListPreference mStatusBarCmSignal;
    private CheckBoxPreference mStatusBarClock;
    private CheckBoxPreference mStatusBarBrightnessControl;
    private CheckBoxPreference mStatusBarNotifCount;
    private PreferenceCategory mPrefCategoryGeneral;
    private CheckBoxPreference mTabletUI;
    private CheckBoxPreference mTabletFlipped;
    private CheckBoxPreference mTabletScaledIcons;
    private CheckBoxPreference mStatusBarLightsOut;

    private Preference mClockColor;

    private ContentResolver mContentResolver;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar);

        PreferenceScreen prefSet = getPreferenceScreen();

        mContext = getActivity().getApplicationContext();

        mContentResolver = mContext.getContentResolver();

        mStatusBarClock = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_CLOCK);
        mStatusBarBrightnessControl =
                (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
        mStatusBarAmPm = (ListPreference) prefSet.findPreference(STATUS_BAR_AM_PM);
        mStatusBarBattery = (ListPreference) prefSet.findPreference(STATUS_BAR_BATTERY);
        mStatusBarCmSignal = (ListPreference) prefSet.findPreference(STATUS_BAR_SIGNAL);

        mStatusBarClock.setChecked((Settings.System.getInt(mContentResolver,
                Settings.System.STATUS_BAR_CLOCK, 1) == 1));
        mStatusBarBrightnessControl.setChecked((Settings.System.getInt(mContentResolver,
                Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1));

        try {
            if (Settings.System.getInt(mContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE) ==
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                mStatusBarBrightnessControl.setEnabled(false);
                mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
            }
        } catch (SettingNotFoundException e) {
        }

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
        mStatusBarAmPm.setOnPreferenceChangeListener(this);

        int statusBarBattery = Settings.System.getInt(mContentResolver,
                Settings.System.STATUS_BAR_BATTERY, 0);
        mStatusBarBattery.setValue(String.valueOf(statusBarBattery));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
        mStatusBarBattery.setOnPreferenceChangeListener(this);

        int signalStyle = Settings.System.getInt(mContentResolver,
                Settings.System.STATUS_BAR_SIGNAL_TEXT, 0);
        mStatusBarCmSignal.setValue(String.valueOf(signalStyle));
        mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntry());
        mStatusBarCmSignal.setOnPreferenceChangeListener(this);

        mStatusBarNotifCount = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NOTIF_COUNT);
        mStatusBarNotifCount.setChecked((Settings.System.getInt(mContentResolver,
                Settings.System.STATUS_BAR_NOTIF_COUNT, 0) == 1));

        mTabletUI = (CheckBoxPreference) findPreference(KEY_TABLET_UI);
        mTabletUI.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.TABLET_MODE, mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_showTabletNavigationBar) ? 1 : 0) == 1);

        mTabletFlipped = (CheckBoxPreference) findPreference(KEY_TABLET_FLIPPED);
        mTabletFlipped.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.TABLET_FLIPPED, 0) == 1);

        mTabletScaledIcons = (CheckBoxPreference) findPreference(KEY_TABLET_SCALED_ICONS);
        mTabletScaledIcons.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.TABLET_SCALED_ICONS, 1) == 1);

        mStatusBarLightsOut =
                (CheckBoxPreference) prefSet.findPreference(KEY_STATUS_BAR_LIGHTS_OUT);
        mStatusBarLightsOut.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.HIDE_SB_LIGHTS_OUT, 0) == 1);

        mPrefCategoryGeneral = (PreferenceCategory) findPreference(STATUS_BAR_CATEGORY_GENERAL);

        if (Utils.isWifiOnly(getActivity())) {
            mPrefCategoryGeneral.removePreference(mStatusBarCmSignal);
        }

        if (Utils.isTablet(getActivity())) {
            mPrefCategoryGeneral.removePreference(mStatusBarBrightnessControl);
        }

        mClockColor =
                (Preference) prefSet.findPreference("clock_color");
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mStatusBarAmPm) {
            int statusBarAmPm = Integer.valueOf((String) newValue);
            int index = mStatusBarAmPm.findIndexOfValue((String) newValue);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUS_BAR_AM_PM, statusBarAmPm);
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarBattery) {
            int statusBarBattery = Integer.valueOf((String) newValue);
            int index = mStatusBarBattery.findIndexOfValue((String) newValue);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUS_BAR_BATTERY, statusBarBattery);
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarCmSignal) {
            int signalStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarCmSignal.findIndexOfValue((String) newValue);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUS_BAR_SIGNAL_TEXT, signalStyle);
            mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntries()[index]);
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
        } else if (preference == mStatusBarBrightnessControl) {
            value = mStatusBarBrightnessControl.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, value ? 1 : 0);
            return true;
        } else if (preference == mStatusBarNotifCount) {
            value = mStatusBarNotifCount.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUS_BAR_NOTIF_COUNT, value ? 1 : 0);
            return true;
        } else if (preference == mTabletUI) {
            value = mTabletUI.isChecked();
            Settings.System.putInt(mContentResolver, Settings.System.TABLET_MODE,
                    value ? 1 : 0);
            return true;
        } else if (preference == mTabletFlipped) {
            value = mTabletFlipped.isChecked();
            Settings.System.putInt(mContentResolver, Settings.System.TABLET_FLIPPED,
                    value ? 1 : 0);
            return true;
        } else if (preference == mTabletScaledIcons) {
            value = mTabletScaledIcons.isChecked();
            Settings.System.putInt(mContentResolver, Settings.System.TABLET_SCALED_ICONS,
                    value ? 1 : 0);
            return true;
        } else if (preference == mStatusBarLightsOut) {
            value = mStatusBarLightsOut.isChecked();
            Settings.System.putInt(mContentResolver, Settings.System.HIDE_SB_LIGHTS_OUT,
                    value ? 1 : 0);
            return true;
        } else if (preference == mClockColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mClockColorListener, Settings.System.getInt(mContentResolver,
                    Settings.System.STATUS_BAR_CLOCK_COLOR, 0xff33b5e5));
            cp.setDefaultColor(0xff33b5e5);
            cp.show();
            return true;
        }
        return false;
    }

    ColorPickerDialog.OnColorChangedListener mClockColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.STATUS_BAR_CLOCK_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };
}
