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

import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class NotificationDrawer extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "NotificationDrawer";

    private static final String UI_COLLAPSE_BEHAVIOUR = "notification_drawer_collapse_on_dismiss";

    private ListPreference mCollapseOnDismiss;
    private Preference mNotificationPanelColor;
    private Preference mHeadsUp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.notification_drawer);

        mHeadsUp = findPreference(Settings.System.HEADS_UP_NOTIFICATION);

        // Notification drawer
        int collapseBehaviour = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_COLLAPSE_ON_DISMISS,
                Settings.System.STATUS_BAR_COLLAPSE_IF_NO_CLEARABLE);
        mCollapseOnDismiss = (ListPreference) findPreference(UI_COLLAPSE_BEHAVIOUR);
        mCollapseOnDismiss.setValue(String.valueOf(collapseBehaviour));
        mCollapseOnDismiss.setOnPreferenceChangeListener(this);
        updateCollapseBehaviourSummary(collapseBehaviour);

        mNotificationPanelColor =
                (Preference) findPreference("status_bar_notification_color");
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean headsUpEnabled = Settings.System.getInt(
                getContentResolver(), Settings.System.HEADS_UP_NOTIFICATION, 0) == 1;
        mHeadsUp.setSummary(headsUpEnabled
                ? R.string.summary_heads_up_enabled : R.string.summary_heads_up_disabled);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mCollapseOnDismiss) {
            int value = Integer.valueOf((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_COLLAPSE_ON_DISMISS, value);
            updateCollapseBehaviourSummary(value);
            return true;
        }

        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

       if (preference == mNotificationPanelColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mNotificationPanelColorListener, Settings.System.getInt(getContentResolver(),
                    Settings.System.NOTIFICATION_PANEL_COLOR, 0xff000000));
            cp.setDefaultColor(0xff000000);
            cp.show();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    ColorPickerDialog.OnColorChangedListener mNotificationPanelColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.NOTIFICATION_PANEL_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };

    private void updateCollapseBehaviourSummary(int setting) {
        String[] summaries = getResources().getStringArray(
                R.array.notification_drawer_collapse_on_dismiss_summaries);
        mCollapseOnDismiss.setSummary(summaries[setting]);
    }
}
