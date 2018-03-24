package com.dziura.patryk.todolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private String mSelectedTheme = "blue";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSharedPreferences();
        if (mSelectedTheme.equals("green"))
            setTheme(R.style.AppTheme);
        else
            setTheme(R.style.AppTheme2);
        setContentView(R.layout.activity_settings);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_theme_key))) {
            mSelectedTheme = sharedPreferences.getString(key,
                    getString(R.string.pref_theme_value_blue));
            Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage( getBaseContext().getPackageName() );
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }

    private void setupSharedPreferences(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSelectedTheme = sharedPreferences.getString(getString(R.string.pref_theme_key),
                getString(R.string.pref_theme_value_blue));

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }
}
