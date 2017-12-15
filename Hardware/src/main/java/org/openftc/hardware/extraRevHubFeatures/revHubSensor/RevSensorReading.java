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

package org.openftc.hardware.extraRevHubFeatures.revHubSensor;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class RevSensorReading
{
    public double doubleValue;
    public String formattedValue;

    public RevSensorReading(double doubleValue)
    {
        this.doubleValue = doubleValue;
        formattedValue = String.valueOf(doubleValue);
    }

    protected void formatForOver1k()
    {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        formattedValue = df.format(doubleValue / 1000);
    }
}
