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

public class StatusBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
    private static final String STATUS_BAR_BATTERY = "status_bar_battery";
    private static final String STATUS_BAR_CLOCK = "status_bar_show_clock";
    private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
    private static final String STATUS_BAR_SIGNAL = "status_bar_signal";
    private static final String COMBINED_BAR_AUTO_HIDE = "combined_bar_auto_hide";
    private static final String COMBINED_BAR_AUTO_HIDE_TIMEOUT = "combined_bar_auto_hide_timeout";
    private static final String STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
    private static final String STATUS_BAR_COLOR = "status_bar_color";
    private static final String NOTIFICATION_PANEL_COLOR = "notification_panel_color";
    private static final String KEY_TABLET_MODE = "tablet_mode";
    private static final String KEY_TABLET_UI = "tablet_ui";
    private static final String KEY_TABLET_FLIPPED = "tablet_flipped";
    private static final String STATUS_BAR_CLOCK_COLOR = "status_bar_clock_color";
    private static final String STATUS_BAR_CLOCK_CLICK = "status_bar_clock_click";
    private static final String PHONE_STYLE_RECENTS = "phone_style_recents";
    private static final String UMS_NOTIFICATION_CONNECT = "ums_notification_connect";
    private static final String RECENTS_PANEL_COLOR = "recents_panel_color";

    private ListPreference mStatusBarAmPm;
    private ListPreference mStatusBarBattery;
    private ListPreference mStatusBarCmSignal;
    private CheckBoxPreference mStatusBarClock;
    private CheckBoxPreference mStatusBarBrightnessControl;
    private CheckBoxPreference mCombinedBarAutoHide;
    private CheckBoxPreference mStatusBarNotifCount;

    private CheckBoxPreference mTabletMode;
    private CheckBoxPreference mTabletUI;
    private CheckBoxPreference mTabletFlipped;
    private Preference mStatusBarClockColor;
    private SeekBarPreference mCombinedBarTimeout;
    private Preference mStatusBarColor;
    private Preference mNotificationPanelColor;
    private CheckBoxPreference mClockClick;
    private CheckBoxPreference mPhoneStyleRecents;
    private CheckBoxPreference mUmsNotification;
    private Preference mRecentsPanelColor;

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
        mStatusBarBrightnessControl = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
        mStatusBarAmPm = (ListPreference) prefSet.findPreference(STATUS_BAR_AM_PM);
        mStatusBarBattery = (ListPreference) prefSet.findPreference(STATUS_BAR_BATTERY);
        mCombinedBarAutoHide = (CheckBoxPreference) prefSet.findPreference(COMBINED_BAR_AUTO_HIDE);
        mStatusBarCmSignal = (ListPreference) prefSet.findPreference(STATUS_BAR_SIGNAL);
        mStatusBarClockColor = (Preference) prefSet.findPreference(STATUS_BAR_CLOCK_COLOR);
        mStatusBarNotifCount = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NOTIF_COUNT);
        mTabletMode = (CheckBoxPreference) findPreference(KEY_TABLET_MODE);
        mTabletUI = (CheckBoxPreference) findPreference(KEY_TABLET_UI);
        mTabletFlipped = (CheckBoxPreference) findPreference(KEY_TABLET_FLIPPED);
        mClockClick = (CheckBoxPreference) findPreference(STATUS_BAR_CLOCK_CLICK);
        mPhoneStyleRecents = (CheckBoxPreference) findPreference(PHONE_STYLE_RECENTS);
        mUmsNotification = (CheckBoxPreference) findPreference(UMS_NOTIFICATION_CONNECT);

        mStatusBarAmPm.setOnPreferenceChangeListener(this);
        mStatusBarBattery.setOnPreferenceChangeListener(this);
        mStatusBarCmSignal.setOnPreferenceChangeListener(this);

        mCombinedBarTimeout = (SeekBarPreference) prefSet.findPreference(COMBINED_BAR_AUTO_HIDE_TIMEOUT);
        mCombinedBarTimeout.setDefault(Settings.System.getInt(getActivity().getApplicationContext()
                .getContentResolver(), Settings.System.FULLSCREEN_TIMEOUT, 2));
        mCombinedBarTimeout.setOnPreferenceChangeListener(this);

        mStatusBarColor = (Preference) prefSet.findPreference(STATUS_BAR_COLOR);
        mNotificationPanelColor = (Preference) prefSet.findPreference(NOTIFICATION_PANEL_COLOR);
        mRecentsPanelColor = (Preference) prefSet.findPreference(RECENTS_PANEL_COLOR);
    }

    public void onResume() {
        super.onResume();

        PreferenceScreen prefSet = getPreferenceScreen();

        mStatusBarClock.setChecked((Settings.System.getInt(mContentResolver,
                Settings.System.STATUS_BAR_CLOCK, 1) == 1));
        mStatusBarBrightnessControl.setChecked((Settings.System.getInt(mContentResolver,
                Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1));

        try {
            if (Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
                    == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
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

        int statusBarBattery = Settings.System.getInt(mContentResolver,
                Settings.System.STATUS_BAR_BATTERY, 0);
        mStatusBarBattery.setValue(String.valueOf(statusBarBattery));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());

        int signalStyle = Settings.System.getInt(mContentResolver,
                Settings.System.STATUS_BAR_SIGNAL_TEXT, 0);
        mStatusBarCmSignal.setValue(String.valueOf(signalStyle));
        mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntry());

        mCombinedBarAutoHide.setChecked((Settings.System.getInt(mContentResolver,
                Settings.System.FULLSCREEN_MODE, 0) == 1));

        mStatusBarNotifCount.setChecked((Settings.System.getInt(mContentResolver,
                Settings.System.STATUS_BAR_NOTIF_COUNT, 0) == 1));

        mTabletMode.setChecked(Settings.System.getInt(mContentResolver,
                        Settings.System.TABLET_MODE, 0) > 0);

        mTabletUI.setChecked(Settings.System.getInt(mContentResolver,
                        Settings.System.TABLET_MODE, 0) == 2);

        mTabletFlipped.setChecked(Settings.System.getInt(mContentResolver,
                        Settings.System.TABLET_FLIPPED, 0) == 1);

        mClockClick.setChecked(Settings.System.getInt(mContentResolver,
                        Settings.System.EXPANDED_CLOCK_ONCLICK, 0) == 1);

        mPhoneStyleRecents.setChecked(Settings.System.getInt(mContentResolver,
                        Settings.System.PHONE_STYLE_RECENTS, 0) == 1);

        mUmsNotification.setChecked(Settings.System.getInt(mContentResolver,
                        Settings.System.UMS_NOTIFICATION_CONNECT, 0) == 1);

        if (Utils.isHybrid(mContext)) {
            mTabletUI.setEnabled(mTabletMode.isChecked());
            mTabletFlipped.setEnabled(mTabletMode.isChecked());
            mStatusBarBrightnessControl.setEnabled(!mTabletMode.isChecked());
            mStatusBarCmSignal.setEnabled(!mTabletMode.isChecked());
            mPhoneStyleRecents.setEnabled(mTabletMode.isChecked());
        } else if (Utils.isTablet(mContext)) {
            mStatusBarBrightnessControl.setEnabled(false);
            mStatusBarCmSignal.setEnabled(false);
            mTabletFlipped.setEnabled(true);
        }

        mCombinedBarTimeout.setSummary(String.valueOf(mCombinedBarTimeout.getDefault()));
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
        } else if (preference == mCombinedBarTimeout) {
            int value = (Integer) newValue;
            Settings.System.putInt(mContentResolver, Settings.System.FULLSCREEN_TIMEOUT, value);
            mCombinedBarTimeout.setSummary(String.valueOf(value));
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
        } else if (preference == mCombinedBarAutoHide) {
            value = mCombinedBarAutoHide.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.FULLSCREEN_MODE, value ? 1 : 0);
            StatusBarManager sbm = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
            sbm.toggleVisibility();
            return true;
        } else if (preference == mStatusBarNotifCount) {
            value = mStatusBarNotifCount.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUS_BAR_NOTIF_COUNT, value ? 1 : 0);
            return true;
        } else if (preference == mTabletMode) {
            value = mTabletMode.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.TABLET_MODE,
                    value ? (mTabletUI.isChecked() ? 2 : 1) : 0);
            mTabletUI.setEnabled(value);
            mTabletFlipped.setEnabled(value);
            mPhoneStyleRecents.setEnabled(value);
            mStatusBarBrightnessControl.setEnabled(!value);
            mStatusBarCmSignal.setEnabled(!value);
            return true;
        } else if (preference == mTabletUI) {
            value = mTabletUI.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.TABLET_MODE,
                    value ? 2 : (mTabletMode.isChecked() ? 1 : 0));
            IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.checkService(
                    Context.WINDOW_SERVICE));
            try {
                wm.clearForcedDisplaySize();
            } catch (Exception e) {
            }
            return true;
        } else if (preference == mTabletFlipped) {
            value = mTabletFlipped.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.TABLET_FLIPPED,
                    value ? 1 : 0);
            return true;
        } else if (preference == mStatusBarClockColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mColorListener, Settings.System.getInt(mContentResolver,
                    Settings.System.STATUS_BAR_CLOCK_COLOR, 0xFF33B5E5));
            cp.setDefaultColor(0xFF33B5E5);
            cp.show();
            return true;
        } else if (preference == mStatusBarColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mStatusBarColorListener, Settings.System.getInt(getActivity()
                    .getApplicationContext()
                    .getContentResolver(), Settings.System.STATUS_BAR_COLOR, 0xFF000000));
            cp.setDefaultColor(0xFF000000);
            cp.show();
            return true;
        } else if (preference == mNotificationPanelColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mNotificationPanelColorListener, Settings.System.getInt(getActivity()
                    .getApplicationContext()
                    .getContentResolver(), Settings.System.NOTIFICATION_PANEL_COLOR, 0xFF000000));
            cp.setDefaultColor(0xFF000000);
            cp.show();
            return true;
        } else if (preference == mClockClick) {
            value = mClockClick.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.EXPANDED_CLOCK_ONCLICK, value ? 1 : 0);
            return true;
        } else if (preference == mPhoneStyleRecents) {
            value = mPhoneStyleRecents.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.PHONE_STYLE_RECENTS, value ? 1 : 0);
            return true;
        } else if (preference == mUmsNotification) {
            value = mUmsNotification.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.UMS_NOTIFICATION_CONNECT, value ? 1 : 0);
            return true;
        } else if (preference == mRecentsPanelColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mRecentsPanelColorListener, Settings.System.getInt(getActivity()
                    .getApplicationContext()
                    .getContentResolver(), Settings.System.RECENTS_PANEL_COLOR, 0xC0000000));
            cp.setDefaultColor(0xC0000000);
            cp.show();
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

    ColorPickerDialog.OnColorChangedListener mStatusBarColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.STATUS_BAR_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };

    ColorPickerDialog.OnColorChangedListener mNotificationPanelColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.NOTIFICATION_PANEL_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };

    ColorPickerDialog.OnColorChangedListener mRecentsPanelColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.RECENTS_PANEL_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };
}
