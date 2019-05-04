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

package org.firstinspires.ftc.driverstation.internal;

import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;

import org.firstinspires.ftc.robotcore.internal.ui.PaintedPathDrawable;

public class StopWatchDrawable extends PaintedPathDrawable
{
    private final float bigDiam = 67.0f;
    private final float bigHand = 20.1f;
    private final float height = 100.5f;
    private final float littleHand = 13.400001f;
    private final float smallDiam = 12.5f;
    private final float stem = 15.0f;
    private final float stroke = 6.0f;
    private final float width = 73.0f;

    public StopWatchDrawable()
    {
        this(-16711936);
    }

    public StopWatchDrawable(@ColorInt int color)
    {
        super(color);

        this.paint.setStyle(Style.STROKE);
        this.paint.setStrokeCap(Cap.ROUND);
    }

    public int getIntrinsicWidth()
    {
        return Math.round(width);
    }

    public int getIntrinsicHeight()
    {
        return Math.round(height);
    }

    protected void computePath(Rect bounds)
    {
        float scale = Math.min(((float) bounds.width()) / width, ((float) bounds.height()) / height);
        this.paint.setStrokeWidth(stroke * scale);
        this.path = new Path();
        float halfStroke = (stroke / 2) * scale;
        RectF bigCircle = new RectF();
        bigCircle.set(((float) bounds.left) + halfStroke, ((float) bounds.bottom) - (bigDiam * scale), ((float) bounds.right) - halfStroke, ((float) bounds.bottom) - halfStroke);
        this.path.addOval(bigCircle, Direction.CCW);
        float middle = bounds.exactCenterX();
        float stemTop = bigCircle.top - (stem * scale);
        this.path.moveTo(middle, bigCircle.top);
        this.path.lineTo(middle, stemTop);
        RectF littleCircle = new RectF();
        littleCircle.set(middle - ((smallDiam / 2) * scale), stemTop - (smallDiam * scale), ((smallDiam / 2) * scale) + middle, stemTop);
        this.path.addOval(littleCircle, Direction.CCW);
        this.path.moveTo(bigCircle.centerX(), bigCircle.centerY());
        this.path.rLineTo(0.0f, -bigHand * scale);
        this.path.moveTo(bigCircle.centerX(), bigCircle.centerY());
        this.path.rLineTo(littleHand * scale, 0.0f);
    }
}