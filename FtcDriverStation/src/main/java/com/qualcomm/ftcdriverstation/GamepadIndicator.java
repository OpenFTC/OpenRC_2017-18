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

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class GamepadIndicator
{
    protected ImageView activeView;
    protected ImageView baseView;
    protected final Context context;
    protected final int idActive;
    protected final int idBase;
    protected State state = State.INVISIBLE;

    public enum State
    {
        INVISIBLE,
        VISIBLE,
        INDICATE
    }

    public GamepadIndicator(Activity activity, int idActive, int idBase)
    {
        this.context = activity;
        this.idActive = idActive;
        this.idBase = idBase;
        initialize(activity);
    }

    public void initialize(Activity activity)
    {
        this.activeView = activity.findViewById(this.idActive);
        this.baseView = activity.findViewById(this.idBase);
    }

    public void setState(final State state)
    {
        this.state = state;
        AppUtil.getInstance().runOnUiThread(new Runnable()
        {
            public void run()
            {
                switch (state)
                {
                    case INVISIBLE:
                        activeView.setVisibility(View.INVISIBLE);
                        baseView.setVisibility(View.INVISIBLE);
                        break;

                    case VISIBLE:
                        activeView.setVisibility(View.INVISIBLE);
                        baseView.setVisibility(View.VISIBLE);
                        break;

                    case INDICATE:
                        indicate();
                        break;
                }
            }
        });
    }

    protected void indicate()
    {
        Animation fadeout = AnimationUtils.loadAnimation(this.context, R.anim.fadeout);
        activeView.setImageResource(R.drawable.icon_controlleractive);
        fadeout.setAnimationListener(new AnimationListener()
        {
            public void onAnimationStart(Animation animation)
            {
                //nothing
            }

            public void onAnimationEnd(Animation animation)
            {
                activeView.setImageResource(R.drawable.icon_controller);
            }

            public void onAnimationRepeat(Animation animation)
            {
                activeView.setImageResource(R.drawable.icon_controller);
            }
        });
        activeView.startAnimation(fadeout);
    }
}