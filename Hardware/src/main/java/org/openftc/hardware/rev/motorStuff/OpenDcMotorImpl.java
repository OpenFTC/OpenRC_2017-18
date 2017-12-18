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

import android.support.annotation.NonNull;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorImplEx;
import com.qualcomm.robotcore.hardware.configuration.MotorConfigurationType;
import org.openftc.hardware.rev.revHubSensor.RevSensorReading;

public class OpenDcMotorImpl extends DcMotorImplEx implements OpenDcMotor
{
    OpenRevDcMotorController controller;

    public OpenDcMotorImpl(DcMotorController controller, int portNumber)
    {
        this(controller, portNumber, Direction.FORWARD);
    }

    public OpenDcMotorImpl(DcMotorController controller, int portNumber, Direction direction)
    {
        this(controller, portNumber, direction, MotorConfigurationType.getUnspecifiedMotorType());
    }

    public OpenDcMotorImpl(DcMotorController controller, int portNumber, Direction direction, @NonNull MotorConfigurationType motorType)
    {
        super(controller, portNumber, direction, motorType);
        this.controller = (OpenRevDcMotorController) controller;
    }

    public RevSensorReading getCurrentDraw()
    {
        return controller.getMotorCurrentDraw(getPortNumber());
    }
}
