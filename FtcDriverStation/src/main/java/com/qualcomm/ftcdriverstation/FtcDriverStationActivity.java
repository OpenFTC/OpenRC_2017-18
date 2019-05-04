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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.util.Predicate;
import com.google.blocks.ftcdriverstation.RemoteProgrammingModeActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qualcomm.analytics.Analytics;
import com.qualcomm.ftccommon.AboutActivity;
import com.qualcomm.ftccommon.ClassManagerFactory;
import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.ftccommon.ConfigWifiDirectActivity;
import com.qualcomm.ftccommon.ConfigWifiDirectActivity.Flag;
import com.qualcomm.ftccommon.FtcEventLoopHandler;
import com.qualcomm.ftccommon.LaunchActivityConstantsList;
import com.qualcomm.ftccommon.LaunchActivityConstantsList.RequestCode;
import com.qualcomm.ftccommon.configuration.EditParameters;
import com.qualcomm.ftccommon.configuration.FtcLoadFileActivity;
import com.qualcomm.ftccommon.configuration.RobotConfigFile;
import com.qualcomm.ftccommon.configuration.RobotConfigFileManager;
import com.qualcomm.ftcdriverstation.FtcDriverStationSettingsActivity.Result;
import com.qualcomm.ftcdriverstation.GamepadIndicator.State;
import com.qualcomm.ftcdriverstation.OpModeSelectionDialogFragment.OpModeSelectionDialogListener;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.configuration.UserConfigurationTypeManager;
import com.qualcomm.robotcore.hardware.configuration.Utility;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.Heartbeat;
import com.qualcomm.robotcore.robocol.RobocolDatagram;
import com.qualcomm.robotcore.robocol.RobocolParsableBase;
import com.qualcomm.robotcore.robocol.TelemetryMessage;
import com.qualcomm.robotcore.robot.RobotState;
import com.qualcomm.robotcore.util.BatteryChecker;
import com.qualcomm.robotcore.util.BatteryChecker.BatteryStatus;
import com.qualcomm.robotcore.util.BatteryChecker.BatteryWatcher;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.ImmersiveMode;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.RollingAverage;
import com.qualcomm.robotcore.wifi.NetworkConnection;
import com.qualcomm.robotcore.wifi.NetworkConnection.Event;
import com.qualcomm.robotcore.wifi.NetworkConnection.NetworkConnectionCallback;
import com.qualcomm.robotcore.wifi.NetworkType;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.firstinspires.ftc.driverstation.internal.StopWatchDrawable;
import org.firstinspires.ftc.ftccommon.internal.ProgramAndManageActivity;
import org.firstinspires.ftc.robotcore.internal.network.CallbackResult;
import org.firstinspires.ftc.robotcore.internal.network.DeviceNameManager;
import org.firstinspires.ftc.robotcore.internal.network.DeviceNameManager.Callback;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.PreferenceRemoterDS;
import org.firstinspires.ftc.robotcore.internal.network.RecvLoopRunnable.RecvLoopCallback;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList.DismissDialog;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList.ShowDialog;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList.ShowProgress;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList.ShowToast;
import org.firstinspires.ftc.robotcore.internal.network.SendOnceRunnable.ClientCallback;
import org.firstinspires.ftc.robotcore.internal.network.SendOnceRunnable.Parameters;
import org.firstinspires.ftc.robotcore.internal.network.SocketConnect;
import org.firstinspires.ftc.robotcore.internal.network.StartResult;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta.Flavor;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.Assert;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;
import org.firstinspires.ftc.robotcore.internal.ui.FilledPolygonDrawable;
import org.firstinspires.ftc.robotcore.internal.ui.GamepadUser;
import org.firstinspires.ftc.robotcore.internal.ui.ProgressParameters;
import org.firstinspires.ftc.robotcore.internal.ui.ThemedActivity;
import org.firstinspires.ftc.robotcore.internal.ui.UILocation;
import org.firstinspires.inspection.DsInspectionActivity;

@SuppressLint({"DefaultLocale"})
public class FtcDriverStationActivity extends ThemedActivity implements NetworkConnectionCallback, RecvLoopCallback, OnSharedPreferenceChangeListener, OpModeSelectionDialogListener, BatteryWatcher, InputDeviceListener, ClientCallback
{
    protected static final float FULLY_OPAQUE = 1.0f;
    protected static final float PARTLY_OPAQUE = 0.3f;
    public static final String TAG = "DriverStation";
    protected double V12BatteryMin;
    protected String V12BatteryMinString;
    protected TextView activeConfigText;
    protected Analytics analytics;
    protected AppUtil appUtil = AppUtil.getInstance();
    protected BatteryChecker batteryChecker;
    protected View batteryInfo;
    protected Button buttonAutonomous;
    protected View buttonInit;
    protected View buttonInitStop;
    protected ImageButton buttonMenu;
    protected View buttonStart;
    protected ImageButton buttonStop;
    protected Button buttonTeleOp;
    protected View chooseOpModePrompt;
    protected boolean clientConnected;
    protected String connectionOwner;
    protected String connectionOwnerPassword;
    protected Context context;
    protected View controlPanelBack;
    protected TextView currentOpModeName;
    protected boolean debugLogging = false;
    protected final OpModeMeta defaultOpMode = new OpModeMeta("$Stop$Robot$");
    protected DeviceNameManagerCallback deviceNameManagerCallback = new DeviceNameManagerCallback();
    protected StartResult deviceNameManagerStartResult = new StartResult();
    protected ImageView dsBatteryIcon;
    protected TextView dsBatteryInfo;
    protected Map<GamepadUser, GamepadIndicator> gamepadIndicators = new HashMap<>();
    protected GamepadManager gamepadManager = new GamepadManager(this);
    protected Heartbeat heartbeatRecv = new Heartbeat();
    protected ImmersiveMode immersion;
    protected ElapsedTime lastUiUpdate = new ElapsedTime();
    private InputManager mInputManager;
    protected NetworkConnectionHandler networkConnectionHandler = NetworkConnectionHandler.getInstance();
    protected OpModeCountDownTimer opModeCountDown;
    protected boolean opModeUseTimer = false;
    protected List<OpModeMeta> opModes = new LinkedList<>();
    protected RelativeLayout phoneBattery;
    protected RollingAverage pingAverage = new RollingAverage(10);
    protected StartResult prefRemoterStartResult = new StartResult();
    protected SharedPreferences preferences;
    protected PreferencesHelper preferencesHelper;
    protected OpModeMeta queuedOpMode = defaultOpMode;
    protected ImageView rcBatteryIcon;
    protected TextView rcBatteryTelemetry;
    protected boolean remoteProgrammingModeFinished;
    protected TextView robotBatteryMinimum;
    protected TextView robotBatteryTelemetry;
    protected RobotConfigFileManager robotConfigFileManager;
    protected RobotState robotState;
    protected TextView systemTelemetry;
    @ColorInt
    protected int systemTelemetryOriginalColor;
    protected TextView textDeviceName;
    protected TextView textDsUiStateIndicator;
    protected TextView textPingStatus;
    protected TextView textTelemetry;
    protected TextView textWifiDirectStatus;
    protected boolean textWifiDirectStatusShowingRC;
    protected View timerAndTimerSwitch;
    protected UIState uiState = UIState.UNKNOWN;
    protected Thread uiThread;
    protected Utility utility;
    protected View wifiInfo;

    protected enum ControlPanelBack
    {
        NO_CHANGE,
        DIM,
        BRIGHT
    }

    private class OpModeCountDownTimer
    {
        public static final long MS_COUNTDOWN_INTERVAL = 30000;
        public static final long MS_PER_S = 1000;
        public static final long MS_TICK = 1000;
        public static final long TICK_INTERVAL = 1;
        private CountDownTimer countDownTimer = null;
        private boolean enabled = false;
        private long msRemaining = MS_COUNTDOWN_INTERVAL;
        private View timerStopWatch;
        private View timerSwitchOff;
        private View timerSwitchOn;
        private TextView timerText;

        public OpModeCountDownTimer()
        {
            timerStopWatch = findViewById(R.id.timerStopWatch);
            timerText = findViewById(R.id.timerText);
            timerSwitchOn = findViewById(R.id.timerSwitchOn);
            timerSwitchOff = findViewById(R.id.timerSwitchOff);
        }

