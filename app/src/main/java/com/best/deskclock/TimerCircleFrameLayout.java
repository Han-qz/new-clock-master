/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.best.deskclock;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * A container that frames a timer circle of some sort. The circle is allowed to grow naturally
 * according to its layout constraints up to the {@link R.dimen#max_timer_circle_size largest}
 * allowable size.
 * 一种框定某种计时器圆的容器。该圆可以根据其布局约束自然增长，最大可达到{@link R.dimen#max_timer_circle_size max}允许的大小。
 */
public class TimerCircleFrameLayout extends FrameLayout {

    public TimerCircleFrameLayout(Context context) {
        super(context);
    }

    public TimerCircleFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimerCircleFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Note: this method assumes the parent container will specify {@link MeasureSpec#EXACTLY exact}
     * width and height values.
     *注意：此方法假定父容器将指定｛@link MeasureSpec#EXACTLY EXACTLY｝宽度和高度值。
     * @param widthMeasureSpec  horizontal space requirements as imposed by the parent 父容器规定的水平空间要求
     * @param heightMeasureSpec vertical space requirements as imposed by the parent 父容器规定的垂直空间要求
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();

        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();

        // Fetch the exact sizes imposed by the parent container.获取父容器施加的确切大小。
        final int width = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight;
        final int height = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom;
        final int smallestDimension = Math.min(width, height);

        // Fetch the absolute maximum circle size allowed. 获取允许的绝对最大圆大小。
        final int maxSize = getResources().getDimensionPixelSize(R.dimen.max_timer_circle_size);
        final int size = Math.min(smallestDimension, maxSize);

        // Set the size of this container.设置此容器的大小。
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(size + paddingLeft + paddingRight,
                MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(size + paddingTop + paddingBottom,
                MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
