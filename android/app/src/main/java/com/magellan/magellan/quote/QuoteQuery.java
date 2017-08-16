package com.magellan.magellan.quote;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class QuoteQuery
{
    public static enum IntervalUnit
    {
        Minute,
        Day,
        Week,
        Month
    }

    public String symbol;
    public DateTime start;
    public DateTime end;
    public IntervalUnit intervalUnit;
    public int interval;

    public QuoteQuery(String sym, DateTime st, DateTime e, IntervalUnit iu, int i)
    {
        symbol = sym;
        start = st;
        end = e;
        intervalUnit = iu;
        interval = i;
    }

    public Duration getIntervalAsDuration()
    {
        switch (intervalUnit)
        {
            case Minute:
                return Duration.standardMinutes(interval);
            case Day:
                return Duration.standardDays(interval);
            case Week:
                return Duration.standardDays(interval * 7);
            case Month:
                return Duration.standardDays(interval * 30);
            default:
                Log.e("Magellan", "getIntervalAsDuration(): intervalUnit is corrupt");
                return null;
        }
    }
}