        private void displaySecondsRemaining(long secondsRemaining)
        {
            if (enabled)
            {
                setTextView(timerText, String.valueOf(secondsRemaining));
            }
        }

        public void enable()
        {
            if (!enabled)
            {
                setVisibility(timerText, View.VISIBLE);
                setVisibility(timerStopWatch, View.GONE);
                setVisibility(timerSwitchOn, View.VISIBLE);
                setVisibility(timerSwitchOff, View.GONE);
                enabled = true;
                displaySecondsRemaining(getSecondsRemaining());
            }
        }

        public void disable()
        {
            setTextView(timerText, "");
            setVisibility(timerText, View.GONE);
            setVisibility(timerStopWatch, View.VISIBLE);
            setVisibility(timerSwitchOn, View.GONE);
            setVisibility(timerSwitchOff, View.VISIBLE);
            enabled = false;
        }

        public boolean isEnabled()
        {
            return enabled;
        }

        public void start()
        {
            if (enabled)
            {
                RobotLog.vv(FtcDriverStationActivity.TAG, "Starting to run current op mode for " + getSecondsRemaining() + " seconds");
                appUtil.synchronousRunOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        CountDownTimer existingTimer = OpModeCountDownTimer.this.countDownTimer;
                        if (existingTimer != null)
                        {
                            existingTimer.cancel();
                        }
                        OpModeCountDownTimer.this.countDownTimer = new CountDownTimer(OpModeCountDownTimer.this.msRemaining, MS_TICK)
                        {
                            public void onTick(long msRemaining)
                            {
                                assertUiThread();
                                OpModeCountDownTimer.this.setMsRemaining(msRemaining);
                                RobotLog.vv(FtcDriverStationActivity.TAG, "Running current op mode for " + (msRemaining / MS_PER_S) + " seconds");
                            }

                            public void onFinish()
                            {
                                assertUiThread();
                                RobotLog.vv(FtcDriverStationActivity.TAG, "Stopping current op mode, timer expired");
                                OpModeCountDownTimer.this.resetCountdown();
                                handleOpModeStop();
                            }
                        }.start();
                    }
                });
            }
        }

        public void stop()
        {
            appUtil.synchronousRunOnUiThread(new Runnable()
            {
                public void run()
                {
                    if (OpModeCountDownTimer.this.countDownTimer != null)
                    {
                        OpModeCountDownTimer.this.countDownTimer.cancel();
                        OpModeCountDownTimer.this.countDownTimer = null;
                    }
                }
            });
        }

        public void stopPreservingRemainingTime()
        {
            CountDownTimer existingTimer = countDownTimer;
            long msWhenStopped = msRemaining;
            if (existingTimer != null)
            {
                synchronized (existingTimer)
                {
                    msWhenStopped = msRemaining;
                }
            }
            stop();
            setMsRemaining(msWhenStopped);
        }

        public long getSecondsRemaining()
        {
            return msRemaining / MS_PER_S;
        }

        public void resetCountdown()
        {
            setMsRemaining(MS_COUNTDOWN_INTERVAL);
        }

        public void setMsRemaining(long msRemaining)
        {
            this.msRemaining = msRemaining;
            if (this.enabled)
            {
                displaySecondsRemaining(msRemaining / MS_PER_S);
            }
        }
    }

    protected enum UIState
    {
        UNKNOWN("U"),
        CANT_CONTINUE("E"),
        DISCONNECTED("X"),
        CONNNECTED("C"),
        WAITING_FOR_OPMODE_SELECTION("M"),
        WAITING_FOR_INIT_EVENT("K"),
        WAITING_FOR_START_EVENT("S"),
        WAITING_FOR_STOP_EVENT("P");

        public final String indicator;

        UIState(String indicator)
        {
            this.indicator = indicator;
        }
    }

    protected class DeviceNameManagerCallback implements Callback
    {
        public void onDeviceNameChanged(String newDeviceName)
        {
            displayDeviceName(newDeviceName);
        }
    }

    public void updateBatteryStatus(BatteryStatus status)
    {
        setTextView(dsBatteryInfo, Double.toString(status.percent) + "%");
        setBatteryIcon(status, dsBatteryIcon);
    }

    private void setBatteryIcon(final BatteryStatus status, final ImageView icon)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                if (status.percent <= 15.0)
                {
                    if (status.isCharging)
                    {
                        icon.setImageResource(R.drawable.icon_battery0_charging);
                    }
                    else
                    {
                        icon.setImageResource(R.drawable.icon_battery0);
                    }
                }
                else if (status.percent <= 45.0)
                {
                    if (status.isCharging)
                    {
                        icon.setImageResource(R.drawable.icon_battery25_charging);
                    }
                    else
                    {
                        icon.setImageResource(R.drawable.icon_battery25);
                    }
                }
                else if (status.percent <= 65.0)
                {
                    if (status.isCharging)
                    {
                        icon.setImageResource(R.drawable.icon_battery50_charging);
                    }
                    else
                    {
                        icon.setImageResource(R.drawable.icon_battery50);
                    }
                }
                else if (status.percent > 85.0)
                {
                    if (status.isCharging)
                    {
                        icon.setImageResource(R.drawable.icon_battery100_charging);
                    }
                    else
                    {
                        icon.setImageResource(R.drawable.icon_battery100);
                    }
                }
                else
                {
                    if (status.isCharging)
                    {
                        icon.setImageResource(R.drawable.icon_battery75_charging);
                    }
                    else
                    {
                        icon.setImageResource(R.drawable.icon_battery75);
                    }
                }
            }
        });
    }

    private void updateBatteryIndependence(boolean rcHasIndependentBattery)
    {
        RobotLog.vv(TAG, "updateBatteryIndependence(%s)", rcHasIndependentBattery);
        RelativeLayout relativeLayout = phoneBattery;
        if (!rcHasIndependentBattery)
        {
            relativeLayout.setVisibility(View.GONE);
        }
        else
        {
            relativeLayout.setVisibility(View.VISIBLE);
        }
    }

    public String getTag()
    {
        return TAG;
    }

    @SuppressLint("WrongViewCast")
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        RobotLog.vv(TAG, "onCreate()");
        uiThread = Thread.currentThread();
        setContentView(R.layout.activity_ftc_driver_station);
        context = this;
        utility = new Utility(this);
        opModeCountDown = new OpModeCountDownTimer();
        PreferenceManager.setDefaultValues(this, R.xml.app_settings, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferencesHelper = new PreferencesHelper(TAG, preferences);
        DeviceNameManager.getInstance().start(deviceNameManagerStartResult);
        PreferenceRemoterDS.getInstance().start(prefRemoterStartResult);
        setClientConnected(false);
        ClassManagerFactory.registerResourceFilters();
        ClassManagerFactory.processAllClasses();
        robotConfigFileManager = new RobotConfigFileManager(this);
        textDeviceName = findViewById(R.id.textDeviceName);
        textDsUiStateIndicator = findViewById(R.id.textDsUiStateIndicator);
        textWifiDirectStatus = findViewById(R.id.textWifiDirectStatus);
        textWifiDirectStatusShowingRC = false;
        textPingStatus = findViewById(R.id.textPingStatus);
        textTelemetry = findViewById(R.id.textTelemetry);
        systemTelemetry = findViewById(R.id.textSystemTelemetry);
        systemTelemetryOriginalColor = systemTelemetry.getCurrentTextColor();
        phoneBattery = findViewById(R.id.phoneBattery);
        rcBatteryTelemetry = findViewById(R.id.rcBatteryTelemetry);
        robotBatteryMinimum = findViewById(R.id.robotBatteryMinimum);
        rcBatteryIcon = findViewById(R.id.rc_battery_icon);
        dsBatteryInfo = findViewById(R.id.dsBatteryInfo);
        robotBatteryTelemetry = findViewById(R.id.robotBatteryTelemetry);
        dsBatteryIcon = findViewById(R.id.DS_battery_icon);
        immersion = new ImmersiveMode(getWindow().getDecorView());
        buttonInit = findViewById(R.id.buttonInit);
        buttonInitStop = findViewById(R.id.buttonInitStop);
        buttonStart = findViewById(R.id.buttonStart);
        controlPanelBack = findViewById(R.id.controlPanel);
        batteryInfo = findViewById(R.id.battery_info_layout);
        wifiInfo = findViewById(R.id.wifi_info_layout);
        ((ImageButton) findViewById(R.id.buttonStartArrow)).setImageDrawable(new FilledPolygonDrawable(((ColorDrawable) findViewById(R.id.buttonStartArrowColor).getBackground()).getColor(), 3));
        ((ImageView) findViewById(R.id.timerStopWatch)).setImageDrawable(new StopWatchDrawable(((ColorDrawable) findViewById(R.id.timerStopWatchColorHolder).getBackground()).getColor()));
        gamepadIndicators.put(GamepadUser.ONE, new GamepadIndicator(this, R.id.user1_icon_clicked, R.id.user1_icon_base));
        gamepadIndicators.put(GamepadUser.TWO, new GamepadIndicator(this, R.id.user2_icon_clicked, R.id.user2_icon_base));
        gamepadManager.setGamepadIndicators(gamepadIndicators);
        activeConfigText = findViewById(R.id.activeConfigName);
        activeConfigText.setText(" ");
        timerAndTimerSwitch = findViewById(R.id.timerAndTimerSwitch);
        buttonAutonomous = findViewById(R.id.buttonAutonomous);
        buttonTeleOp = findViewById(R.id.buttonTeleOp);
        currentOpModeName = findViewById(R.id.currentOpModeName);
        chooseOpModePrompt = findViewById(R.id.chooseOpModePrompt);
        buttonStop = findViewById(R.id.buttonStop);
        buttonMenu = findViewById(R.id.menu_buttons);
        buttonMenu.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                AppUtil.getInstance().openOptionsMenuFor(FtcDriverStationActivity.this);
            }
        });
        preferences.registerOnSharedPreferenceChangeListener(this);
        gamepadManager.open();
        analytics = new Analytics(context, Analytics.DS_COMMAND_STRING, new HardwareMap(this));
        batteryChecker = new BatteryChecker(this, this, (long) 300000);
        batteryChecker.startBatteryMonitoring();
        resetBatteryStats();
        mInputManager = (InputManager) getSystemService(INPUT_SERVICE);
        networkConnectionHandler.pushNetworkConnectionCallback(this);
        networkConnectionHandler.pushReceiveLoopCallback(this);
        startOrRestartNetwork();
        DeviceNameManager.getInstance().registerCallback(deviceNameManagerCallback);
    }

    protected void startOrRestartNetwork()
    {
        RobotLog.vv(TAG, "startOrRestartNetwork()");
        assumeClientDisconnect();
        showWifiStatus(false, getString(R.string.wifiStatusDisconnected));
        initializeNetwork();
    }

    protected void onStart()
    {
        super.onStart();
        RobotLog.onApplicationStart();
        RobotLog.vv(TAG, "onStart()");
        for (GamepadIndicator indicator : gamepadIndicators.values())
        {
            indicator.setState(State.INVISIBLE);
        }
    }

    protected void onResume()
    {
        super.onResume();
        RobotLog.vv(TAG, "onResume()");
        analytics.register();
        resetBatteryStats();
        mInputManager.registerInputDeviceListener(this, null);
        if (remoteProgrammingModeFinished)
        {
            networkConnectionHandler.sendCommand(new Command(CommandList.CMD_STOP_PROGRAMMING_MODE));
            remoteProgrammingModeFinished = false;
        }
    }

    protected void initializeNetwork()
    {
        updateLoggingPrefs();
        NetworkType networkType = networkConnectionHandler.getDefaultNetworkType(this);
        connectionOwner = preferences.getString(getString(R.string.pref_connection_owner_identity), getString(R.string.connection_owner_default));
        connectionOwnerPassword = preferences.getString(getString(R.string.pref_connection_owner_password), getString(R.string.connection_owner_password_default));
        networkConnectionHandler.init(NetworkConnectionHandler.newWifiLock(this), networkType, connectionOwner, connectionOwnerPassword, this);
        if (networkConnectionHandler.isNetworkConnected())
        {
            RobotLog.vv("Robocol", "Spoofing a Network Connection event...");
            onNetworkConnectionEvent(Event.CONNECTION_INFO_AVAILABLE);
        }
    }

    protected void onPause()
    {
        super.onPause();
        RobotLog.vv(TAG, "onPause()");
        analytics.unregister();
        gamepadManager.clearGamepadAssignments();
        mInputManager.unregisterInputDeviceListener(this);
        initDefaultOpMode();
    }

    protected void onStop()
    {
        super.onStop();
        RobotLog.vv(TAG, "onStop()");
        pingStatus("");
    }

    protected void onDestroy()
    {
        super.onDestroy();
        RobotLog.vv(TAG, "onDestroy()");
        gamepadManager.close();
        DeviceNameManager.getInstance().unregisterCallback(deviceNameManagerCallback);
        networkConnectionHandler.removeNetworkConnectionCallback(this);
        networkConnectionHandler.removeReceiveLoopCallback(this);
        shutdown();
        PreferenceRemoterDS.getInstance().stop(prefRemoterStartResult);
        DeviceNameManager.getInstance().stop(deviceNameManagerStartResult);
        RobotLog.cancelWriteLogcatToDisk();
    }

    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus)
        {
            immersion.cancelSystemUIHide();
        }
        else if (ImmersiveMode.apiOver19())
        {
            immersion.hideSystemUI();
        }
    }

    public void showToast(String msg)
    {
        appUtil.showToast(UILocation.ONLY_LOCAL, msg);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String prefName)
    {
        RobotLog.vv(TAG, "onSharedPreferenceChanged() pref=%s", prefName);
        if (prefName.equals(context.getString(R.string.pref_device_name_rc_display)))
        {
            final String rcName = sharedPreferences.getString(prefName, "");
            if (rcName.length() > 0)
            {
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        if (textWifiDirectStatusShowingRC)
                        {
                            textWifiDirectStatus.setText(rcName);
                        }
                    }
                });
            }
        }
        else if (prefName.equals(getString(R.string.pref_has_independent_phone_battery_rc)))
        {
            updateBatteryIndependence(preferences.getBoolean(getString(R.string.pref_has_independent_phone_battery_rc), true));
        }
        else if (prefName.equals(getString(R.string.pref_app_theme)))
        {
            restartForAppThemeChange(R.string.appThemeChangeRestartNotifyDS);
        }
        updateLoggingPrefs();
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.ftc_driver_station, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                startActivityForResult(new Intent(getBaseContext(), FtcDriverStationSettingsActivity.class), RequestCode.SETTINGS_DRIVER_STATION.ordinal());
                return true;

            case R.id.action_restart_robot:
                networkConnectionHandler.sendCommand(new Command(CommandList.CMD_RESTART_ROBOT));
                return true;

            case R.id.action_configure:
                EditParameters parameters = new EditParameters();
                Intent intentConfigure = new Intent(AppUtil.getDefContext(), FtcLoadFileActivity.class);
                parameters.putIntent(intentConfigure);
                startActivityForResult(intentConfigure, RequestCode.CONFIGURE_DRIVER_STATION.ordinal());
                return true;

            case R.id.action_programming_mode:
                networkConnectionHandler.sendCommand(new Command(CommandList.CMD_START_PROGRAMMING_MODE));
                return true;

            case R.id.action_program_and_manage:
                RobotLog.vv(TAG, "action_program_and_manage clicked");
                networkConnectionHandler.sendCommand(new Command(CommandList.CMD_START_DS_PROGRAM_AND_MANAGE));
                return true;

            case R.id.action_inspection_mode:
                startActivityForResult(new Intent(getBaseContext(), FtcDriverStationInspectionReportsActivity.class), RequestCode.INSPECTIONS.ordinal());
                return true;

            case R.id.action_about:
                Intent intent = new Intent(AppUtil.getDefContext(), AboutActivity.class);
                intent.putExtra(LaunchActivityConstantsList.ABOUT_ACTIVITY_CONNECTION_TYPE, networkConnectionHandler.getNetworkType());
                startActivity(intent);
                return true;

            case R.id.action_exit_app:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onActivityResult(int request, int result, Intent intent)
    {
        RobotLog.vv(TAG, "onActivityResult(request=%d)", request);
        if (request == RequestCode.SETTINGS_DRIVER_STATION.ordinal())
        {
            if (intent != null)
            {
                Result settingsResult = Result.deserialize(intent.getExtras().getString("RESULT"));
                if (settingsResult.prefLogsClicked)
                {
                    updateLoggingPrefs();
                }
                if (settingsResult.prefPairClicked)
                {
                    startOrRestartNetwork();
                }
                if (settingsResult.prefAdvancedClicked)
                {
                    networkConnectionHandler.sendCommand(new Command(CommandList.CMD_RESTART_ROBOT));
                }
            }
        }
        else if (request == RequestCode.CONFIGURE_DRIVER_STATION.ordinal())
        {
            requestUIState();
            networkConnectionHandler.sendCommand(new Command(CommandList.CMD_RESTART_ROBOT));
        }
        else if (request == RequestCode.PROGRAMMING_MODE.ordinal())
        {
            remoteProgrammingModeFinished = true;
        }
    }

    protected void updateLoggingPrefs()
    {
        debugLogging = preferences.getBoolean(getString(R.string.pref_debug_driver_station_logs), false);
        gamepadManager.setDebug(debugLogging);
    }

    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    public boolean dispatchGenericMotionEvent(MotionEvent event)
    {
        if (!Gamepad.isGamepadDevice(event.getDeviceId()))
        {
            return super.dispatchGenericMotionEvent(event);
        }
        handleGamepadEvent(event);
        return true;
    }

    public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (!Gamepad.isGamepadDevice(event.getDeviceId()))
        {
            return super.dispatchKeyEvent(event);
        }
        handleGamepadEvent(event);
        return true;
    }

    public CallbackResult onNetworkConnectionEvent(Event event)
    {
        CallbackResult result = CallbackResult.NOT_HANDLED;
        RobotLog.i("Received networkConnectionEvent: " + event.toString());
        String msg;
        switch (event)
        {
            case PEERS_AVAILABLE:
                if (networkConnectionHandler.isWifiDirect())
                {
                    onPeersAvailableWifiDirect();
                }
                else
                {
                    onPeersAvailableSoftAP();
                }
                result = CallbackResult.HANDLED;
                break;

            case CONNECTED_AS_GROUP_OWNER:
                RobotLog.ee(TAG, "Wifi Direct - connected as Group Owner, was expecting Peer");
                showWifiStatus(false, getString(R.string.wifiStatusErrorConnectedAsGroupOwner));
                ConfigWifiDirectActivity.launch(getBaseContext(), Flag.WIFI_DIRECT_DEVICE_NAME_INVALID);
                result = CallbackResult.HANDLED;
                break;

            case CONNECTING:
                showWifiStatus(false, getString(R.string.wifiStatusConnecting));
                result = CallbackResult.HANDLED;
                break;

            case CONNECTED_AS_PEER:
                showWifiStatus(false, getString(R.string.wifiStatusConnected));
                result = CallbackResult.HANDLED;
                break;

            case CONNECTION_INFO_AVAILABLE:
                showWifiStatus(true, getBestRobotControllerName());
                if (!NetworkConnection.isDeviceNameValid(networkConnectionHandler.getDeviceName()))
                {
                    RobotLog.ee(TAG, "Wifi-Direct device name contains non-printable characters");
                    ConfigWifiDirectActivity.launch(getBaseContext(), Flag.WIFI_DIRECT_DEVICE_NAME_INVALID);
                }
                else if (networkConnectionHandler.connectedWithUnexpectedDevice())
                {
                    showWifiStatus(false, getString(R.string.wifiStatusErrorWrongDevice));
                    if (networkConnectionHandler.isWifiDirect())
                    {
                        ConfigWifiDirectActivity.launch(getBaseContext(), Flag.WIFI_DIRECT_FIX_CONFIG);
                    }
                    else if (connectionOwner == null && connectionOwnerPassword == null)
                    {
                        showWifiStatus(false, getString(R.string.wifiStatusNotPaired));
                        return CallbackResult.HANDLED;
                    }
                    else
                    {
                        networkConnectionHandler.startConnection(connectionOwner, connectionOwnerPassword);
                    }
                    return CallbackResult.HANDLED;
                }
                networkConnectionHandler.handleConnectionInfoAvailable(SocketConnect.CONNECTION_OWNER);
                assumeClientConnectAndRefreshUI(ControlPanelBack.NO_CHANGE);
                result = CallbackResult.HANDLED;
                break;

            case DISCONNECTED:
                msg = getString(R.string.wifiStatusDisconnected);
                showWifiStatus(false, msg);
                RobotLog.vv(TAG, "Network Connection - " + msg);
                networkConnectionHandler.discoverPotentialConnections();
                result = CallbackResult.HANDLED;
                break;

            case ERROR:
                msg = getString(R.string.dsErrorMessage, networkConnectionHandler.getFailureReason());
                showWifiStatus(false, msg);
                RobotLog.vv(TAG, "Network Connection - " + msg);
                break;
        }
        return result;
    }

    private String getBestRobotControllerName()
    {
        return preferences.getString(context.getString(R.string.pref_device_name_rc_display), networkConnectionHandler.getConnectionOwnerName());
    }

    private void onPeersAvailableWifiDirect()
    {
        if (!networkConnectionHandler.connectingOrConnected())
        {
            onPeersAvailableSoftAP();
        }
    }

    private void onPeersAvailableSoftAP()
    {
        if (networkConnectionHandler.connectionMatches(getString(R.string.connection_owner_default)))
        {
            showWifiStatus(false, getString(R.string.wifiStatusNotPaired));
        }
        else
        {
            showWifiStatus(false, getString(R.string.wifiStatusSearching));
        }
        networkConnectionHandler.handlePeersAvailable();
    }

    public void onClickButtonInit(View view)
    {
        handleOpModeInit();
    }

    public void onClickButtonStart(View view)
    {
        handleOpModeStart();
    }

    public void onClickTimer(View view)
    {
        opModeUseTimer = !opModeUseTimer;
        enableAndResetTimer(opModeUseTimer);
    }

    protected void enableAndResetTimer(boolean enable)
    {
        if (enable)
        {
            stopTimerAndReset();
            opModeCountDown.enable();
        }
        else
        {
            opModeCountDown.disable();
        }
        opModeUseTimer = enable;
    }

    protected void enableAndResetTimerForQueued()
    {
        enableAndResetTimer(queuedOpMode.flavor == Flavor.AUTONOMOUS);
    }

    void stopTimerPreservingRemainingTime()
    {
        opModeCountDown.stopPreservingRemainingTime();
    }

    void stopTimerAndReset()
    {
        opModeCountDown.stop();
        opModeCountDown.resetCountdown();
    }

    public void onClickButtonAutonomous(View view)
    {
        showOpModeDialog(filterOpModes(new Predicate<OpModeMeta>()
        {
            public boolean apply(OpModeMeta opModeMeta)
            {
                return opModeMeta.flavor == Flavor.AUTONOMOUS;
            }
        }), R.string.opmodeDialogTitleAutonomous);
    }

    public void onClickButtonTeleOp(View view)
    {
        showOpModeDialog(filterOpModes(new Predicate<OpModeMeta>()
        {
            public boolean apply(OpModeMeta opModeMeta)
            {
                return opModeMeta.flavor == Flavor.TELEOP;
            }
        }), R.string.opmodeDialogTitleTeleOp);
    }

    protected List<OpModeMeta> filterOpModes(Predicate<OpModeMeta> filter)
    {
        List<OpModeMeta> result = new LinkedList<>();
        for (OpModeMeta opModeMeta : opModes)
        {
            if (filter.apply(opModeMeta))
            {
                result.add(opModeMeta);
            }
        }
        return result;
    }

    protected void showOpModeDialog(List<OpModeMeta> opModes, @StringRes int menuTitle)
    {
        stopTimerPreservingRemainingTime();
        initDefaultOpMode();
        OpModeSelectionDialogFragment opModeSelectionDialogFragment = new OpModeSelectionDialogFragment();
        opModeSelectionDialogFragment.setOnSelectionDialogListener(this);
        opModeSelectionDialogFragment.setOpModes(opModes);
        opModeSelectionDialogFragment.setTitle(menuTitle);
        opModeSelectionDialogFragment.show(getFragmentManager(), "op_mode_selection");
    }

    public void onClickButtonStop(View view)
    {
        handleOpModeStop();
    }

    public void onOpModeSelectionClick(OpModeMeta selection)
    {
        handleOpModeQueued(selection);
    }

    protected void shutdown()
    {
        networkConnectionHandler.stop();
        networkConnectionHandler.shutdown();
    }

    public CallbackResult packetReceived(RobocolDatagram packet) throws RobotCoreException
    {
        return CallbackResult.NOT_HANDLED;
    }

    public void peerConnected(boolean peerLikelyChanged)
    {
        if (!clientConnected || peerLikelyChanged)
        {
            RobotLog.vv(TAG, "robot controller connected");
            assumeClientConnectAndRefreshUI(ControlPanelBack.NO_CHANGE);
        }
    }

    public void peerDisconnected()
    {
        if (clientConnected)
        {
            RobotLog.vv(TAG, "robot controller disconnected");
            assumeClientDisconnect();
        }
    }

    public CallbackResult peerDiscoveryEvent(RobocolDatagram packet) throws RobotCoreException
    {
        networkConnectionHandler.updateConnection(packet, new Parameters(gamepadManager), this);
        return CallbackResult.HANDLED;
    }

    public CallbackResult heartbeatEvent(RobocolDatagram packet, long tReceived)
    {
        try
        {
            heartbeatRecv.fromByteArray(packet.getData());
            RobotLog.processTimeSynch(heartbeatRecv.t0, heartbeatRecv.t1, heartbeatRecv.t2, tReceived);
            double elapsedSeconds = heartbeatRecv.getElapsedSeconds();
            int sequenceNumber = heartbeatRecv.getSequenceNumber();
            setRobotState(RobotState.fromByte(heartbeatRecv.getRobotState()));
            pingAverage.addNumber((int) (1000.0d * elapsedSeconds));
            if (lastUiUpdate.time() > 0.5d)
            {
                lastUiUpdate.reset();
                pingStatus(String.format("%dms", pingAverage.getAverage()));
            }
        } catch (RobotCoreException e)
        {
            RobotLog.logStackTrace(e);
        }
        return CallbackResult.HANDLED;
    }

    protected void setRobotState(RobotState state)
    {
        if (robotState != state)
        {
            robotState = state;
        }
    }

    protected CallbackResult handleNotifyRobotState(String extra)
    {
        setRobotState(RobotState.fromByte(Integer.valueOf(extra)));
        return CallbackResult.HANDLED;
    }

    public CallbackResult commandEvent(Command command)
    {
        CallbackResult result = CallbackResult.NOT_HANDLED;

        try
        {
            String name = command.getName();
            String extra = command.getExtra();

            switch (name)
            {
                case RobotCoreCommandList.CMD_NOTIFY_OP_MODE_LIST:
                    return handleCommandNotifyOpModeList(extra);

                case RobotCoreCommandList.CMD_SHOW_TOAST:
                    return handleCommandShowToast(extra);

                case RobotCoreCommandList.CMD_DISMISS_DIALOG:
                    return handleCommandDismissDialog(command);

                case RobotCoreCommandList.CMD_DISMISS_PROGRESS:
                    return handleCommandDismissProgress();

                case RobotCoreCommandList.CMD_SHOW_DIALOG:
                    return handleCommandShowDialog(extra);

                case RobotCoreCommandList.CMD_ROBOT_CONTROLLER_PREFERENCE:
                    return PreferenceRemoterDS.getInstance().handleCommandRobotControllerPreference(extra);

                case RobotCoreCommandList.CMD_SHOW_PROGRESS:
                    return handleCommandShowProgress(extra);

                case RobotCoreCommandList.CMD_NOTIFY_ACTIVE_CONFIGURATION:
                    return handleCommandNotifyActiveConfig(extra);

                case RobotCoreCommandList.CMD_NOTIFY_ROBOT_STATE:
                    return handleNotifyRobotState(extra);

                case RobotCoreCommandList.CMD_NOTIFY_INIT_OP_MODE:
                    return handleCommandNotifyInitOpMode(extra);

                case RobotCoreCommandList.CMD_NOTIFY_RUN_OP_MODE:
                    return handleCommandNotifyStartOpMode(extra);

                case RobotCoreCommandList.CMD_NOTIFY_USER_DEVICE_LIST:
                    return handleCommandNotifyUserDeviceList(extra);

                case RobotCoreCommandList.CMD_DISMISS_ALL_DIALOGS:
                    return handleCommandDismissAllDialogs(command);

                case CommandList.CMD_START_DS_PROGRAM_AND_MANAGE_RESP:
                    return handleCommandStartProgramAndManageResp(extra);

                case CommandList.CMD_START_PROGRAMMING_MODE_RESP:
                    return handleCommandStartProgrammingModeResp(extra);

                default:
                    return result;
            }

        } catch (Exception e)
        {
            RobotLog.logStackTrace(e);
            return result;
        }
    }

    public CallbackResult telemetryEvent(RobocolDatagram packet)
    {
        StringBuilder telemetryString = new StringBuilder();
        try
        {
            TelemetryMessage telemetry = new TelemetryMessage(packet.getData());
            if (telemetry.getRobotState() != RobotState.UNKNOWN)
            {
                setRobotState(telemetry.getRobotState());
            }
            boolean userKeyPresent = false;
            Map<String, String> strings = telemetry.getDataStrings();
            for (String key : telemetry.isSorted() ? new TreeSet<>(strings.keySet()) : strings.keySet())
            {
                if (key.equals(EventLoopManager.ROBOT_BATTERY_LEVEL_KEY))
                {
                    showRobotBatteryVoltage(strings.get(key));
                }
                else
                {
                    userKeyPresent = true;
                    if (key.length() > 0 && key.charAt(0) != '\u0000')
                    {
                        telemetryString.append(key).append(": ");
                    }
                    telemetryString.append(strings.get(key)).append("\n");
                }
            }
            telemetryString.append("\n");

            String tag = telemetry.getTag();

            switch (tag)
            {
                case EventLoopManager.SYSTEM_NONE_KEY:
                    clearSystemTelemetry();
                    break;

                case EventLoopManager.SYSTEM_ERROR_KEY:
                    reportGlobalError(strings.get(tag), true);
                    break;

                case EventLoopManager.SYSTEM_WARNING_KEY:
                    reportGlobalWarning(strings.get(tag));
                    break;

                case EventLoopManager.RC_BATTERY_STATUS_KEY:
                    String serialized = strings.get(tag);
                    RobotLog.vv(TAG, "RC battery Telemetry event: " + serialized);
                    BatteryStatus status = BatteryStatus.deserialize(serialized);
                    setTextView(rcBatteryTelemetry, Double.toString(status.percent) + "%");
                    setBatteryIcon(status, rcBatteryIcon);

                    break;

                case EventLoopManager.ROBOT_BATTERY_LEVEL_KEY:
                    String value = strings.get(tag);
                    RobotLog.vv(TAG, "Robot Battery Telemetry event: " + value);
                    showRobotBatteryVoltage(value);
                    break;

                default:
                    if(userKeyPresent)
                    {
                        setUserTelemetry(telemetryString.toString());
                    }
                    break;
            }

            return CallbackResult.HANDLED;
        }
        catch (RobotCoreException e)
        {
            RobotLog.logStackTrace(e);
            return CallbackResult.HANDLED;
        }
    }

    @SuppressLint("WrongViewCast")
    protected void showRobotBatteryVoltage(String value)
    {
        RelativeLayout robotBatteryBackground = findViewById(R.id.robot_battery_background);
        LinearLayout robotBatteryLayout = findViewById(R.id.rc_battery_layout);
        TextView robotNoVoltageSensor = findViewById(R.id.rc_no_voltage_sensor);
        if (value.equals(FtcEventLoopHandler.NO_VOLTAGE_SENSOR))
        {
            setVisibility(robotBatteryLayout, View.GONE);
            setVisibility(robotNoVoltageSensor, View.VISIBLE);
            resetBatteryStats();
            setBG(robotBatteryBackground, findViewById(R.id.rcBatteryBackgroundReference).getBackground());
            return;
        }
        setVisibility(robotBatteryLayout, View.VISIBLE);
        setVisibility(robotNoVoltageSensor, View.GONE);
        double voltage = Double.valueOf(value);
        if (voltage < V12BatteryMin)
        {
            V12BatteryMin = voltage;
            V12BatteryMinString = value;
        }
        setTextView(robotBatteryTelemetry, value + " V");
        setTextView(robotBatteryMinimum, "( " + V12BatteryMinString + " V )");
        int red = 127;
        int green = 127;
        if (voltage > 14.55)
        {
            red = 0;
        }
        else if (voltage > 12.0)
        {
            red = 127 - ((int) ((voltage - 12.0) * 50.0));
        }
        else
        {
            green = voltage > 9.45 ? (int) ((voltage - 9.45) * 50.0) : 0;
        }
        setBGColor(robotBatteryBackground, Color.rgb(red, green, 0));
    }

    protected void setBGColor(final View view, final int color)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                view.setBackgroundColor(color);
            }
        });
    }

    protected void setBG(final View view, final Drawable drawable)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                view.setBackground(drawable);
            }
        });
    }

    protected void resetBatteryStats()
    {
        V12BatteryMin = Double.POSITIVE_INFINITY;
        V12BatteryMinString = "";
    }

    protected void setUserTelemetry(String userTelemetry)
    {
        setTextView(this.textTelemetry, userTelemetry);
    }

    protected void clearUserTelemetry()
    {
        setTextView(textTelemetry, "");
    }

    protected void clearSystemTelemetry()
    {
        setVisibility(systemTelemetry, View.GONE);
        setTextView(systemTelemetry, "");
        setTextColor(systemTelemetry, systemTelemetryOriginalColor);
        RobotLog.clearGlobalErrorMsg();
        RobotLog.clearGlobalWarningMsg();
    }

    public CallbackResult gamepadEvent(RobocolDatagram robocolDatagram)
    {
        return CallbackResult.NOT_HANDLED;
    }

    public CallbackResult emptyEvent(RobocolDatagram robocolDatagram)
    {
        return CallbackResult.NOT_HANDLED;
    }

    public CallbackResult reportGlobalError(String error, boolean recoverable)
    {
        if (!RobotLog.getGlobalErrorMsg().equals(error))
        {
            RobotLog.ee(TAG, "System telemetry error: " + error);
            RobotLog.clearGlobalErrorMsg();
            RobotLog.setGlobalErrorMsg(error);
        }

        setTextColor(systemTelemetry, AppUtil.getInstance().getColor(R.color.text_error));
        setVisibility(systemTelemetry, View.VISIBLE);
        StringBuilder message = new StringBuilder();
        String string = getString(R.string.dsRobotStatus);

        if(robotState != null)
        {
            message.append(String.format(string, robotState.toString()));
        }
        else
        {
            message.append(String.format(string, getString(R.string.dsUnknown)));
        }

        if (recoverable)
        {
            message.append(getString(R.string.dsToAttemptRecovery));
        }

        message.append(String.format(getString(R.string.dsErrorMessage), error));
        setTextView(systemTelemetry, message.toString());
        stopTimerAndReset();
        uiRobotCantContinue();
        return CallbackResult.HANDLED;
    }

    protected void reportGlobalWarning(String warning)
    {
        if (!RobotLog.getGlobalWarningMessage().equals(warning))
        {
            RobotLog.ee(TAG, "System telemetry warning: " + warning);
            RobotLog.clearGlobalWarningMsg();
            RobotLog.setGlobalWarningMessage(warning);
        }
        setTextColor(systemTelemetry, AppUtil.getInstance().getColor(R.color.text_warning));
        setVisibility(systemTelemetry, View.VISIBLE);
        setTextView(systemTelemetry, String.format(getString(R.string.dsWarningMessage), warning));
    }

    protected void uiRobotCantContinue()
    {
        traceUiStateChange("ui:uiRobotCantContinue", UIState.CANT_CONTINUE);
        disableAndDimOpModeMenu();
        disableOpModeControls();
        dimControlPanelBack();
    }

    protected void disableOpModeControls()
    {
        setEnabled(buttonInit, false);
        setVisibility(buttonInit, View.VISIBLE);
        setVisibility(buttonStart, View.INVISIBLE);
        setVisibility(buttonStop, View.INVISIBLE);
        setVisibility(buttonInitStop, View.INVISIBLE);
        setVisibility(timerAndTimerSwitch, View.INVISIBLE);
    }

    protected void uiRobotControllerIsDisconnected()
    {
        traceUiStateChange("ui:uiRobotControllerIsDisconnected", UIState.DISCONNECTED);
        dimControlPanelBack();
        setOpacity(wifiInfo, PARTLY_OPAQUE);
        setOpacity(batteryInfo, PARTLY_OPAQUE);
        disableAndDimOpModeMenu();
        disableOpModeControls();
    }

    protected void uiRobotControllerIsConnected(ControlPanelBack controlPanelBack)
    {
        traceUiStateChange("ui:uiRobotControllerIsConnected", UIState.CONNNECTED);
        enableAndBrightenForConnected(controlPanelBack);
        AppUtil.getInstance().dismissAllDialogs(UILocation.ONLY_LOCAL);
        AppUtil.getInstance().dismissProgress(UILocation.ONLY_LOCAL);
        setTextView(rcBatteryTelemetry, "");
        setTextView(robotBatteryTelemetry, "");
    }

    protected void enableAndBrightenForConnected(ControlPanelBack controlPanelBack)
    {
        setControlPanelBack(controlPanelBack);
        setOpacity(wifiInfo, FULLY_OPAQUE);
        setOpacity(batteryInfo, FULLY_OPAQUE);
        enableAndBrightenOpModeMenu();
    }

    protected void checkConnectedEnableBrighten(ControlPanelBack controlPanelBack)
    {
        if (!clientConnected)
        {
            RobotLog.vv(TAG, "auto-rebrightening for connected state");
            enableAndBrightenForConnected(controlPanelBack);
            setClientConnected(true);
            requestUIState();
        }
    }

    protected void uiWaitingForOpModeSelection()
    {
        traceUiStateChange("ui:uiWaitingForOpModeSelection", UIState.WAITING_FOR_OPMODE_SELECTION);
        checkConnectedEnableBrighten(ControlPanelBack.DIM);
        dimControlPanelBack();
        enableAndBrightenOpModeMenu();
        showQueuedOpModeName();
        disableOpModeControls();
    }

    protected void uiWaitingForInitEvent()
    {
        traceUiStateChange("ui:uiWaitingForInitEvent", UIState.WAITING_FOR_INIT_EVENT);
        checkConnectedEnableBrighten(ControlPanelBack.BRIGHT);
        brightenControlPanelBack();
        showQueuedOpModeName();
        enableAndBrightenOpModeMenu();
        setEnabled(buttonInit, true);
        setVisibility(buttonInit, View.VISIBLE);
        setVisibility(buttonStart, View.INVISIBLE);
        setVisibility(buttonStop, View.INVISIBLE);
        setVisibility(buttonInitStop, View.INVISIBLE);
        setTimerButtonEnabled(true);
        setVisibility(timerAndTimerSwitch, View.VISIBLE);
    }

    protected void setTimerButtonEnabled(boolean enable)
    {
        setEnabled(timerAndTimerSwitch, enable);
        setEnabled(findViewById(R.id.timerBackground), enable);
        setEnabled(findViewById(R.id.timerStopWatch), enable);
        setEnabled(findViewById(R.id.timerText), enable);
        setEnabled(findViewById(R.id.timerSwitchOn), enable);
        setEnabled(findViewById(R.id.timerSwitchOff), enable);
    }

    protected void uiWaitingForStartEvent()
    {
        traceUiStateChange("ui:uiWaitingForStartEvent", UIState.WAITING_FOR_START_EVENT);
        checkConnectedEnableBrighten(ControlPanelBack.BRIGHT);
        showQueuedOpModeName();
        enableAndBrightenOpModeMenu();
        setVisibility(buttonStart, View.VISIBLE);
        setVisibility(buttonInit, View.INVISIBLE);
        setVisibility(buttonStop, View.INVISIBLE);
        setVisibility(buttonInitStop, View.VISIBLE);
        setTimerButtonEnabled(true);
        setVisibility(timerAndTimerSwitch, View.VISIBLE);
    }

    protected void uiWaitingForStopEvent()
    {
        traceUiStateChange("ui:uiWaitingForStopEvent", UIState.WAITING_FOR_STOP_EVENT);
        checkConnectedEnableBrighten(ControlPanelBack.BRIGHT);
        showQueuedOpModeName();
        enableAndBrightenOpModeMenu();
        setVisibility(buttonStop, View.VISIBLE);
        setVisibility(buttonInit, View.INVISIBLE);
        setVisibility(buttonStart, View.INVISIBLE);
        setVisibility(buttonInitStop, View.INVISIBLE);
        setTimerButtonEnabled(false);
        setVisibility(timerAndTimerSwitch, View.VISIBLE);
    }

    protected boolean isDefaultOpMode(String opmodeName)
    {
        return defaultOpMode.name.equals(opmodeName);
    }

    protected boolean isDefaultOpMode(OpModeMeta meta)
    {
        return isDefaultOpMode(meta.name);
    }

    protected OpModeMeta getOpModeMeta(String opModeName)
    {
        synchronized (opModes)
        {
            for (OpModeMeta opModeMeta : opModes)
            {
                if (opModeMeta.name.equals(opModeName))
                {
                    return opModeMeta;
                }
            }
            return new OpModeMeta(opModeName);
        }
    }

    protected void showQueuedOpModeName()
    {
        showQueuedOpModeName(queuedOpMode);
    }

    protected void showQueuedOpModeName(OpModeMeta opModeMeta)
    {
        if (isDefaultOpMode(opModeMeta))
        {
            setVisibility(currentOpModeName, View.GONE);
            setVisibility(chooseOpModePrompt, View.VISIBLE);
            return;
        }
        setTextView(currentOpModeName, opModeMeta.name);
        setVisibility(currentOpModeName, View.VISIBLE);
        setVisibility(chooseOpModePrompt, View.GONE);
    }

    protected void traceUiStateChange(String dbgMsg, UIState uiState)
    {
        RobotLog.vv(TAG, dbgMsg);
        this.uiState = uiState;
        setTextView(this.textDsUiStateIndicator, uiState.indicator);
    }

    protected void assumeClientConnectAndRefreshUI(ControlPanelBack controlPanelBack)
    {
        assumeClientConnect(controlPanelBack);
        requestUIState();
    }

    protected void assumeClientConnect(ControlPanelBack controlPanelBack)
    {
        RobotLog.vv(TAG, "Assuming client connected");
        setClientConnected(true);
        uiRobotControllerIsConnected(controlPanelBack);
    }

    protected void assumeClientDisconnect()
    {
        RobotLog.vv(TAG, "Assuming client disconnected");
        setClientConnected(false);
        enableAndResetTimer(false);
        opModeCountDown.disable();
        queuedOpMode = defaultOpMode;
        opModes.clear();
        pingStatus(" ");
        networkConnectionHandler.clientDisconnect();
        RobocolParsableBase.initializeSequenceNumber(10000);
        RobotLog.clearGlobalErrorMsg();
        setRobotState(RobotState.UNKNOWN);
        uiRobotControllerIsDisconnected();
    }

    protected boolean setClientConnected(boolean clientConnected)
    {
        boolean wasConnected = this.clientConnected;
        this.clientConnected = clientConnected;
        this.preferencesHelper.writeBooleanPrefIfDifferent(getString(R.string.pref_rc_connected), clientConnected);
        return wasConnected;
    }

    protected void handleOpModeQueued(OpModeMeta selection)
    {
        if (setQueuedOpModeIfDifferent(selection))
        {
            enableAndResetTimerForQueued();
        }
        uiWaitingForInitEvent();
    }

    protected boolean setQueuedOpModeIfDifferent(String newOpMode)
    {
        return setQueuedOpModeIfDifferent(getOpModeMeta(newOpMode));
    }

    protected boolean setQueuedOpModeIfDifferent(OpModeMeta newOpMode)
    {
        if (newOpMode.name.equals(queuedOpMode.name))
        {
            return false;
        }
        queuedOpMode = newOpMode;
        showQueuedOpModeName();
        return true;
    }

    protected void handleOpModeInit()
    {
        networkConnectionHandler.sendCommand(new Command(CommandList.CMD_INIT_OP_MODE, queuedOpMode.name));
    }

    protected void handleOpModeStart()
    {
        networkConnectionHandler.sendCommand(new Command(CommandList.CMD_RUN_OP_MODE, queuedOpMode.name));
    }

    protected void handleOpModeStop()
    {
        initDefaultOpMode();
    }

    protected void initDefaultOpMode()
    {
        networkConnectionHandler.sendCommand(new Command(CommandList.CMD_INIT_OP_MODE, defaultOpMode.name));
    }

    protected void runDefaultOpMode()
    {
        networkConnectionHandler.sendCommand(new Command(CommandList.CMD_RUN_OP_MODE, defaultOpMode.name));
    }

    protected CallbackResult handleCommandNotifyInitOpMode(String opModeInited)
    {
        if (uiState == UIState.CANT_CONTINUE)
        {
            return CallbackResult.HANDLED;
        }
        RobotLog.vv(TAG, "Robot Controller initializing op mode: " + opModeInited);
        stopTimerPreservingRemainingTime();
        if (isDefaultOpMode(opModeInited))
        {
            handleDefaultOpModeInitOrStart(false);
        }
        else
        {
            clearUserTelemetry();
            if (setQueuedOpModeIfDifferent(opModeInited))
            {
                RobotLog.vv(TAG, "timer: init new opmode");
                enableAndResetTimerForQueued();
            }
            else if (opModeCountDown.isEnabled())
            {
                RobotLog.vv(TAG, "timer: init w/ timer enabled");
                opModeCountDown.resetCountdown();
            }
            else
            {
                RobotLog.vv(TAG, "timer: init w/o timer enabled");
            }
            uiWaitingForStartEvent();
        }
        return CallbackResult.HANDLED;
    }

    protected CallbackResult handleCommandNotifyStartOpMode(String opModeStarted)
    {
        if (uiState == UIState.CANT_CONTINUE)
        {
            return CallbackResult.HANDLED;
        }
        RobotLog.vv(TAG, "Robot Controller starting op mode: " + opModeStarted);
        if (isDefaultOpMode(opModeStarted))
        {
            handleDefaultOpModeInitOrStart(true);
        }
        else
        {
            if (setQueuedOpModeIfDifferent(opModeStarted))
            {
                RobotLog.vv(TAG, "timer: started new opmode: auto-initing timer");
                enableAndResetTimerForQueued();
            }
            uiWaitingForStopEvent();
            if (opModeUseTimer)
            {
                opModeCountDown.start();
            }
            else
            {
                stopTimerAndReset();
            }
        }
        return CallbackResult.HANDLED;
    }

    protected void handleDefaultOpModeInitOrStart(boolean isStart)
    {
        if (isDefaultOpMode(queuedOpMode))
        {
            uiWaitingForOpModeSelection();
            return;
        }
        uiWaitingForInitEvent();
        if (!isStart)
        {
            runDefaultOpMode();
        }
    }

    protected void requestUIState()
    {
        networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_REQUEST_UI_STATE));
    }

    protected CallbackResult handleCommandNotifyOpModeList(String extra)
    {
        assumeClientConnect(ControlPanelBack.NO_CHANGE);
        opModes = new Gson().fromJson(extra, new TypeToken<Collection<OpModeMeta>>()
        {
        }.getType());
        RobotLog.vv(TAG, "Received the following op modes: " + opModes.toString());
        return CallbackResult.HANDLED;
    }

    protected CallbackResult handleCommandNotifyUserDeviceList(String extra)
    {
        UserConfigurationTypeManager.getInstance().deserializeUserDeviceTypes(extra);
        return CallbackResult.HANDLED;
    }

    protected CallbackResult handleCommandNotifyActiveConfig(String extra)
    {
        RobotLog.vv(TAG, "%s.handleCommandRequestActiveConfigResp(%s)", getClass().getSimpleName(), extra);
        final RobotConfigFile configFile = robotConfigFileManager.getConfigFromString(extra);
        robotConfigFileManager.setActiveConfig(configFile);
        appUtil.runOnUiThread(this, new Runnable()
        {
            public void run()
            {
                activeConfigText.setText(configFile.getName());
            }
        });
        return CallbackResult.HANDLED_CONTINUE;
    }

    protected CallbackResult handleCommandShowToast(String extra)
    {
        ShowToast showToast = ShowToast.deserialize(extra);
        appUtil.showToast(UILocation.ONLY_LOCAL, showToast.message, showToast.duration);
        return CallbackResult.HANDLED;
    }

    protected CallbackResult handleCommandShowProgress(String extra)
    {
        ProgressParameters showProgress = ShowProgress.deserialize(extra);
        appUtil.showProgress(UILocation.ONLY_LOCAL, "test", showProgress);
        return CallbackResult.HANDLED;
    }

    protected CallbackResult handleCommandDismissProgress()
    {
        appUtil.dismissProgress(UILocation.ONLY_LOCAL);
        return CallbackResult.HANDLED;
    }

    protected CallbackResult handleCommandShowDialog(String extra)
    {
        ShowDialog showDialog = ShowDialog.deserialize(extra);
        appUtil.showAlertDialog(showDialog.uuidString, UILocation.ONLY_LOCAL, showDialog.title, showDialog.message);
        return CallbackResult.HANDLED;
    }

    protected CallbackResult handleCommandDismissDialog(Command command)
    {
        appUtil.dismissDialog(UILocation.ONLY_LOCAL, DismissDialog.deserialize(command.getExtra()));
        return CallbackResult.HANDLED;
    }

    protected CallbackResult handleCommandDismissAllDialogs(Command command)
    {
        appUtil.dismissAllDialogs(UILocation.ONLY_LOCAL);
        return CallbackResult.HANDLED;
    }

    private CallbackResult handleCommandStartProgrammingModeResp(String extra)
    {
        if (!(extra == null || extra.isEmpty()))
        {
            Intent remoteProgrammingModeIntent = new Intent(AppUtil.getDefContext(), RemoteProgrammingModeActivity.class);
            remoteProgrammingModeIntent.putExtra(LaunchActivityConstantsList.RC_WEB_INFO, extra);
            startActivityForResult(remoteProgrammingModeIntent, RequestCode.PROGRAMMING_MODE.ordinal());
        }
        return CallbackResult.HANDLED;
    }

    private CallbackResult handleCommandStartProgramAndManageResp(String extra)
    {
        if (!(extra == null || extra.isEmpty()))
        {
            Intent remoteProgrammingModeIntent = new Intent(AppUtil.getDefContext(), ProgramAndManageActivity.class);
            remoteProgrammingModeIntent.putExtra(LaunchActivityConstantsList.RC_WEB_INFO, extra);
            startActivityForResult(remoteProgrammingModeIntent, RequestCode.PROGRAM_AND_MANAGE.ordinal());
        }
        return CallbackResult.HANDLED;
    }

    public void onClickRCBatteryToast(View v)
    {
        showToast(getString(R.string.toastRobotControllerBattery));
    }

    public void onClickRobotBatteryToast(View v)
    {
        resetBatteryStats();
        showToast(getString(R.string.toastRobotBattery));
    }

    public void onClickDSBatteryToast(View v)
    {
        showToast(getString(R.string.toastDriverStationBattery));
    }

    protected void showWifiStatus(final boolean showingRCName, final String status)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                textWifiDirectStatusShowingRC = showingRCName;
                textWifiDirectStatus.setText(status);
            }
        });
    }

    protected void displayDeviceName(final String name)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                textDeviceName.setText(name);
            }
        });
    }

    protected void assertUiThread()
    {
        Assert.assertTrue(Thread.currentThread() == uiThread);
    }

    protected void setButtonText(final Button button, final String text)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                button.setText(text);
            }
        });
    }

    protected void setTextView(final TextView textView, final String text)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                textView.setText(text);
            }
        });
    }

    protected void setTextColor(final TextView textView, @ColorInt final int color)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                textView.setTextColor(color);
            }
        });
    }

    protected void setOpacity(final View v, final float opacity)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                v.setAlpha(opacity);
            }
        });
    }

    protected void setImageResource(final ImageButton button, final int resourceId)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                button.setImageResource(resourceId);
            }
        });
    }

    protected void setVisibility(final View view, final int visibility)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                view.setVisibility(visibility);
            }
        });
    }

    protected void setEnabled(final View view, final boolean enabled)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                view.setEnabled(enabled);
            }
        });
    }

    protected void setControlPanelBack(ControlPanelBack state)
    {
        switch (state)
        {
            case DIM:
                dimControlPanelBack();
                break;

            case BRIGHT:
                brightenControlPanelBack();
                break;
        }
    }

    protected void dimControlPanelBack()
    {
        setOpacity(controlPanelBack, PARTLY_OPAQUE);
    }

    protected void brightenControlPanelBack()
    {
        setOpacity(controlPanelBack, FULLY_OPAQUE);
    }

    protected void disableAndDimOpModeMenu()
    {
        disableAndDim(buttonAutonomous);
        disableAndDim(buttonTeleOp);
        disableAndDim(currentOpModeName);
        disableAndDim(chooseOpModePrompt);
    }

    protected void enableAndBrightenOpModeMenu()
    {
        enableAndBrighten(buttonAutonomous);
        enableAndBrighten(buttonTeleOp);
        setOpacity(currentOpModeName, FULLY_OPAQUE);
        setOpacity(chooseOpModePrompt, FULLY_OPAQUE);
    }

    protected void disableAndDim(View view)
    {
        setOpacity(view, PARTLY_OPAQUE);
        setEnabled(view, false);
    }

    protected void enableAndBrighten(View view)
    {
        setOpacity(view, FULLY_OPAQUE);
        setEnabled(view, true);
    }

    protected void pingStatus(String status)
    {
        setTextView(textPingStatus, status);
    }

    protected void logGamepadEvent(Gamepad gamepad, String message)
    {
        if (debugLogging)
        {
            RobotLog.dd(TAG, "gampad event: %s id=%d user=%s", message, gamepad.getGamepadId(), gamepad.getUser());
        }
    }

    protected synchronized void handleGamepadEvent(MotionEvent event)
    {
        Gamepad gamepad = gamepadManager.getGamepadById(event.getDeviceId());
        if (gamepad != null)
        {
            logGamepadEvent(gamepad, "motion");
            gamepad.update(event);
            indicateGamepad(event);
        }
    }

    protected void indicateGamepad(InputEvent event)
    {
        Gamepad gamepad = gamepadManager.getAssignedGamepadById(event.getDeviceId());
        if (gamepad != null)
        {
            gamepadIndicators.get(gamepad.getUser()).setState(State.INDICATE);
        }
    }

    protected synchronized void handleGamepadEvent(KeyEvent event)
    {
        int gamepadId = event.getDeviceId();
        Gamepad gamepad = gamepadManager.ensureGamepadExists(gamepadId);
        logGamepadEvent(gamepad, "key");
        gamepad.update(event);
        indicateGamepad(event);
        if (gamepad.start && (gamepad.a || gamepad.b))
        {
            if (gamepad.a)
            {
                assignGamepad(gamepadId, GamepadUser.ONE);
            }
            if (gamepad.b)
            {
                assignGamepad(gamepadId, GamepadUser.TWO);
            }
        }
    }

    protected void assignGamepad(int gamepadId, GamepadUser gamepadUser)
    {
        gamepadManager.assignGamepad(gamepadId, gamepadUser);
    }

    public void onInputDeviceAdded(int deviceId)
    {
        RobotLog.vv(TAG, String.format("New input device (id = %d) detected.", deviceId));
    }

    public void onInputDeviceRemoved(int deviceId)
    {
        RobotLog.vv(TAG, String.format("Input device (id = %d) removed.", deviceId));
        gamepadManager.removeGamepad(deviceId);
    }

    public void onInputDeviceChanged(int deviceId)
    {
        RobotLog.vv(TAG, String.format("Input device (id = %d) modified.", deviceId));
    }
}