package de.baensch.airsniffer.lifecycle;

import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import de.baensch.airsniffer.R;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        Preference preference_api_base_base = findPreference("edit_text_preference_api_base");
        preference_api_base_base.setOnPreferenceChangeListener(this);


        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String apiURL = sharedPrefs.getString(preference_api_base_base.getKey(), "");
        onPreferenceChange(preference_api_base_base, apiURL);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        preference.setSummary(newValue.toString());
        return true;
    }
}
