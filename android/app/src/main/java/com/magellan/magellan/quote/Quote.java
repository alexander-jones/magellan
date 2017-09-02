package com.magellan.magellan.quote;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.nustaq.serialization.FSTConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class Quote implements Serializable
{
    private DateTime mTime;
    private float mOpen;
    private float mClose;
    private float mLow;
    private float mHigh;
    private int mVolume;

    static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

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

    public static void saveTo(File outState, List<Quote> quotes)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(outState);
            byte bytes[] = conf.asByteArray(quotes);
            fos.write(bytes);
            fos.close();
        }
        catch (IOException e)
        {
            Log.e("Magellan","Quote.saveTo() :" + e.getMessage());
        }
    }

    public static List<Quote> loadFrom(File inFile) {
        try
        {
            FileInputStream fileInputStream = new FileInputStream(inFile);
            byte[] bytes = new byte[(int) inFile.length()];
            fileInputStream.read(bytes);
            return (List<Quote>) conf.asObject(bytes);
        }
        catch (IOException e)
        {
            Log.e("Magellan","Quote.loadFrom() :" + e.getMessage());
            return null;
        }
    }
}
