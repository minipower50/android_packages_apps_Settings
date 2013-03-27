/*
 * Copyright (C) 2012 The CyanogenMod project
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

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemSettings extends SettingsPreferenceFragment {
    private static final String TAG = "SystemSettings";

    private static final String KEY_NOTIFICATION_PULSE = "notification_pulse";
    private static final String KEY_BATTERY_LIGHT = "battery_light";
    private static final String KEY_HARDWARE_KEYS = "hardware_keys";
    private static final String KEY_NAVIGATION_BAR = "navigation_bar";
    private static final String KEY_LOCK_CLOCK = "lock_clock";
    private static final String KEY_STATUS_BAR = "status_bar";
    private static final String KEY_QUICK_SETTINGS = "quick_settings_panel";
    private static final String KEY_NOTIFICATION_DRAWER = "notification_drawer";
    private static final String KEY_POWER_MENU = "power_menu";
    private static final String KEY_NAVIGATION_CONTROL = "navigation_control";
    private static final String KEY_USER_INTERFACE = "user_interface";
    private static final String KEY_BAR_SETTINGS = "combined_bar_settings";
    private static final String KEY_PIE_CONTROL = "pie_control";

    private PreferenceScreen mNotificationPulse;
    private PreferenceScreen mBatteryPulse;
    private boolean mIsPrimary;
    private PreferenceScreen mBarSettings;
    private PreferenceScreen mQuickSettings;
    private PreferenceScreen mNotificationPanel;
    private PreferenceScreen mPieControl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.system_settings);
        PreferenceScreen prefScreen = getPreferenceScreen();

        // Determine which user is logged in
        mIsPrimary = UserHandle.myUserId() == UserHandle.USER_OWNER;
        if (mIsPrimary) {
            // Primary user only preferences
            // Battery lights
            mBatteryPulse = (PreferenceScreen) findPreference(KEY_BATTERY_LIGHT);
            if (mBatteryPulse != null) {
                if (getResources().getBoolean(
                        com.android.internal.R.bool.config_intrusiveBatteryLed) == false) {
                    prefScreen.removePreference(mBatteryPulse);
                } else {
                    updateBatteryPulseDescription();
                }
            }

            // Only show the hardware keys config on a device that does not have a navbar
            // and the navigation bar config on phones that has a navigation bar
            boolean removeKeys = false;
            boolean removeNavbar = false;
            if (getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys) == 0) {
                removeKeys = true;
            } else {
                removeNavbar = true;
            }

            // Act on the above
            if (removeKeys) {
                prefScreen.removePreference(findPreference(KEY_HARDWARE_KEYS));
            }

            //if (removeNavbar) {
            //    prefScreen.removePreference(findPreference(KEY_NAVIGATION_BAR));
            //}
        } else {
            // Secondary user is logged in, remove all primary user specific preferences
            prefScreen.removePreference(findPreference(KEY_BATTERY_LIGHT));
            prefScreen.removePreference(findPreference(KEY_HARDWARE_KEYS));
            //prefScreen.removePreference(findPreference(KEY_NAVIGATION_BAR));
            prefScreen.removePreference(findPreference(KEY_STATUS_BAR));
            prefScreen.removePreference(findPreference(KEY_QUICK_SETTINGS));
            prefScreen.removePreference(findPreference(KEY_POWER_MENU));
            prefScreen.removePreference(findPreference(KEY_NOTIFICATION_DRAWER));
            prefScreen.removePreference(findPreference(KEY_NAVIGATION_CONTROL));
            prefScreen.removePreference(findPreference(KEY_USER_INTERFACE));
            prefScreen.removePreference(mBarSettings);
        }

        // Preferences that applies to all users
        // Notification lights
        mNotificationPulse = (PreferenceScreen) findPreference(KEY_NOTIFICATION_PULSE);
        if (mNotificationPulse != null) {
            if (!getResources().getBoolean(com.android.internal.R.bool.config_intrusiveNotificationLed)) {
                prefScreen.removePreference(mNotificationPulse);
            } else {
                updateLightPulseDescription();
            }
        }

        // Pie controls
        mPieControl = (PreferenceScreen) findPreference(KEY_PIE_CONTROL);

        // Don't display the lock clock preference if its not installed
        removePreferenceIfPackageNotInstalled(findPreference(KEY_LOCK_CLOCK));
    }

    private void updateLightPulseDescription() {
        if (Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.NOTIFICATION_LIGHT_PULSE, 0) == 1) {
            mNotificationPulse.setSummary(getString(R.string.notification_light_enabled));
        } else {
            mNotificationPulse.setSummary(getString(R.string.notification_light_disabled));
        }
    }

    private void updateBatteryPulseDescription() {
        if (Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.BATTERY_LIGHT_ENABLED, 1) == 1) {
            mBatteryPulse.setSummary(getString(R.string.notification_light_enabled));
        } else {
            mBatteryPulse.setSummary(getString(R.string.notification_light_disabled));
        }
     }

    @Override
    public void onResume() {
        super.onResume();

        // All users
        if (mNotificationPulse != null) {
            updateLightPulseDescription();
        }

        // Primary user only
        if (mIsPrimary && mBatteryPulse != null) {
            updateBatteryPulseDescription();
        }

        if (mPieControl != null) {
            updatePieControlDescription();
        }

        mBarSettings = (PreferenceScreen) findPreference(KEY_BAR_SETTINGS);
        mQuickSettings = (PreferenceScreen) findPreference(KEY_QUICK_SETTINGS);
        mNotificationPanel = (PreferenceScreen) findPreference(KEY_NOTIFICATION_DRAWER);

        boolean tabletMode = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.TABLET_MODE,
                getActivity().getApplicationContext().getResources().getBoolean(
                com.android.internal.R.bool.config_showTabletNavigationBar) ? 1 : 0) == 1;

        if (!tabletMode && mBarSettings != null) {
            getPreferenceScreen().removePreference(mBarSettings);
        } else if (tabletMode) {
            if (mQuickSettings != null) getPreferenceScreen().removePreference(mQuickSettings);
            if (mNotificationPanel != null) getPreferenceScreen().removePreference(mNotificationPanel);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void updatePieControlDescription() {
        if (Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.PIE_CONTROLS, 0) == 1) {
            mPieControl.setSummary(getString(R.string.pie_control_enabled));
        } else {
            mPieControl.setSummary(getString(R.string.pie_control_disabled));
        }
    }

    private boolean removePreferenceIfPackageNotInstalled(Preference preference) {
        String intentUri = ((PreferenceScreen) preference).getIntent().toUri(1);
        Pattern pattern = Pattern.compile("component=([^/]+)/");
        Matcher matcher = pattern.matcher(intentUri);

        String packageName = matcher.find() ? matcher.group(1) : null;
        if (packageName != null) {
            try {
                getPackageManager().getPackageInfo(packageName, 0);
            } catch (NameNotFoundException e) {
                Log.e(TAG, "package " + packageName + " not installed, hiding preference.");
                getPreferenceScreen().removePreference(preference);
                return true;
            }
        }
        return false;
    }
}
