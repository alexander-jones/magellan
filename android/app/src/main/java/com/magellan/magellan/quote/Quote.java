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
    public DateTime time;
    public float open;
    public float close;
    public float low;
    public float high;
    public long volume;

    static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    public Quote(DateTime t, float o, float c, float l, float h, long v)
    {
        time = t;
        close = c;
        open = o;
        low = l;
        high = h;
        volume = v;
    }

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
