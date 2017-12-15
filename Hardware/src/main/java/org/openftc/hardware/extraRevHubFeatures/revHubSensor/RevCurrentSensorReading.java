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

public class RevCurrentSensorReading extends RevSensorReading
{
    public RevCurrentSensorReading(double doubleValue)
    {
        super(doubleValue);

        if(doubleValue < 1000)
        {
            formattedValue = formattedValue.concat("ma");
        }
        else
        {
            formatForOver1k();
            formattedValue = formattedValue.concat("a");
        }
    }
}
