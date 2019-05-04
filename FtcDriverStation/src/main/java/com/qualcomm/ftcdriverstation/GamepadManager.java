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
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.qualcomm.ftcdriverstation.GamepadIndicator.State;
import com.qualcomm.hardware.logitech.LogitechGamepadF310;
import com.qualcomm.hardware.microsoft.MicrosoftGamepadXbox360;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.RobotLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.firstinspires.ftc.robotcore.internal.system.Assert;
import org.firstinspires.ftc.robotcore.internal.ui.GamepadUser;
import org.firstinspires.ftc.robotcore.internal.ui.RobotCoreGamepadManager;

public class GamepadManager implements RobotCoreGamepadManager
{
    public static final String TAG = "GamepadManager";
    protected final Context context;
    protected boolean debug = false;
    protected Map<Integer, Gamepad> gamepadIdToGamepadMap = new ConcurrentHashMap<>();
    protected Map<GamepadUser, GamepadIndicator> gamepadIndicators = null;
    protected final PreferenceListener preferenceListener = new PreferenceListener();
    protected SharedPreferences preferences;
    protected Set<GamepadUser> recentlyUnassignedUsers = Collections.newSetFromMap(new ConcurrentHashMap<GamepadUser, Boolean>());
    protected Map<GamepadUser, Integer> userToGamepadIdMap = new ConcurrentHashMap<>();

    protected class PreferenceListener implements OnSharedPreferenceChangeListener
    {
        protected PreferenceListener()
        {
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            if (key.equals(GamepadManager.this.context.getString(R.string.pref_gamepad_user1_type_key)))
            {
                GamepadManager.this.unassignUser(GamepadUser.ONE);
            }
            else if (key.equals(GamepadManager.this.context.getString(R.string.pref_gamepad_user2_type_key)))
            {
                GamepadManager.this.unassignUser(GamepadUser.TWO);
            }
        }
    }

    public GamepadManager(Context context)
    {
        this.context = context;
    }

    public void setGamepadIndicators(Map<GamepadUser, GamepadIndicator> indicators)
    {
        gamepadIndicators = indicators;
    }

    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

    public void open()
    {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.registerOnSharedPreferenceChangeListener(preferenceListener);
    }

    public void close()
    {
        preferences.unregisterOnSharedPreferenceChangeListener(preferenceListener);
    }

    public synchronized Gamepad ensureGamepadExists(int gamepadId)
    {
        Gamepad result;
        result = gamepadIdToGamepadMap.get(gamepadId);
        if (result == null)
        {
            result = new Gamepad();
            gamepadIdToGamepadMap.put(gamepadId, result);
        }
        return result;
    }

    public synchronized void removeGamepad(int gamepadId)
    {
        Gamepad gamepad = getAssignedGamepadById(gamepadId);
        if (gamepad != null)
        {
            internalUnassignUser(gamepad.getUser());
        }
        gamepadIdToGamepadMap.remove(gamepadId);
    }

    public synchronized void unassignUser(GamepadUser gamepadUser)
    {
        Integer existing = userToGamepadIdMap.get(gamepadUser);
        if (existing != null)
        {
            gamepadIdToGamepadMap.remove(existing);
        }
        internalUnassignUser(gamepadUser);
    }

    public synchronized List<Gamepad> getGamepadsForTransmission()
    {
        ArrayList<Gamepad> result = new ArrayList<>(2);
        for (Entry<GamepadUser, Integer> pair : userToGamepadIdMap.entrySet())
        {
            result.add(getGamepadById(pair.getValue()));
        }
        for (GamepadUser gamepadUser : recentlyUnassignedUsers)
        {
            RobotLog.vv(TAG, "transmitting synthetic gamepad user=%s", gamepadUser);
            Gamepad gamepad = new Gamepad();
            gamepad.setGamepadId(-2);
            gamepad.refreshTimestamp();
            gamepad.setUser(gamepadUser);
            result.add(gamepad);
            recentlyUnassignedUsers.remove(gamepadUser);
        }
        return result;
    }

