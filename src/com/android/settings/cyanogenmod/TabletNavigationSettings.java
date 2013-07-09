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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ListFragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class TabletNavigationSettings extends SettingsPreferenceFragment {
    private static final String TAG = "TabletNavigationSettings";

    private static final String BUTTON_PICKER = "button_picker";
    private static final String BUTTON_ORDER = "button_order";
    private static final String COMBINED_BAR_NAVIGATION_FORCE_MENU =
            "combined_bar_navigation_force_menu";

    private PreferenceScreen mButtonPicker;
    private PreferenceScreen mButtonOrder;
    private CheckBoxPreference mCombinedBarNavigationForceMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getPreferenceManager() != null) {
            addPreferencesFromResource(R.xml.tablet_bar_settings);

            PreferenceScreen prefSet = getPreferenceScreen();

            mButtonPicker = (PreferenceScreen) prefSet.findPreference(BUTTON_PICKER);
            mButtonOrder = (PreferenceScreen) prefSet.findPreference(BUTTON_ORDER);

            mCombinedBarNavigationForceMenu =
                    (CheckBoxPreference) prefSet.findPreference(COMBINED_BAR_NAVIGATION_FORCE_MENU);

            mCombinedBarNavigationForceMenu.setChecked((Settings.System.getInt(getContentResolver(),
                    Settings.System.TABLET_FORCE_MENU, 0) == 1));
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mCombinedBarNavigationForceMenu) {
            boolean value = mCombinedBarNavigationForceMenu.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.TABLET_FORCE_MENU, value ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public static class ButtonChooser extends SettingsPreferenceFragment {

        public ButtonChooser() {
        }

        private static final String TAG = "ButtonChooserActivity";

        private static final String BUTTONS_CATEGORY = "pref_tablet_bar_buttons";
        private static final String SELECT_BUTTON_KEY_PREFIX = "pref_tablet_bar_button_";

        private HashMap<CheckBoxPreference, String> mCheckBoxPrefs = new HashMap<CheckBoxPreference, String>();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            addPreferencesFromResource(R.xml.tablet_bar_settings_chooser);

            PreferenceScreen prefSet = getPreferenceScreen();

            if (getActivity().getApplicationContext() == null) {
                return;
            }

            PreferenceCategory prefButtons = (PreferenceCategory) prefSet
                    .findPreference(BUTTONS_CATEGORY);

            // empty our preference category and set it to order as added
            prefButtons.removeAll();
            prefButtons.setOrderingAsAdded(false);

            // emtpy our checkbox map
            mCheckBoxPrefs.clear();

            // get our list of buttons
            ArrayList<String> buttonList = TabletNavigationUtil.getButtonListFromString(TabletNavigationUtil
                    .getCurrentButtons(getActivity().getApplicationContext()));

            // fill that checkbox map!
            for (TabletNavigationUtil.ButtonInfo button : TabletNavigationUtil.BUTTONS.values()) {
                if (button.getId() != null) {
                    // create a checkbox
                    CheckBoxPreference cb = new CheckBoxPreference(getActivity()
                            .getApplicationContext());

                    // set a dynamic key based on button id
                    cb.setKey(SELECT_BUTTON_KEY_PREFIX + button.getId());

                    // set vanity info
                    cb.setTitle(button.getTitleResId());

                    // set our checked state
                    if (buttonList.contains(button.getId())) {
                        cb.setChecked(true);
                    } else {
                        cb.setChecked(false);
                    }

                    // add to our prefs set
                    mCheckBoxPrefs.put(cb, button.getId());

                    // add to the category
                    prefButtons.addPreference(cb);
                }
            }
        }

        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                Preference preference) {
            // we only modify the button list if it was one of our checks that
            // was clicked
            boolean buttonWasModified = false;
            ArrayList<String> buttonList = new ArrayList<String>();
            for (Map.Entry<CheckBoxPreference, String> entry : mCheckBoxPrefs.entrySet()) {
                if (entry.getKey().isChecked()) {
                    buttonList.add(entry.getValue());
                }

                if (preference == entry.getKey()) {
                    buttonWasModified = true;
                }
            }

            if (buttonWasModified) {
                // now we do some wizardry and reset the button list
                TabletNavigationUtil.saveCurrentButtons(getActivity().getApplicationContext(),
                        TabletNavigationUtil.mergeInNewButtonString(
                                TabletNavigationUtil.getCurrentButtons(getActivity()
                                        .getApplicationContext()), TabletNavigationUtil
                                        .getButtonStringFromList(buttonList)));
                return true;
            }

            return false;
        }
    }

    public static class ButtonOrder extends ListFragment
    {
        private static final String TAG = "ButtonOrderActivity";

        private ListView mButtonList;
        private ButtonAdapter mButtonAdapter;
        View mContentView = null;
        Context mContext;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            mContentView = inflater.inflate(R.layout.order_power_widget_buttons_activity, null);
            return mContentView;
        }

        /** Called when the activity is first created. */
        // @Override
        // public void onCreate(Bundle icicle)
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mContext = getActivity().getApplicationContext();

            mButtonList = getListView();
            ((TouchInterceptor) mButtonList).setDropListener(mDropListener);
            mButtonAdapter = new ButtonAdapter(mContext);
            setListAdapter(mButtonAdapter);
        }

        @Override
        public void onDestroy() {
            ((TouchInterceptor) mButtonList).setDropListener(null);
            setListAdapter(null);
            super.onDestroy();
        }

        @Override
        public void onResume() {
            super.onResume();
            // reload our buttons and invalidate the views for redraw
            mButtonAdapter.reloadButtons();
            mButtonList.invalidateViews();
        }

        private TouchInterceptor.DropListener mDropListener = new TouchInterceptor.DropListener() {
            public void drop(int from, int to) {
                // get the current button list
                ArrayList<String> buttons = TabletNavigationUtil.getButtonListFromString(
                        TabletNavigationUtil.getCurrentButtons(mContext));

                // move the button
                if (from < buttons.size()) {
                    String button = buttons.remove(from);

                    if (to <= buttons.size()) {
                        buttons.add(to, button);

                        // save our buttons
                        TabletNavigationUtil.saveCurrentButtons(mContext,
                                TabletNavigationUtil.getButtonStringFromList(buttons));

                        // tell our adapter/listview to reload
                        mButtonAdapter.reloadButtons();
                        mButtonList.invalidateViews();
                    }
                }
            }
        };

        private class ButtonAdapter extends BaseAdapter {
            private Context mContext;
            private Resources mSystemUIResources = null;
            private LayoutInflater mInflater;
            private ArrayList<TabletNavigationUtil.ButtonInfo> mButtons;

            public ButtonAdapter(Context c) {
                mContext = c;
                mInflater = LayoutInflater.from(mContext);

                PackageManager pm = mContext.getPackageManager();
                if (pm != null) {
                    try {
                        mSystemUIResources = pm.getResourcesForApplication("com.android.systemui");
                    } catch (Exception e) {
                        mSystemUIResources = null;
                        Log.e(TAG, "Could not load SystemUI resources", e);
                    }
                }

                reloadButtons();
            }

            public void reloadButtons() {
                ArrayList<String> buttons = TabletNavigationUtil.getButtonListFromString(
                        TabletNavigationUtil.getCurrentButtons(mContext));

                mButtons = new ArrayList<TabletNavigationUtil.ButtonInfo>();
                for (String button : buttons) {
                    if (TabletNavigationUtil.BUTTONS.containsKey(button)) {
                        mButtons.add(TabletNavigationUtil.BUTTONS.get(button));
                    }
                }
            }

            public int getCount() {
                return mButtons.size();
            }

            public Object getItem(int position) {
                return mButtons.get(position);
            }

            public long getItemId(int position) {
                return position;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                final View v;
                if (convertView == null) {
                    v = mInflater.inflate(R.layout.order_power_widget_button_list_item, null);
                } else {
                    v = convertView;
                }

                TabletNavigationUtil.ButtonInfo button = mButtons.get(position);

                final TextView name = (TextView) v.findViewById(R.id.name);
                final ImageView icon = (ImageView) v.findViewById(R.id.icon);

                name.setText(button.getTitleResId());

                // assume no icon first
                icon.setVisibility(View.GONE);

                // attempt to load the icon for this button
                if (mSystemUIResources != null) {
                    int resId = mSystemUIResources.getIdentifier(button.getIcon(), null, null);
                    if (resId > 0) {
                        try {
                            Drawable d = mSystemUIResources.getDrawable(resId);
                            icon.setVisibility(View.VISIBLE);
                            icon.setImageDrawable(d);
                        } catch (Exception e) {
                            Log.e(TAG, "Error retrieving icon drawable", e);
                        }
                    }
                }

                return v;
            }
        }
    }

}
