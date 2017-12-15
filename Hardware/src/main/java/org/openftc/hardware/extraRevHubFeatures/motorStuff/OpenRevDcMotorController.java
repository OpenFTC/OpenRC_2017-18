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

package org.openftc.hardware.extraRevHubFeatures.motorStuff;

import android.content.Context;
import com.qualcomm.hardware.lynx.LynxDcMotorController;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.LynxNackException;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCResponse;
import com.qualcomm.robotcore.exception.RobotCoreException;
import org.openftc.hardware.extraRevHubFeatures.revHubSensor.RevCurrentSensorReading;
import org.openftc.hardware.extraRevHubFeatures.revHubSensor.RevSensorReading;

public class OpenRevDcMotorController extends LynxDcMotorController
{
    public OpenRevDcMotorController(Context context, LynxModule module) throws RobotCoreException, InterruptedException
    {
        super(context, module);
    }

    public synchronized RevSensorReading getMotorCurrentDraw(int port)
    {
        LynxGetADCCommand.Channel channel = null;

        if(port == 0)
        {
            channel = LynxGetADCCommand.Channel.MOTOR0_CURRENT;
        }
        else if(port == 1)
        {
            channel = LynxGetADCCommand.Channel.MOTOR1_CURRENT;
        }
        else if(port == 2)
        {
            channel = LynxGetADCCommand.Channel.MOTOR2_CURRENT;
        }
        else if(port == 3)
        {
            channel = LynxGetADCCommand.Channel.MOTOR3_CURRENT;
        }

        LynxGetADCCommand command = new LynxGetADCCommand(this.getModule(), channel, LynxGetADCCommand.Mode.ENGINEERING);
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
}
