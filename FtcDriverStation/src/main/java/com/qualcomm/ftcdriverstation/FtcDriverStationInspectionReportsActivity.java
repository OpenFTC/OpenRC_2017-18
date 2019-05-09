/*
 * NOTE: This file was reverse engineered from its original binary form in the
 * official Driver Station APK file under the terms of the following license,
 * which is also specified in the root of the 'FtcRobotController' directory
 * of the official SDK repository (https://github.com/ftctechnh/ftc_app).
 *
 * Copyright (c) 2014-2016 Qualcomm Technologies Inc., FIRST contributors
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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.firstinspires.ftc.robotcore.internal.network.DeviceNameManager;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;

public class FtcDriverStationInspectionReportsActivity extends Activity
{
    protected static final String CLIENT_CONNECTED = "CLIENT_CONNECTED";
    public static final String TAG = "FtcDriverStationInspectionReportsActivity";

    public static class SettingsFragment extends PreferenceFragment
    {
        protected boolean clientConnected = false;

        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            clientConnected = getArguments().getBoolean(FtcDriverStationInspectionReportsActivity.CLIENT_CONNECTED);
            addPreferencesFromResource(R.xml.inspection);
            if (!clientConnected)
            {
                findPreference(getString(R.string.pref_launch_inspect_rc)).setEnabled(false);
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ds_inspection_reports);
        DeviceNameManager.getInstance().initializeDeviceNameIfNecessary();
        SettingsFragment settingsFragment = new SettingsFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(CLIENT_CONNECTED, new PreferencesHelper(TAG,this).readBoolean(getString(R.string.pref_rc_connected), false));
        settingsFragment.setArguments(arguments);
        getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
    }
}