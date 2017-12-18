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

package org.openftc.hardware.rev.motorStuff;

import android.content.Context;
import com.qualcomm.hardware.lynx.LynxDcMotorController;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.LynxNackException;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCResponse;
import com.qualcomm.robotcore.exception.RobotCoreException;
import org.openftc.hardware.rev.revHubSensor.RevCurrentSensorReading;
import org.openftc.hardware.rev.revHubSensor.RevSensorReading;

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
