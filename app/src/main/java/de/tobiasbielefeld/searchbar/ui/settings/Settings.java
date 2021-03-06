/*
 * Copyright (C) 2017  Tobias Bielefeld
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you want to contact me, send me an e-mail at tobias.bielefeld@gmail.com
 */

package de.tobiasbielefeld.searchbar.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.List;

import de.tobiasbielefeld.searchbar.R;

import static de.tobiasbielefeld.searchbar.SharedData.*;

/**
 *  Settings activity created with the "Create settings activity" tool from Android Studio.
 */

public class Settings extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((ViewGroup) getListView().getParent()).setPadding(0, 0, 0, 0);                             //remove huge padding in landscape

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        reinitializeData(getApplicationContext());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //only item is the back arrow
        finish();
        return true;
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    public void onBuildHeaders(List<PreferenceActivity.Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    public void onResume() {
        super.onResume();

        sharedPref.registerOnSharedPreferenceChangeListener(this);
        showOrHideStatusBar();
        setOrientation();
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Update settings when the shared preferences get new values. It uses if/else instead
     * of switch/case because only this way i can use my static string variables, otherwise
     * I would need to write the strings manually in the cases.
     *
     * @param sharedPreferences Where the changes appeared
     * @param key The key with the changed value
     */
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(PREF_STATUS_BAR)) {
            showOrHideStatusBar();
        } else if (key.equals(PREF_ORIENTATION)) {
            setOrientation();
        } else if (key.equals(PREF_LANGUAGE)) {
            setLocale();
        }
    }

    /**
     * Tests if a loaded fragment is valid
     *
     * @param fragmentName The name of the fragment to test
     * @return True if it's valid, false otherwise
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || SearchPreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);

    }

    public static class SearchPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_search);
            setHasOptionsMenu(true);
        }
    }

    public static class GeneralPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
        }
    }


    /**
     * Applies the user setting of the screen orientation.
     */
    private void setOrientation() {
        switch (getSavedString(PREF_ORIENTATION, DEFAULT_ORIENTATION)) {
            case "1": //follow system settings
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                break;
            case "2": //portrait
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case "3": //landscape
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case "4": //landscape upside down
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
        }
    }

    /**
     * Applies the user setting of the status bar.
     */
    private void showOrHideStatusBar() {
        if (getSavedBoolean(PREF_STATUS_BAR, DEFAULT_STATUS_BAR))
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * Restarts the app to apply the new locale settings
     */
    private void setLocale() {
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
