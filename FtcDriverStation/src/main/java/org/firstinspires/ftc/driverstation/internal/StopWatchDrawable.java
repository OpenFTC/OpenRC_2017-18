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