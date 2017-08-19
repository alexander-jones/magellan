package com.magellan.magellan.quote;

import org.joda.time.DateTime;

public class Quote
{
    private DateTime mTime;
    private float mOpen;
    private float mClose;
    private float mLow;
    private float mHigh;
    private int mVolume;

    public Quote(DateTime time, float open, float close, float low, float high, int volume)
    {
        mTime = time;
        mClose = close;
        mOpen = open;
        mLow = low;
        mHigh = high;
        mVolume = volume;
    }

    public DateTime getTime() { return mTime;}
    public float getClose() { return mClose;}
    public float getOpen() { return mOpen;}
    public float getLow() { return mLow;}
    public float getHigh() { return mHigh;}
    public int getVolume() { return mVolume;}
}
