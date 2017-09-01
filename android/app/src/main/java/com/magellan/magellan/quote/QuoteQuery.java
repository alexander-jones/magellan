package com.magellan.magellan.quote;

import android.util.Log;

import com.magellan.magellan.ApplicationContext;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class QuoteQuery
{
    public static enum Period
    {
        OneDay,
        OneWeek,
        OneMonth,
        ThreeMonths,
        OneYear,
        FiveYears,
        TenYears
    }

    public static enum IntervalUnit
    {
        Minute,
        Hour,
        Day,
        Week,
        Month,
    }

    public String symbol;
    public Period period;
    public IntervalUnit intervalUnit;
    public int interval;

    public QuoteQuery(String sym, Period p, IntervalUnit iu, int i)
    {
        symbol = sym;
        period = p;
        intervalUnit = iu;
        interval = i;
    }

    public DateTime getStart()
    {
        switch (period)
        {
            case OneDay:
                return ApplicationContext.getLastTradingDayOpenTime();
            case OneWeek:
                return getEnd().minusWeeks(1);
            case OneMonth:
                return getEnd().minusMonths(1);
            case ThreeMonths:
                return getEnd().minusMonths(3);
            case OneYear:
                return getEnd().minusYears(1);
            case FiveYears:
                return getEnd().minusYears(5);
            case TenYears:
                return getEnd().minusYears(10);
            default:
                Log.e("Magellan", "getStart(): period is corrupt");
                return null;
        }
    }

    public DateTime getEnd()
    {
        return ApplicationContext.getLastTradingDayCloseTime();
    }


    public Duration getIntervalAsDuration()
    {
        switch (intervalUnit)
        {
            case Minute:
                return Duration.standardMinutes(interval);
            case Hour:
                return Duration.standardHours(interval);
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