/*
 * Copyright (c) 2017 FTC team 4634 FROGbots
 *
 * All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openftc.hardware.extraRevHubFeatures;

import android.graphics.Color;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.LynxNackException;
import com.qualcomm.hardware.lynx.LynxUsbDevice;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCResponse;
import com.qualcomm.hardware.lynx.commands.standard.LynxSetModuleLEDColorCommand;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.openftc.hardware.extraRevHubFeatures.revHubSensor.RevCurrentSensorReading;
import org.openftc.hardware.extraRevHubFeatures.revHubSensor.RevSensorReading;
import org.openftc.hardware.extraRevHubFeatures.revHubSensor.RevVoltageSensorReading;

public class OpenRevHub extends LynxModule
{
    public OpenRevHub(LynxUsbDevice lynxUsbDevice, int moduleAddress, boolean isParent)
    {
        super(lynxUsbDevice, moduleAddress, isParent);
    }

    public synchronized void setLedColor(byte r, byte g, byte b)
    {
        LynxSetModuleLEDColorCommand colorCommand = new LynxSetModuleLEDColorCommand(this, r, g, b);
        try
        {
            colorCommand.send();
        }
        catch (InterruptedException | LynxNackException e)
        {
            e.printStackTrace();
        }
    }

    public synchronized void setLedColor(HardwareMap hardwareMap, int resId)
    {
        int color = hardwareMap.appContext.getResources().getColor(resId);

        byte red = (byte) Color.red(color);
        byte green = (byte) Color.green(color);
        byte blue = (byte) Color.blue(color);

        setLedColor(red, green, blue);
    }

    public synchronized RevSensorReading getTotalModuleCurrentDraw()
    {
        LynxGetADCCommand command = new LynxGetADCCommand(this, LynxGetADCCommand.Channel.BATTERY_CURRENT, LynxGetADCCommand.Mode.ENGINEERING);
        try
        {
            LynxGetADCResponse response = command.sendReceive();
            int ma = response.getValue();
            return new RevCurrentSensorReading(ma);
        }
        catch (InterruptedException|RuntimeException|LynxNackException e)
        {
            handleException(e);
        }
        return new RevSensorReading(0);
    }

    public synchronized RevSensorReading getServoBusCurrentDraw()
    {
        LynxGetADCCommand.Channel channel = LynxGetADCCommand.Channel.SERVO_CURRENT;

        LynxGetADCCommand command = new LynxGetADCCommand(this, channel, LynxGetADCCommand.Mode.ENGINEERING);
        try
        {
            LynxGetADCResponse response = command.sendReceive();
            int ma = response.getValue();
            return new RevCurrentSensorReading(ma);
        }
        catch (InterruptedException|RuntimeException|LynxNackException e)
        {
            handleException(e);
        }
        return new RevSensorReading(0);
    }

    public synchronized RevSensorReading getGpioBusCurrentDraw()
    {
        LynxGetADCCommand.Channel channel = LynxGetADCCommand.Channel.GPIO_CURRENT;

        LynxGetADCCommand command = new LynxGetADCCommand(this, channel, LynxGetADCCommand.Mode.ENGINEERING);
        try
        {
            LynxGetADCResponse response = command.sendReceive();
            int ma = response.getValue();
            return new RevCurrentSensorReading(ma);
        }
        catch (InterruptedException|RuntimeException|LynxNackException e)
        {
            handleException(e);
        }
        return new RevSensorReading(0);
    }

    public synchronized RevSensorReading getI2CbusCurrentDraw()
    {
        LynxGetADCCommand.Channel channel = LynxGetADCCommand.Channel.I2C_BUS_CURRENT;

        LynxGetADCCommand command = new LynxGetADCCommand(this, channel, LynxGetADCCommand.Mode.ENGINEERING);
        try
        {
            LynxGetADCResponse response = command.sendReceive();
            int ma = response.getValue();
            return new RevCurrentSensorReading(ma);
        }
        catch (InterruptedException|RuntimeException|LynxNackException e)
        {
            handleException(e);
        }
        return new RevSensorReading(0);
    }

    public synchronized RevSensorReading read5vMonitor()
    {
        LynxGetADCCommand.Channel channel = LynxGetADCCommand.Channel.FIVE_VOLT_MONITOR;

        LynxGetADCCommand command = new LynxGetADCCommand(this, channel, LynxGetADCCommand.Mode.ENGINEERING);
        try
        {
            LynxGetADCResponse response = command.sendReceive();
            int ma = response.getValue();
            return new RevVoltageSensorReading(ma);
        }
        catch (InterruptedException|RuntimeException|LynxNackException e)
        {
            handleException(e);
        }
        return new RevSensorReading(0);
    }

    public synchronized RevSensorReading read12vMonitor()
    {
        LynxGetADCCommand.Channel channel = LynxGetADCCommand.Channel.BATTERY_MONITOR;

        LynxGetADCCommand command = new LynxGetADCCommand(this, channel, LynxGetADCCommand.Mode.ENGINEERING);
        try
        {
            LynxGetADCResponse response = command.sendReceive();
            int ma = response.getValue();
            return new RevVoltageSensorReading(ma);
        }
        catch (InterruptedException|RuntimeException|LynxNackException e)
        {
            handleException(e);
        }
        return new RevSensorReading(0);
    }

    public synchronized RevSensorReading getInternalTemperature()
    {
        LynxGetADCCommand.Channel channel = LynxGetADCCommand.Channel.CONTROLLER_TEMPERATURE;

        LynxGetADCCommand command = new LynxGetADCCommand(this, channel, LynxGetADCCommand.Mode.ENGINEERING);
        try
        {
            LynxGetADCResponse response = command.sendReceive();
            return new RevSensorReading(response.getValue());
        }
        catch (InterruptedException|RuntimeException|LynxNackException e)
        {
            handleException(e);
        }
        return new RevSensorReading(0);
    }
}