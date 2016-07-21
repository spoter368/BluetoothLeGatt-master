package com.amti.vela.bluetoothlegatt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.amti.vela.bluetoothlegatt.bluetooth.DeviceScanActivity;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    static SharedPreferences prefs;

    static EditTextPreference device;
    static CheckBoxPreference autoConnect, neverAsk;
    static Preference notificationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        overridePendingTransition(R.anim.slidein, R.anim.fadeout);

        //action bar
        final Toolbar actionBarToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(actionBarToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //status bar color
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.action_bar_dark_blue));
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
        getFragmentManager().beginTransaction().replace(R.id.settings_frame, new SettingsFragment()).commit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setSummaries();
    }

    public static void setSummaries()
    {
        String deviceString = prefs.getString(Preferences.PREFS_DEVICE_KEY, "");
        if(device != null)
        {
            device.setSummary(deviceString);
            device.setEnabled(autoConnect.isChecked());
        }

        String title = NotificationService.notificationsBound ? "Enable notification access (enabled)" : "Enable notification access (disabled)";
        notificationButton.setTitle(title);
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_items);

            device = (EditTextPreference)findPreference(Preferences.PREFS_DEVICE_KEY);
            autoConnect = (CheckBoxPreference)findPreference(Preferences.PREFS_AUTO_CONNECT_KEY);
            neverAsk = (CheckBoxPreference)findPreference(Preferences.PREFS_NEVER_ASK_KEY);
            notificationButton = findPreference(Preferences.PREFS_NOTIFICATION_KEY);

            notificationButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                    else
                    {
                        startActivity(new Intent(MainActivity.NOTIFICATION_SETTINGS_PACKAGE));
                    }
                    return true;
                }
            });

            setSummaries();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSummaries();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.fadein, R.anim.fade_and_scale_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}