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

package com.best.deskclock.stopwatch;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.UNSPECIFIED;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.best.deskclock.R;

//秒表外观布局
/**
 * Dynamically apportions size the stopwatch circle depending on the preferred width of the laps
 * list and the container size. Layouts fall into two different buckets:
 * 根据圈列表的首选宽度和容器大小动态分配秒表圈的大小。布局分为两个不同的部分：
 * <p>
 * When the width of the laps list is less than half the container width, the laps list and
 * stopwatch display are each centered within half the container.
 * 当圈数列表的宽度小于容器宽度的一半时，圈数列表和秒表显示均位于容器的一半范围内。
 * <pre>
 *     ---------------------------------------------------------------------------
 *     |                                    |               Lap 5                |
 *     |                                    |               Lap 4                |
 *     |             21:45.67               |               Lap 3                |
 *     |                                    |               Lap 2                |
 *     |                                    |               Lap 1                |
 *     ---------------------------------------------------------------------------
 * </pre>
 * <p>
 * When the width of the laps list is greater than half the container width, the laps list is
 * granted all of the space it requires and the stopwatch display is centered within the remaining
 * container width.
 * 当圈数列表的宽度大于容器宽度的一半时，圈数列表将获得所需的所有空间，秒表显示将位于剩余容器宽度的中心。
 * <pre>
 *     ---------------------------------------------------------------------------
 *     |               |                          Lap 5                          |
 *     |               |                          Lap 4                          |
 *     |   21:45.67    |                          Lap 3                          |
 *     |               |                          Lap 2                          |
 *     |               |                          Lap 1                          |
 *     ---------------------------------------------------------------------------
 * </pre>
 */
public class StopwatchLandscapeLayout extends ViewGroup {

    private View mLapsListView;
    private View mStopwatchView;

    public StopwatchLandscapeLayout(Context context) {
        super(context);
    }

    public StopwatchLandscapeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StopwatchLandscapeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mLapsListView = findViewById(R.id.laps_list);
        mStopwatchView = findViewById(R.id.stopwatch_time_wrapper);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int halfWidth = width / 2;

        final int minWidthSpec = MeasureSpec.makeMeasureSpec(width, UNSPECIFIED);
        final int maxHeightSpec = MeasureSpec.makeMeasureSpec(height, AT_MOST);

        // First determine the width of the laps list.
        //首先确定圈数列表的宽度。
        final int lapsListWidth;
        if (mLapsListView != null && mLapsListView.getVisibility() != GONE) {
            // Measure the intrinsic size of the laps list.
            mLapsListView.measure(minWidthSpec, maxHeightSpec);

            // Actual laps list width is the larger of half the container and its intrinsic width.
            // 实际圈数列表宽度是容器的一半及其固有宽度中的较大者。
            lapsListWidth = Math.max(mLapsListView.getMeasuredWidth(), halfWidth);
            final int lapsListWidthSpec = MeasureSpec.makeMeasureSpec(lapsListWidth, EXACTLY);
            mLapsListView.measure(lapsListWidthSpec, maxHeightSpec);
        } else {
            lapsListWidth = 0;
        }

        // Stopwatch timer consumes the remaining width of container not granted to laps list.
        // 秒表计时器使用的是未授予圈数列表的容器剩余宽度。
        final int stopwatchWidth = width - lapsListWidth;
        final int stopwatchWidthSpec = MeasureSpec.makeMeasureSpec(stopwatchWidth, EXACTLY);
        mStopwatchView.measure(stopwatchWidthSpec, maxHeightSpec);

        // Record the measured size of this container.记录此容器的测量尺寸。
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // Compute the space available for layout.计算可用于布局的空间。
        final int left = getPaddingLeft();
        final int top = getPaddingTop();
        final int right = getWidth() - getPaddingRight();
        final int bottom = getHeight() - getPaddingBottom();
        final int width = right - left;
        final int height = bottom - top;
        final int halfHeight = height / 2;
        final boolean isLTR = getLayoutDirection() == LAYOUT_DIRECTION_LTR;

        final int lapsListWidth;
        if (mLapsListView != null && mLapsListView.getVisibility() != GONE) {
            // Layout the laps list, centering it vertically. 布置圈数列表，使其垂直居中。
            lapsListWidth = mLapsListView.getMeasuredWidth();
            final int lapsListHeight = mLapsListView.getMeasuredHeight();
            final int lapsListTop = top + halfHeight - (lapsListHeight / 2);
            final int lapsListBottom = lapsListTop + lapsListHeight;
            final int lapsListLeft;
            final int lapsListRight;
            if (isLTR) {
                lapsListLeft = right - lapsListWidth;
                lapsListRight = right;
            } else {
                lapsListLeft = left;
                lapsListRight = left + lapsListWidth;
            }

            mLapsListView.layout(lapsListLeft, lapsListTop, lapsListRight, lapsListBottom);
        } else {
            lapsListWidth = 0;
        }

        // Layout the stopwatch, centering it horizontally and vertically.
        // 布置秒表，使其水平和垂直居中。
        final int stopwatchWidth = mStopwatchView.getMeasuredWidth();
        final int stopwatchHeight = mStopwatchView.getMeasuredHeight();
        final int stopwatchTop = top + halfHeight - (stopwatchHeight / 2);
        final int stopwatchBottom = stopwatchTop + stopwatchHeight;
        final int stopwatchLeft;
        final int stopwatchRight;
        if (isLTR) {
            stopwatchLeft = left + ((width - lapsListWidth - stopwatchWidth) / 2);
            stopwatchRight = stopwatchLeft + stopwatchWidth;
        } else {
            stopwatchRight = right - ((width - lapsListWidth - stopwatchWidth) / 2);
            stopwatchLeft = stopwatchRight - stopwatchWidth;
        }

        mStopwatchView.layout(stopwatchLeft, stopwatchTop, stopwatchRight, stopwatchBottom);
    }
}
