/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.best.deskclock.stopwatch;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.best.deskclock.R;
import com.best.deskclock.ThemeUtils;
import com.best.deskclock.Utils;
import com.best.deskclock.data.DataModel;
import com.best.deskclock.data.Lap;
import com.best.deskclock.data.Stopwatch;

import java.util.List;

/**
 * Custom view that draws a reference lap as a circle when one exists.
 * 当存在参考圈时，将其绘制为圆的自定义视图。
 */
public final class StopwatchCircleView extends View {

    /**
     * The size of the dot indicating the user's position within the reference lap.
     * 指示用户在参考圈内位置的点的大小。
     */
    private final float mDotRadius;

    /**
     * An amount to subtract from the true radius to account for drawing thicknesses.
     * 要从实际半径中减去的量，以说明图纸厚度。
     */
    private final float mRadiusOffset;

    /**
     * Used to scale the width of the marker to make it similarly visible on all screens.
     * 用于缩放标记的宽度，使其在所有屏幕上同样可见。
     */
    private final float mScreenDensity;

    /**
     * The color indicating the remaining portion of the current lap.
     * 指示当前圈剩余部分的颜色。
     */
    private final int mRemainderColor;

    /**
     * The color indicating the completed portion of the lap.
     * 指示圈完成部分的颜色。
     */
    private final int mCompletedColor;

    /**
     * The size of the stroke that paints the lap circle.
     * 绘制圈的笔划的大小。
     */
    private final float mStrokeSize;

    /**
     * The size of the stroke that paints the marker for the end of the prior lap.
     * 在前一圈结束时绘制标记的笔划大小。
     */
    private final float mMarkerStrokeSize;

    private final Paint mPaint = new Paint();
    private final Paint mFill = new Paint();
    private final RectF mArcRect = new RectF();

    @SuppressWarnings("unused")
    public StopwatchCircleView(Context context) {
        this(context, null);
    }

    public StopwatchCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final Resources resources = context.getResources();
        final float dotDiameter = resources.getDimension(R.dimen.circletimer_dot_size);

        mDotRadius = dotDiameter / 2f;
        mScreenDensity = resources.getDisplayMetrics().density;
        mStrokeSize = resources.getDimension(R.dimen.circletimer_circle_size);
        mMarkerStrokeSize = resources.getDimension(R.dimen.circletimer_marker_size);
        mRadiusOffset = Utils.calculateRadiusOffset(mStrokeSize, dotDiameter, mMarkerStrokeSize);


        mRemainderColor = ThemeUtils.resolveColor(context, androidx.appcompat.R.attr.colorControlNormal);
        mCompletedColor = ThemeUtils.resolveColor(context, androidx.appcompat.R.attr.colorAccent);

        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        mFill.setAntiAlias(true);
        mFill.setColor(mCompletedColor);
        mFill.setStyle(Paint.Style.FILL);
    }

    /**
     * Start the animation if it is not currently running.
     * 如果动画当前未运行，请启动该动画。
     */
    void update() {
        postInvalidateOnAnimation();
    }

    @Override
    public void onDraw(Canvas canvas) {
        // Compute the size and location of the circle to be drawn.计算要绘制的圆的大小和位置。
        final int xCenter = getWidth() / 2;
        final int yCenter = getHeight() / 2;
        final float radius = Math.min(xCenter, yCenter) - mRadiusOffset;

        // Reset old painting state.
        mPaint.setColor(mRemainderColor);
        mPaint.setStrokeWidth(mStrokeSize);

        final List<Lap> laps = getLaps();

        // If a reference lap does not exist or should not be drawn, draw a simple white circle.
        // 如果不存在或不应绘制参考圈，请绘制一个简单的白色圆圈。
        if (laps.isEmpty() || !DataModel.getDataModel().canAddMoreLaps()) {
            // Draw a complete white circle; no red arc required.
            canvas.drawCircle(xCenter, yCenter, radius, mPaint);

            // No need to continue animating the plain white circle.无需继续设置纯白色圆圈的动画。
            return;
        }

        // The first lap is the reference lap to which all future laps are compared.
        // 第一圈是比较所有未来圈数的参考圈数。
        final Stopwatch stopwatch = getStopwatch();
        final int lapCount = laps.size();
        final Lap firstLap = laps.get(lapCount - 1);
        final Lap priorLap = laps.get(0);
        final long firstLapTime = firstLap.getLapTime();
        final long currentLapTime = stopwatch.getTotalTime() - priorLap.getAccumulatedTime();

        // Draw a combination of red and white arcs to create a circle.
        // 绘制红色和白色圆弧的组合以创建圆。
        mArcRect.top = yCenter - radius;
        mArcRect.bottom = yCenter + radius;
        mArcRect.left = xCenter - radius;
        mArcRect.right = xCenter + radius;
        final float redPercent = (float) currentLapTime / (float) firstLapTime;
        final float whitePercent = 1 - (redPercent > 1 ? 1 : redPercent);

        // Draw a white arc to indicate the amount of reference lap that remains.
        // 绘制一条白色弧线以指示剩余的参考圈数。
        canvas.drawArc(mArcRect, 270 + (1 - whitePercent) * 360, whitePercent * 360, false, mPaint);

        // Draw a red arc to indicate the amount of reference lap completed.
        // 绘制一条红色弧线，指示完成的参考圈数。
        mPaint.setColor(mCompletedColor);
        canvas.drawArc(mArcRect, 270, redPercent * 360, false, mPaint);

        // Starting on lap 2, a marker can be drawn indicating where the prior lap ended.
        // 从第2圈开始，可以画一个标记，指示前一圈的终点。
        if (lapCount > 1) {
            mPaint.setColor(mRemainderColor);
            mPaint.setStrokeWidth(mMarkerStrokeSize);
            final float markerAngle = (float) priorLap.getLapTime() / (float) firstLapTime * 360;
            final float startAngle = 270 + markerAngle;
            final float sweepAngle = mScreenDensity * (float) (360 / (radius * Math.PI));
            canvas.drawArc(mArcRect, startAngle, sweepAngle, false, mPaint);
        }

        // If the stopwatch is not running it does not require continuous updates.
        // 如果秒表未运行，则不需要持续更新。
        if (stopwatch.isRunning()) {
            postInvalidateOnAnimation();
        }
    }

    private Stopwatch getStopwatch() {
        return DataModel.getDataModel().getStopwatch();
    }

    private List<Lap> getLaps() {
        return DataModel.getDataModel().getLaps();
    }
}
