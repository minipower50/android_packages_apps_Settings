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
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class UserInterface extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String DUAL_PANE_PREFS = "dual_pane_prefs";
    private static final String UMS_NOTIFICATION_CONNECT = "ums_notification_connect";
    private static final String LARGE_RECENT_THUMBS = "large_recent_thumbs";
    private static final String HIDE_USB_NOTIFICATION = "hide_usb_notification";
    private static final String LAUNCHER_MENU = "launcher_menu";

    private ListPreference mDualPanePrefs;
    private CheckBoxPreference mUmsNotificationConnect;
    private CheckBoxPreference mLargeRecentThumbs;
    private CheckBoxPreference mHideUsbNotification;
    private CheckBoxPreference mLauncherMenu;

    private Preference mRecentsColor;

    private ContentResolver mContentResolver;
    private Context mContext;

    private Preference mLcdDensity;
    int newDensityValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.user_interface);

        PreferenceScreen prefSet = getPreferenceScreen();

        mContentResolver = getContentResolver();

        mDualPanePrefs = (ListPreference) prefSet.findPreference(DUAL_PANE_PREFS);
        mDualPanePrefs.setOnPreferenceChangeListener(this);
        boolean preferMultiPane = getResources().getBoolean(
                com.android.internal.R.bool.preferences_prefer_dual_pane);
        int dualPane = Settings.System.getInt(mContentResolver,
                Settings.System.DUAL_PANE_PREFS, preferMultiPane ? 1 : 0);
        mDualPanePrefs.setValue(String.valueOf(dualPane));

        mUmsNotificationConnect = (CheckBoxPreference) prefSet.findPreference(UMS_NOTIFICATION_CONNECT);

        mUmsNotificationConnect.setChecked((Settings.System.getInt(mContentResolver,
                Settings.System.UMS_NOTIFICATION_CONNECT, 0) == 1));

        mHideUsbNotification = (CheckBoxPreference) prefSet.findPreference(HIDE_USB_NOTIFICATION);

        mHideUsbNotification.setChecked((Settings.System.getInt(mContentResolver,
                Settings.System.HIDE_USB_NOTIFICATION, 0) == 1));

        mLargeRecentThumbs = (CheckBoxPreference) prefSet.findPreference(LARGE_RECENT_THUMBS);

        mLargeRecentThumbs.setChecked((Settings.System.getInt(mContentResolver,
                Settings.System.LARGE_RECENT_THUMBS, 0) == 1));

        mRecentsColor =
                (Preference) prefSet.findPreference("recents_panel_color");

        mLcdDensity = findPreference("lcd_density_setup");
        String currentProperty = SystemProperties.get("ro.sf.lcd_density");
        try {
            newDensityValue = Integer.parseInt(currentProperty);
        } catch (Exception e) {
            getPreferenceScreen().removePreference(mLcdDensity);
        }

        mLcdDensity.setSummary(getResources().getString(R.string.lcd_density_current) + currentProperty);

        mLauncherMenu = (CheckBoxPreference) prefSet.findPreference(LAUNCHER_MENU);

        mLauncherMenu.setChecked((Settings.System.getInt(mContentResolver,
                Settings.System.LAUNCHER_MENU, 1) == 1));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDualPanePrefs) {
            int value = Integer.valueOf((String) newValue);
            Settings.System.putInt(mContentResolver,
                    Settings.System.DUAL_PANE_PREFS, value);
            getActivity().recreate();
            return true;
        }
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mUmsNotificationConnect) {
            value = mUmsNotificationConnect.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.UMS_NOTIFICATION_CONNECT, value ? 1 : 0);
            return true;
        } else if (preference == mLargeRecentThumbs) {
            value = mLargeRecentThumbs.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.LARGE_RECENT_THUMBS, value ? 1 : 0);
            return true;
        } else if (preference == mRecentsColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mRecentsColorListener, Settings.System.getInt(mContentResolver,
                    Settings.System.RECENTS_PANEL_COLOR, 0xe0000000));
            cp.setDefaultColor(0xe0000000);
            cp.show();
            return true;
        } else if (preference == mHideUsbNotification) {
            value = mHideUsbNotification.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.HIDE_USB_NOTIFICATION, value ? 1 : 0);
            return true;
        } else if (preference == mLcdDensity) {
            ((PreferenceActivity) getActivity())
                    .startPreferenceFragment(new DensityChanger(), true);
            return true;
        } else if (preference == mLauncherMenu) {
            value = mLauncherMenu.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.LAUNCHER_MENU, value ? 1 : 0);
            return true;
        }
        return false;
    }

    ColorPickerDialog.OnColorChangedListener mRecentsColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(mContentResolver,
                        Settings.System.RECENTS_PANEL_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };
}
