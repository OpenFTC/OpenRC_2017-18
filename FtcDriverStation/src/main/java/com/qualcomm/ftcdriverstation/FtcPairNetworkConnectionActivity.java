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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.ScanResult;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.wifi.NetworkConnection;
import com.qualcomm.robotcore.wifi.NetworkConnection.Event;
import com.qualcomm.robotcore.wifi.NetworkConnection.NetworkConnectionCallback;
import com.qualcomm.robotcore.wifi.NetworkConnectionFactory;
import com.qualcomm.robotcore.wifi.NetworkType;
import com.qualcomm.robotcore.wifi.SoftApAssistant;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.firstinspires.ftc.robotcore.internal.network.CallbackResult;
import org.firstinspires.ftc.robotcore.internal.network.PreferenceRemoterDS;

public class FtcPairNetworkConnectionActivity extends Activity implements OnClickListener, NetworkConnectionCallback
{
    public static final String TAG = "FtcPairNetworkConnection";
    private String connectionOwnerIdentity;
    private String connectionOwnerPassword;
    private ScheduledFuture<?> discoveryFuture;
    private EditText editTextSoftApPassword;
    private boolean filterForTeam = true;
    private NetworkConnection networkConnection;
    private SharedPreferences sharedPref;
    private int teamNum;
    private TextView textViewSoftApPasswordLabel;

    public static class PeerRadioButton extends RadioButton
    {
        private String deviceIdentity = "";

        public PeerRadioButton(Context context)
        {
            super(context);
        }

        public String getDeviceIdentity()
        {
            return deviceIdentity;
        }

