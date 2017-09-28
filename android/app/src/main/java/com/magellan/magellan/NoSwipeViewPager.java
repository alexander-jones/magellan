package com.magellan.magellan;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoSwipeViewPager extends ViewPager {
    private Boolean mScrollingEnabled = false;
    public NoSwipeViewPager(Context context) {
        super(context);
    }

    public NoSwipeViewPager(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mScrollingEnabled ? super.onInterceptTouchEvent(event) : false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mScrollingEnabled ?  super.onTouchEvent(event) : false;
    }

    public void setScrollEnabled(Boolean enable){
        mScrollingEnabled = enable;
    }
}