    @Nullable
    public synchronized Gamepad getGamepadById(Integer gamepadId)
    {
        if (gamepadId != null)
        {
            return gamepadIdToGamepadMap.get(gamepadId);
        }

        return null;
    }

    @Nullable
    public synchronized Gamepad getAssignedGamepadById(Integer gamepadId)
    {
        if (gamepadId != null)
        {
            for (Entry<GamepadUser, Integer> pair : userToGamepadIdMap.entrySet())
            {
                if (pair.getValue().equals(gamepadId))
                {
                    return getGamepadById(pair.getValue());
                }
            }
        }

        return null;
    }

    public synchronized void clearGamepadAssignments()
    {
        for (GamepadUser gamepadUser : userToGamepadIdMap.keySet())
        {
            unassignUser(gamepadUser);
        }
    }

    protected void internalUnassignUser(GamepadUser gamepadUser)
    {
        gamepadIndicators.get(gamepadUser).setState(State.INVISIBLE);
        userToGamepadIdMap.remove(gamepadUser);
        recentlyUnassignedUsers.add(gamepadUser);
    }

    public synchronized void assignGamepad(int gamepadId, GamepadUser gamepadUser)
    {
        if (debug)
        {
            RobotLog.dd(TAG, "assigning gampadId=%d user=%s", gamepadId, gamepadUser);
        }
        for (Entry<GamepadUser, Integer> entry : userToGamepadIdMap.entrySet())
        {
            if (entry.getValue() == gamepadId)
            {
                internalUnassignUser(entry.getKey());
            }
        }
        userToGamepadIdMap.put(gamepadUser, gamepadId);
        recentlyUnassignedUsers.remove(gamepadUser);
        makeGamepadForUser(gamepadUser, gamepadId);
        gamepadIndicators.get(gamepadUser).setState(State.VISIBLE);
        RobotLog.vv(TAG, "assigned id=%d user=%s type=%s class=%s", gamepadId, gamepadUser, gamepadIdToGamepadMap.get(gamepadId).type(), ((Gamepad) gamepadIdToGamepadMap.get(gamepadId)).getClass().getSimpleName());
    }

    protected void makeGamepadForUser(GamepadUser gamepadUser, int gamepadId)
    {
        Gamepad gamepad;
        String key = "";

        switch (gamepadUser)
        {
            case ONE:
                key = context.getString(R.string.pref_gamepad_user1_type_key);
                break;
            case TWO:
                key = context.getString(R.string.pref_gamepad_user2_type_key);
                break;
        }

        String gamepadType = preferences.getString(key, context.getString(R.string.gamepad_default));
        if (gamepadType.equals(context.getString(R.string.gamepad_logitech_f310)))
        {
            gamepad = new LogitechGamepadF310();
        }
        else if (gamepadType.equals(context.getString(R.string.gamepad_microsoft_xbox_360)))
        {
            gamepad = new MicrosoftGamepadXbox360();
        }
        else
        {
            gamepad = new Gamepad();
        }

        gamepad.setGamepadId(gamepadId);
        Gamepad existing = gamepadIdToGamepadMap.get(gamepadId);

        boolean existsAndIsCorrectType = existing != null && existing.getClass() == gamepad.getClass();

        if (existsAndIsCorrectType)
        {
            gamepad = existing;
        }
        else if (existing != null)
        {
            try
            {
                gamepad.copy(existing);
            }
            catch (RobotCoreException ignored) {}
        }

        Assert.assertTrue(gamepad.getGamepadId() == gamepadId);
        gamepad.setUser(gamepadUser);
        gamepad.refreshTimestamp();
        gamepadIdToGamepadMap.put(gamepadId, gamepad);

        if (debug)
        {
            RobotLog.dd(TAG, "initialized gamepad id=%d user=%s", gamepad.getGamepadId(), gamepad.getUser());
        }
    }
}