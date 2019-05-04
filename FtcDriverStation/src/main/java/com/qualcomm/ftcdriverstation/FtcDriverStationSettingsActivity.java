/*
 * Copyright (c) 2014-2016 Qualcomm Technologies Inc.
 * Copyright (c) 2018 OpenFTC Team
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of Qualcomm Technologies Inc nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * Qualcomm Technologies Inc., will periodically collect anonymous information
 * about the device this software is installed on such as the make, model, and
 * software versions, but no information that identifies you.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.qualcomm.ftcdriverstation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.google.gson.Gson;
import com.qualcomm.ftccommon.configuration.EditActivity;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.network.DeviceNameManager;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;

public class FtcDriverStationSettingsActivity extends EditActivity
{
    protected static final String CLIENT_CONNECTED = "CLIENT_CONNECTED";
    protected static final String RESULT = "RESULT";
    public static final String TAG = "FtcDriverStationSettingsActivity";
    protected boolean clientConnected = false;
    protected Result result = new Result();

    public static class Result
    {
        public boolean prefAdvancedClicked = false;
        public boolean prefLogsClicked = false;
        public boolean prefPairClicked = false;

        public String serialize()
        {
            return new Gson().toJson(this);
        }

        public static Result deserialize(String string)
        {
            return new Gson().fromJson(string, Result.class);
        }
    }

    public static class SettingsFragment extends PreferenceFragment
    {
        protected FtcDriverStationSettingsActivity activity;

        public void setActivity(FtcDriverStationSettingsActivity activity)
        {
            this.activity = activity;
        }

        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            boolean clientConnected = getArguments().getBoolean(FtcDriverStationSettingsActivity.CLIENT_CONNECTED);
            addPreferencesFromResource(R.xml.app_settings);
            if (!clientConnected)
            {
                findPreference(getString(R.string.pref_device_name_rc)).setEnabled(false);
                findPreference(getString(R.string.pref_app_theme_rc)).setEnabled(false);
                findPreference(getString(R.string.pref_sound_on_off_rc)).setEnabled(false);
            }
            findPreference(getString(R.string.pref_pair_rc)).setOnPreferenceClickListener(new OnPreferenceClickListener()
            {
                public boolean onPreferenceClick(Preference preference)
                {
                    RobotLog.vv(FtcDriverStationSettingsActivity.TAG, "prefPair clicked");
                    activity.result.prefPairClicked = true;
                    return false;
                }
            });
            findPreference(getString(R.string.pref_launch_advanced_rc_settings)).setOnPreferenceClickListener(new OnPreferenceClickListener()
            {
                public boolean onPreferenceClick(Preference preference)
                {
                    RobotLog.vv(FtcDriverStationSettingsActivity.TAG, "prefAdvanced clicked");
                    activity.result.prefAdvancedClicked = true;
                    return false;
                }
            });
            findPreference(getString(R.string.pref_debug_driver_station_logs)).setOnPreferenceClickListener(new OnPreferenceClickListener()
            {
                public boolean onPreferenceClick(Preference preference)
                {
                    RobotLog.vv(FtcDriverStationSettingsActivity.TAG, "prefLogs clicked");
                    activity.result.prefLogsClicked = true;
                    return false;
                }
            });
        }
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ds_settings);
        clientConnected = new PreferencesHelper(TAG, this).readBoolean(getString(R.string.pref_rc_connected), false);
        DeviceNameManager.getInstance().initializeDeviceNameIfNecessary();
        SettingsFragment settingsFragment = new SettingsFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(CLIENT_CONNECTED, clientConnected);
        settingsFragment.setArguments(arguments);
        settingsFragment.setActivity(this);
        getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
    }

    protected void finishOk()
    {
        Bundle bundle = new Bundle();
        bundle.putString(RESULT, this.result.serialize());
        Intent intent = new Intent();
        intent.putExtras(bundle);
        finishOk(intent);
    }
}