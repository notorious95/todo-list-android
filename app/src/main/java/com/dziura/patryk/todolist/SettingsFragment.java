package com.dziura.patryk.todolist;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.settings_preference);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int preferenceCount = preferenceScreen.getPreferenceCount();

        for (int i=0; i<preferenceCount; i++){
            Preference preference = preferenceScreen.getPreference(i);
            if (! (preference instanceof CheckBoxPreference)){
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
       Preference preference = findPreference(key);
       if (preference != null){
           if (! (preference instanceof CheckBoxPreference)) {
               String value = sharedPreferences.getString(key, "");
               setPreferenceSummary(preference, value);
           }
       }
    }

    private void setPreferenceSummary(Preference preference, String value){
        if (preference instanceof ListPreference){
            ListPreference listPreference = (ListPreference) preference;
            int preferenceIndex = listPreference.findIndexOfValue(value);
            listPreference.setSummary(listPreference.getEntries()[preferenceIndex]);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
