/*
 * Copyright (c) 2019 FTC team 4634 FROGbots
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

package org.openftc.ds;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.qualcomm.ftcdriverstation.FtcDriverStationActivity;

import org.openftc.rc.UiUtils;
import org.openftc.rc.Utils;

public class SplashActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /*
         * Check to see if the RC app is also installed, and
         * if it is, notify the user of the problem and
         * offer them a way to remove one or the other.
         */
        if(Utils.isFtcRobotControllerInstalled(getPackageManager()))
        {
            /*
             * Houston, we have a problem
             */
            UiUtils.showBothAppsInstalledDialog(this);
        }
        else
        {
            /*
             * Start loading the main activity. After it's loaded, close
             * down the splash screen
             */

            Intent intent = new Intent(this, FtcDriverStationActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
