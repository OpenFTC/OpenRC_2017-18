/*
 * Copyright (c) 2017 FTC team 4634 FROGbots
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.openftc.hardware.rev;

import android.graphics.Color;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.LynxNackException;
import com.qualcomm.hardware.lynx.LynxUsbDevice;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCResponse;
import com.qualcomm.hardware.lynx.commands.standard.LynxSetModuleLEDColorCommand;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.openftc.hardware.rev.revHubSensor.RevCurrentSensorReading;
import org.openftc.hardware.rev.revHubSensor.RevSensorReading;
import org.openftc.hardware.rev.revHubSensor.RevVoltageSensorReading;

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