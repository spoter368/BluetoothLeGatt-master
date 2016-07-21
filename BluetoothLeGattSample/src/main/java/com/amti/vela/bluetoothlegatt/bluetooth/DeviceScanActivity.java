/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.amti.vela.bluetoothlegatt.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.amti.vela.bluetoothlegatt.CustomListAdapter;
import com.amti.vela.bluetoothlegatt.Preferences;
import com.amti.vela.bluetoothlegatt.R;
import com.amti.vela.bluetoothlegatt.MainActivity;
import com.amti.vela.bluetoothlegatt.SettingsActivity;

import java.util.ArrayList;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends AppCompatActivity {
    private final static String TAG = DeviceScanActivity.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000; // Stops scanning after 10 seconds.

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler leScanHandler;

    private ArrayList<BluetoothDevice> mLeDevicesList;
    private CustomListAdapter devicesAdapter;
    ListView deviceListView;
    SwipeRefreshLayout swipeContainer;

    boolean disableActionButton = false;

    boolean enableBtFlag;
    boolean checkedBt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_search);

        initGui();

        btInit();

        //have to request location permissions for marshmallow to do bluetooth
        if(Build.VERSION.SDK_INT >= 23)
        {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            { requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1); }

            if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            { requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 1); }
        }
        else
            autoConnectIfEnabled();
    }


    void autoConnectIfEnabled()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAutoConnect = prefs.getBoolean(Preferences.PREFS_AUTO_CONNECT_KEY, false);
        String deviceString = prefs.getString(Preferences.PREFS_DEVICE_KEY, "");
        if(isAutoConnect && deviceString.split("\n").length == 2 && mBluetoothAdapter.isEnabled())
        {
            final Intent intent = new Intent(DeviceScanActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRAS_DEVICE, deviceString);
            if (mScanning) {
                scanLeDevice(false);
                mScanning = false;
            }
            startActivity(intent);
        }
    }


    boolean isBtCompat()
    {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "This device does not support bluetooth", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "This device does not support bluetooth", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        return true;
    }

    void initGui()
    {
        leScanHandler = new Handler();

        //widgets
        deviceListView = (ListView)findViewById(R.id.listView);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        mLeDevicesList = new ArrayList<BluetoothDevice>();
        devicesAdapter = new CustomListAdapter(this, R.layout.custom_listview_item);
        deviceListView.setAdapter(devicesAdapter);

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.action_bar_light_blue,
                R.color.swipe_refresh_dark_green,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //action bar
        Toolbar actionBarToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(actionBarToolBar);
        actionBarToolBar.setTitle("Choose a Device");
        actionBarToolBar.setTitleTextColor(ContextCompat.getColor(this, R.color.action_bar_white));

        //status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3)
            {
                BluetoothDevice device = mLeDevicesList.get(position);
                if (device == null) return;
                final Intent intent = new Intent(DeviceScanActivity.this, MainActivity.class);
                String deviceName = device.getName() == null ? "Unknown Device" : device.getName();
                intent.putExtra(MainActivity.EXTRAS_DEVICE,  deviceName + "\n" + device.getAddress());
                if (mScanning) {
                    scanLeDevice(false);
                    mScanning = false;
                }

                startActivity(intent);
            }
        });

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the bt request has completed successfully
                clearDevices();
                scanLeDevice(true);
            }
        });
    }


    void btInit()
    {
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //with the swipe refresh, we need to manually set refresh to true through a runnable on app start or it doesn't work
        swipeContainer.post(new Runnable() {
            @Override
            public void run() {
                swipeContainer.setRefreshing(true);
            }
        });
    }

    void setSwipeContainerRefresh(final boolean refresh)
    {
        swipeContainer.post(new Runnable() {
            @Override
            public void run() {
                swipeContainer.setRefreshing(refresh);
            }
        });
    }

    void clearDevices()
    {
        mLeDevicesList.clear();
        devicesAdapter.clear();
        devicesAdapter.notifyDataSetChanged();
    }

    private void scanLeDevice(final boolean enable) {
        //pentuple check bt
        if(!enableBtIfNeeded())
        {
            //start scanning
            if (enable)
            {
                // this handler stops the scan after a period of time.
                leScanHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScanning = false;
                        disableActionButton = false;
                        setSwipeContainerRefresh(false);
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        Log.e(TAG, "Stopped scan");
                        invalidateOptionsMenu();
                    }
                }, SCAN_PERIOD);

                mScanning = true;
                disableActionButton = true;
                setSwipeContainerRefresh(true);
                mBluetoothAdapter.startLeScan(mLeScanCallback);
                Log.e(TAG, "Started scan");
            }

            //stop scanning
            else
            {
                mScanning = false;
                disableActionButton = false;
                setSwipeContainerRefresh(false);
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                Log.e(TAG, "Stopped scan");
            }
            invalidateOptionsMenu();
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
        new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //for the love of god, do not add the device if it already exists
                        boolean exists = false;
                        for (BluetoothDevice btDevice : mLeDevicesList)
                        {
                            if(device.getAddress().equals(btDevice.getAddress()))
                            {
                                exists = true;
                                break;
                            }
                        }
                        if(!exists)
                        {
                            mLeDevicesList.add(device);
                            if(device.getName() != null)
                                devicesAdapter.add(new ClipData.Item(device.getName()+"\n"+device.getAddress()));
                            else
                                devicesAdapter.add(new ClipData.Item("Unknown Device\n"+device.getAddress()));
                            devicesAdapter.notifyDataSetChanged();
                        }
                    }

                });

            }

        };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_search, menu);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //menu.findItem(R.id.menu_search).getIcon().setTint(ContextCompat.getColor(this, R.color.action_button_dark_blue));
        }


        if(!mScanning)
        {
            swipeContainer.setRefreshing(false);
        }
        if(disableActionButton)
            menu.findItem(R.id.menu_search).setEnabled(false);
        else
            menu.findItem(R.id.menu_search).setEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_search:
                mLeDevicesList.clear();
                devicesAdapter.clear();
                scanLeDevice(true);
                return true;
            case R.id.menu_settings:
                Intent intent = new Intent(DeviceScanActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        clearDevices();

        if(!checkedBt)
            scanLeDevice(true);
        else
        {
            scanLeDevice(false);
            checkedBt = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        enableBtFlag = false;
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {

        }
        else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            if(isBtCompat())
                autoConnectIfEnabled();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    boolean enableBtIfNeeded()
    {
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            //we have enableBtFlag so that we know if we already have one of these dialogs showing
            if (!mBluetoothAdapter.isEnabled() && !enableBtFlag) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                enableBtFlag = true;
                //checkedBt is for when onResume is called when we get back from the enable bt dialog, the user has
                //to manually start the bt search
                checkedBt = true;
            }
        }
        return enableBtFlag;
    }
}