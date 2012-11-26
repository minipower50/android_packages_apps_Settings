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
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class RecentApps extends SettingsPreferenceFragment {

    private static final String PHONE_STYLE_RECENTS = "phone_style_recents";
    private static final String RECENTS_PANEL_COLOR = "recents_panel_color";
    private static final String RECENT_APPS_CLEAR_ALL = "recent_apps_clear_all";

    private CheckBoxPreference mRecentAppsClearAll;
    private CheckBoxPreference mPhoneStyleRecents;
    private Preference mRecentsPanelColor;

    private CheckBoxPreference mHideLightsOut;

    private ContentResolver mContentResolver;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.recent_apps);

        PreferenceScreen prefSet = getPreferenceScreen();

        mContext = getActivity().getApplicationContext();
        mContentResolver = mContext.getContentResolver();

        mRecentAppsClearAll = (CheckBoxPreference) prefSet.findPreference(RECENT_APPS_CLEAR_ALL);
        mPhoneStyleRecents = (CheckBoxPreference) findPreference(PHONE_STYLE_RECENTS);
        mRecentsPanelColor = (Preference) prefSet.findPreference(RECENTS_PANEL_COLOR);

        boolean tabletMode = Settings.System.getInt(mContentResolver,
                        Settings.System.TABLET_MODE, 0) > 0;

        mRecentAppsClearAll.setChecked(Settings.System.getInt(mContentResolver,
                        Settings.System.RECENT_APPS_CLEAR_ALL, 1) == 1);

        mPhoneStyleRecents.setChecked(Settings.System.getInt(mContentResolver,
                        Settings.System.PHONE_STYLE_RECENTS, 0) == 1);

        if (Utils.isPhone(mContext) || !tabletMode) {
            prefSet.removePreference(mPhoneStyleRecents);
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mRecentAppsClearAll) {
            value = mRecentAppsClearAll.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.RECENT_APPS_CLEAR_ALL, value ? 1 : 0);
            return true;
        } else if (preference == mPhoneStyleRecents) {
            value = mPhoneStyleRecents.isChecked();
            Settings.System.putInt(mContentResolver,
                    Settings.System.PHONE_STYLE_RECENTS, value ? 1 : 0);
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
