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

import com.android.settings.R;

import android.content.Context;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class TabletNavigationUtil {
    public static final String BUTTON_BACK = "back";
    public static final String BUTTON_HOME = "home";
    public static final String BUTTON_RECENT = "recent";
    public static final String BUTTON_MENU = "menu";

    public static final HashMap<String, ButtonInfo> BUTTONS = new HashMap<String, ButtonInfo>();
    static {
        BUTTONS.put(BUTTON_BACK, new TabletNavigationUtil.ButtonInfo(
                BUTTON_BACK, R.string.edge_swipe_back,
                "com.android.systemui:drawable/ic_sysbar_back"));
        BUTTONS.put(BUTTON_HOME, new TabletNavigationUtil.ButtonInfo(
                BUTTON_HOME, R.string.edge_swipe_home,
                "com.android.systemui:drawable/ic_sysbar_home"));
        BUTTONS.put(BUTTON_RECENT, new TabletNavigationUtil.ButtonInfo(
                BUTTON_RECENT, R.string.edge_swipe_recents,
                "com.android.systemui:drawable/ic_sysbar_recent"));
        BUTTONS.put(BUTTON_MENU, new TabletNavigationUtil.ButtonInfo(
                BUTTON_MENU, R.string.edge_swipe_menu,
                "com.android.systemui:drawable/ic_sysbar_menu"));
    }

    private static final String BUTTON_DELIMITER = "|";
    private static final String BUTTONS_DEFAULT = BUTTON_BACK
            + BUTTON_DELIMITER + BUTTON_HOME
            + BUTTON_DELIMITER + BUTTON_RECENT
            + BUTTON_DELIMITER + BUTTON_MENU;

    public static String getCurrentButtons(Context context) {
        String buttons = Settings.System.getString(context.getContentResolver(),
                Settings.System.TABLET_BUTTONS);
        if (buttons == null) {
            buttons = BUTTONS_DEFAULT;
        }
        return buttons;
    }

    public static void saveCurrentButtons(Context context, String buttons) {
        Settings.System.putString(context.getContentResolver(),
                Settings.System.TABLET_BUTTONS, buttons);
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