        public void setDeviceIdentity(String deviceIdentity)
        {
            this.deviceIdentity = deviceIdentity;
        }
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftc_network_connection);
        String networkType = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(NetworkConnectionFactory.NETWORK_CONNECTION_TYPE, NetworkType.WIFIDIRECT.toString());
        editTextSoftApPassword = findViewById(R.id.editTextSoftApPassword);
        textViewSoftApPasswordLabel = findViewById(R.id.textViewSoftApPasswordLabel);
        networkConnection = NetworkConnectionFactory.getNetworkConnection(NetworkConnectionFactory.getTypeFromString(networkType), getBaseContext());
        String deviceName = networkConnection.getDeviceName();
        if (deviceName.isEmpty())
        {
            teamNum = 0;
            deviceName = getString(R.string.wifi_direct_name_unknown);
        }
        else
        {
            teamNum = getTeamNumber(deviceName);
        }
        TextView instructions = findViewById(R.id.textWifiInstructions);
        TextView wifiName = findViewById(R.id.textViewWifiName);
        TextView label = findViewById(R.id.textViewWifiNameLabel);
        if (networkType.equalsIgnoreCase(NetworkType.WIFIDIRECT.toString()))
        {
            instructions.setText(getString(R.string.pair_instructions));
            wifiName.setVisibility(View.VISIBLE);
            wifiName.setText(deviceName);
            label.setVisibility(View.VISIBLE);
        }
        else if (networkType.equalsIgnoreCase(NetworkType.SOFTAP.toString()))
        {
            instructions.setText(getString(R.string.softap_instructions));
            wifiName.setVisibility(View.INVISIBLE);
            label.setVisibility(View.INVISIBLE);
        }
        ((Switch) findViewById(R.id.wifi_filter)).setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                filterForTeam = isChecked;
                updateDevicesList();
            }
        });
    }

    public void onStart()
    {
        super.onStart();
        RobotLog.ii(TAG, "Starting Pairing with Driver Station activity");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        connectionOwnerIdentity = sharedPref.getString(getString(R.string.pref_connection_owner_identity), getString(R.string.connection_owner_default));
        TextView softApPasswordInstructions = findViewById(R.id.textViewSoftApPasswordInstructions);
        if (networkConnection.getNetworkType() == NetworkType.SOFTAP)
        {
            connectionOwnerPassword = sharedPref.getString(getString(R.string.pref_connection_owner_password), getString(R.string.connection_owner_password_default));
            textViewSoftApPasswordLabel.setVisibility(View.VISIBLE);
            editTextSoftApPassword.setVisibility(View.VISIBLE);
            editTextSoftApPassword.setText(connectionOwnerPassword);
            softApPasswordInstructions.setVisibility(View.VISIBLE);
        }
        else
        {
            textViewSoftApPasswordLabel.setVisibility(View.INVISIBLE);
            editTextSoftApPassword.setVisibility(View.INVISIBLE);
            softApPasswordInstructions.setVisibility(View.INVISIBLE);
        }
        networkConnection.enable();
        networkConnection.setCallback(this);
        updateDevicesList();
        discoveryFuture = ThreadPool.getDefaultScheduler().scheduleAtFixedRate(new Runnable()
        {
            public void run()
            {
                networkConnection.discoverPotentialConnections();
            }
        }, 0, 10000, TimeUnit.MILLISECONDS);
    }

    public void onStop()
    {
        super.onStop();
        discoveryFuture.cancel(false);
        networkConnection.cancelPotentialConnections();
        networkConnection.disable();
        connectionOwnerPassword = editTextSoftApPassword.getText().toString();
        Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.pref_connection_owner_password), connectionOwnerPassword);
        editor.apply();
    }

    public void onClick(View view)
    {
        if (view instanceof PeerRadioButton)
        {
            PeerRadioButton button = (PeerRadioButton) view;
            if (button.getId() == 0)
            {
                connectionOwnerIdentity = getString(R.string.connection_owner_default);
                connectionOwnerPassword = getString(R.string.connection_owner_password_default);
            }
            else
            {
                connectionOwnerIdentity = button.getDeviceIdentity();
            }
            Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.pref_connection_owner_identity), connectionOwnerIdentity);
            editor.apply();
            RobotLog.ii(TAG, "Setting Driver Station name to " + connectionOwnerIdentity);
        }
    }

    public CallbackResult onNetworkConnectionEvent(Event event)
    {
        CallbackResult result = CallbackResult.NOT_HANDLED;
        switch (event)
        {
            case PEERS_AVAILABLE:
                updateDevicesList();
                return CallbackResult.HANDLED;
            default:
                return result;
        }
    }

    private int getTeamNumber(String name)
    {
        int dashPos = name.indexOf("-");
        if (dashPos == -1)
        {
            return 0;
        }

        try
        {
            return Integer.parseInt(name.substring(0, dashPos));
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    private void updateDevicesList()
    {
        RadioGroup rg = findViewById(R.id.radioGroupDevices);
        rg.clearCheck();
        rg.removeAllViews();
        PeerRadioButton b = new PeerRadioButton(this);
        String none = getString(R.string.connection_owner_default);
        b.setId(0);
        b.setText("None\nDo not pair with any device");
        b.setPadding(0, 0, 0, 24);
        b.setOnClickListener(this);
        b.setDeviceIdentity(none);
        if (connectionOwnerIdentity.equalsIgnoreCase(none))
        {
            b.setChecked(true);
        }
        rg.addView(b);
        int i = 1;
        Map<String, String> namesAndAddresses = new TreeMap<>();
        if (networkConnection.getNetworkType() == NetworkType.WIFIDIRECT)
        {
            namesAndAddresses = buildMap(((WifiDirectAssistant) networkConnection).getPeers());
        }
        else if (networkConnection.getNetworkType() == NetworkType.SOFTAP)
        {
            namesAndAddresses = buildResultsMap(((SoftApAssistant) networkConnection).getScanResults());
        }
        for (String deviceName : namesAndAddresses.keySet())
        {
            if (!filterForTeam || deviceName.contains(teamNum + "-") || deviceName.startsWith("0000-"))
            {
                String deviceIdentity = namesAndAddresses.get(deviceName);
                b = new PeerRadioButton(this);
                int i2 = i + 1;
                b.setId(i);
                String display = "";
                if (networkConnection.getNetworkType() == NetworkType.WIFIDIRECT)
                {
                    display = deviceName + "\n" + deviceIdentity;
                }
                else if (networkConnection.getNetworkType() == NetworkType.SOFTAP)
                {
                    display = deviceName;
                }
                b.setText(display);
                b.setPadding(0, 0, 0, 24);
                b.setDeviceIdentity(deviceIdentity);
                if (deviceIdentity.equalsIgnoreCase(connectionOwnerIdentity))
                {
                    b.setChecked(true);
                }
                b.setOnClickListener(this);
                rg.addView(b);
                i = i2;
            }
        }
    }

    public Map<String, String> buildResultsMap(List<ScanResult> results)
    {
        Map<String, String> map = new TreeMap<>();
        for (ScanResult result : results)
        {
            map.put(result.SSID, result.SSID);
        }
        return map;
    }

    public Map<String, String> buildMap(List<WifiP2pDevice> peers)
    {
        Map<String, String> map = new TreeMap<>();
        for (WifiP2pDevice peer : peers)
        {
            map.put(PreferenceRemoterDS.getInstance().getDeviceNameForWifiP2pGroupOwner(peer.deviceName), peer.deviceAddress);
        }
        return map;
    }
}