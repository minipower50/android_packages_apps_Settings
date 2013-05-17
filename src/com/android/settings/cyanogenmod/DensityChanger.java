
package com.android.settings.cyanogenmod;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.IPackageDataObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.util.CMDProcessor;
import com.android.settings.util.CMDProcessor.CommandResult;
import com.android.settings.util.Helpers;

public class DensityChanger extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "DensityChanger";

    Preference mReboot;
    Preference mClearMarketData;
    Preference mOpenMarket;
    ListPreference mCustomDensity;

    private static final int MSG_DATA_CLEARED = 500;

    private static final int DIALOG_CUSTOM_DENSITY = 101;
    private static final int DIALOG_WARN_DENSITY = 102;

    int newDensityValue;
    private Context mContext;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_DATA_CLEARED:
                    mClearMarketData.setSummary(R.string.lcd_density_market_data_cleared);
                    break;
            }

        };
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lcd_density);
        mContext = getActivity();

        String currentDensity = SystemProperties.get("ro.sf.lcd_density");
        PreferenceScreen prefs = getPreferenceScreen();

        mCustomDensity = (ListPreference) findPreference("lcd_density_picker");
        mCustomDensity.setOnPreferenceChangeListener(this);

        mReboot = findPreference("lcd_density_reboot");
        mClearMarketData = findPreference("lcd_density_clear_market_data");
        mOpenMarket = findPreference("open_market");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mReboot) {
            PowerManager pm = (PowerManager) getActivity()
                    .getSystemService(Context.POWER_SERVICE);
            pm.reboot(null);
            return true;

        } else if (preference == mClearMarketData) {

            new ClearMarketDataTask().execute("");
            return true;

        } else if (preference == mOpenMarket) {
            Intent openMarket = new Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_APP_MARKET)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName activityName = openMarket.resolveActivity(getActivity()
                    .getPackageManager());
            if (activityName != null) {
                mContext.startActivity(openMarket);
            } else {
                preference
                        .setSummary(getResources().getString(R.string.lcd_density_open_market_failed_text));
            }
            return true;

        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        LayoutInflater factory = LayoutInflater.from(mContext);

        switch (dialogId) {
            case DIALOG_CUSTOM_DENSITY:
                final View textEntryView = factory.inflate(
                        R.layout.custom_density_entry_dialog, null);
                return new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.lcd_density_enter_custom_density_title))
                        .setView(textEntryView)
                        .setPositiveButton(getResources().getString(R.string.lcd_density_enter_custom_density_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                EditText dpi = (EditText) textEntryView.findViewById(R.id.dpi_edit);
                                Editable text = dpi.getText();
                                Log.i(TAG, text.toString());

                                try {
                                    newDensityValue = Integer.parseInt(text.toString());
                                    showDialog(DIALOG_WARN_DENSITY);
                                } catch (Exception e) {
                                    mCustomDensity.setSummary(getResources().getString(R.string.lcd_density_invalid_text));
                                }

                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                dialog.dismiss();
                            }
                        }).create();
            case DIALOG_WARN_DENSITY:
                return new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.lcd_density_dialog_title))
                        .setMessage(
                                getResources().getString(R.string.lcd_density_dialog_summary))
                        .setCancelable(false)
                        .setNeutralButton(getResources().getString(R.string.lcd_density_dialog_button_ok), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setLcdDensity(newDensityValue);
                                dialog.dismiss();
                                mCustomDensity.setSummary(getResources().getString(R.string.lcd_density_changed_summary) + newDensityValue);

                            }
                        })
                        .setPositiveButton(getResources().getString(R.string.lcd_density_dialog_button_reboot), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setLcdDensity(newDensityValue);
                                PowerManager pm = (PowerManager) getActivity()
                                        .getSystemService(Context.POWER_SERVICE);
                                pm.reboot(null);
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .create();
        }
        return null;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mCustomDensity) {
            String strValue = (String) newValue;
            if (strValue.equals(getResources().getString(R.string.lcd_density_custom))) {
                showDialog(DIALOG_CUSTOM_DENSITY);
                return true;
            } else {
                newDensityValue = Integer.parseInt((String) newValue);
                showDialog(DIALOG_WARN_DENSITY);
                return true;
            }
        }

        return false;
    }

    private void setLcdDensity(int newDensity) {
        Helpers.getMount("rw");
        new CMDProcessor().su.runWaitFor("busybox sed -i 's|ro.sf.lcd_density=.*|"
                + "ro.sf.lcd_density" + "=" + newDensity + "|' " + "/system/build.prop");
        Helpers.getMount("ro");
    }

    class ClearUserDataObserver extends IPackageDataObserver.Stub {
        public void onRemoveCompleted(final String packageName, final boolean succeeded) {
            mHandler.sendEmptyMessage(MSG_DATA_CLEARED);
        }
    }

    private class ClearMarketDataTask extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... stuff) {
            String vending = "/data/data/com.android.vending/";
            CommandResult cr = new CMDProcessor().su.runWaitFor("ls " + vending);

            if (cr.stdout == null)
                return false;

            for (String dir : cr.stdout.split("\n")) {
                if (!dir.equals("lib")) {
                    String c = "rm -r " + vending + dir;
                    if (!new CMDProcessor().su.runWaitFor(c).success()) {
                        Log.e(TAG, c + " ... ERROR");
                        return false;
                    } else {
                        Log.i(TAG, c + " ... ok");
                    }
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            mClearMarketData.setSummary(result ? getResources().getString(R.string.lcd_density_market_data_cleared)
                    : getResources().getString(R.string.lcd_density_market_data_not_cleared));
        }
    }
}
