/*
 * Copyright (C) 2011 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cyanogenmod;

import com.android.internal.telephony.Phone;
import com.android.settings.R;

import android.content.Context;
/* import android.net.wimax.WimaxHelper; */
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CombinedBarSettingsUtil {
    public static final String BUTTON_WIFI = "toggleWifi";
    public static final String BUTTON_BLUETOOTH = "toggleBluetooth";
    public static final String BUTTON_BRIGHTNESS = "toggleBrightness";
    public static final String BUTTON_SOUND = "toggleSound";
    public static final String BUTTON_NOTIFICATIONS = "toggleNotifications";
    public static final String BUTTON_SETTINGS = "toggleSettings";
    public static final String BUTTON_AUTOROTATE = "toggleAutoRotate";
    public static final String BUTTON_AIRPLANE = "toggleAirplane";
    public static final String BUTTON_MEDIA = "toggleMedia";
    public static final String BUTTON_SLEEP = "toggleSleep";

    public static final HashMap<String, ButtonInfo> BUTTONS = new HashMap<String, ButtonInfo>();
    static {
        BUTTONS.put(BUTTON_SOUND, new CombinedBarSettingsUtil.ButtonInfo(
                BUTTON_SOUND, R.string.combined_bar_settings_volume,
                "com.android.systemui:drawable/stat_ring_on"));
        BUTTONS.put(BUTTON_BRIGHTNESS, new CombinedBarSettingsUtil.ButtonInfo(
                BUTTON_BRIGHTNESS, R.string.combined_bar_settings_brightness,
                "com.android.systemui:drawable/stat_brightness_on"));
        BUTTONS.put(BUTTON_AIRPLANE, new CombinedBarSettingsUtil.ButtonInfo(
                BUTTON_AIRPLANE, R.string.combined_bar_settings_airplane,
                "com.android.systemui:drawable/stat_airplane_on"));
        BUTTONS.put(BUTTON_WIFI, new CombinedBarSettingsUtil.ButtonInfo(
                BUTTON_WIFI, R.string.combined_bar_settings_wifi,
                "com.android.systemui:drawable/stat_wifi_on"));
        BUTTONS.put(BUTTON_BLUETOOTH, new CombinedBarSettingsUtil.ButtonInfo(
                BUTTON_BLUETOOTH, R.string.combined_bar_settings_bluetooth,
                "com.android.systemui:drawable/stat_bluetooth_on"));
        BUTTONS.put(BUTTON_AUTOROTATE, new CombinedBarSettingsUtil.ButtonInfo(
                BUTTON_AUTOROTATE, R.string.combined_bar_settings_autorotate,
                "com.android.systemui:drawable/stat_orientation_on"));
        BUTTONS.put(BUTTON_NOTIFICATIONS, new CombinedBarSettingsUtil.ButtonInfo(
                BUTTON_NOTIFICATIONS, R.string.combined_bar_settings_notifications,
                "com.android.systemui:drawable/ic_notification_open"));
        BUTTONS.put(BUTTON_SETTINGS, new CombinedBarSettingsUtil.ButtonInfo(
                BUTTON_SETTINGS, R.string.combined_bar_settings_shortcut,
                "com.android.systemui:drawable/ic_sysbar_quicksettings"));
        //BUTTONS.put(BUTTON_MEDIA, new CombinedBarSettingsUtil.ButtonInfo(
        //        BUTTON_MEDIA, R.string.combined_bar_settings_media,
        //        "com.android.systemui:drawable/stat_media_play"));
        BUTTONS.put(BUTTON_SLEEP, new CombinedBarSettingsUtil.ButtonInfo(
                BUTTON_SLEEP, R.string.combined_bar_settings_sleep,
                "com.android.systemui:drawable/stat_screen_timeout_on"));
    }

    private static final String BUTTON_DELIMITER = "|";
    private static final String BUTTONS_DEFAULT = BUTTON_BRIGHTNESS
            + BUTTON_DELIMITER + BUTTON_SOUND
            + BUTTON_DELIMITER + BUTTON_AUTOROTATE
            + BUTTON_DELIMITER + BUTTON_NOTIFICATIONS
            + BUTTON_DELIMITER + BUTTON_SETTINGS;

    public static String getCurrentButtons(Context context) {
        String buttons = Settings.System.getString(context.getContentResolver(),
                Settings.System.COMBINED_BAR_SETTINGS);
        if (buttons == null) {
            buttons = BUTTONS_DEFAULT;
        }
        return buttons;
    }

    public static void saveCurrentButtons(Context context, String buttons) {
        Settings.System.putString(context.getContentResolver(),
                Settings.System.COMBINED_BAR_SETTINGS, buttons);
    }

    public static String mergeInNewButtonString(String oldString, String newString) {
        ArrayList<String> oldList = getButtonListFromString(oldString);
        ArrayList<String> newList = getButtonListFromString(newString);
        ArrayList<String> mergedList = new ArrayList<String>();

        // add any items from oldlist that are in new list
        for (String button : oldList) {
            if (newList.contains(button)) {
                mergedList.add(button);
            }
        }

        // append anything in newlist that isn't already in the merged list to
        // the end of the list
        for (String button : newList) {
            if (!mergedList.contains(button)) {
                mergedList.add(button);
            }
        }

        // return merged list
        return getButtonStringFromList(mergedList);
    }

    public static ArrayList<String> getButtonListFromString(String buttons) {
        return new ArrayList<String>(Arrays.asList(buttons.split("\\|")));
    }

    public static String getButtonStringFromList(ArrayList<String> buttons) {
        if (buttons == null || buttons.size() <= 0) {
            return "";
        } else {
            String s = buttons.get(0);
            for (int i = 1; i < buttons.size(); i++) {
                s += BUTTON_DELIMITER + buttons.get(i);
            }
            return s;
        }
    }

    public static class ButtonInfo {
        private String mId;
        private int mTitleResId;
        private String mIcon;

        public ButtonInfo(String id, int titleResId, String icon) {
            mId = id;
            mTitleResId = titleResId;
            mIcon = icon;
        }

        public String getId() {
            return mId;
        }

        public int getTitleResId() {
            return mTitleResId;
        }

        public String getIcon() {
            return mIcon;
        }
    }
}
