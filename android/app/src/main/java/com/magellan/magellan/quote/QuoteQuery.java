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

    public static enum Interval
    {
        OneMinute,
        FiveMinutes,
        FifteenMinutes,
        ThirtyMinutes,
        OneHour,
        OneDay,
        OneWeek,
        OneMonth,
    }

    public String symbol;
    public Period period;
    public Interval interval;

    public QuoteQuery(String sym, Period p, Interval i)
    {
        symbol = sym;
        period = p;
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
        switch (interval)
        {
            case OneMinute:
                return Duration.standardMinutes(1);
            case FiveMinutes:
                return Duration.standardMinutes(5);
            case FifteenMinutes:
                return Duration.standardMinutes(15);
            case ThirtyMinutes:
                return Duration.standardMinutes(30);
            case OneHour:
                return Duration.standardHours(1);
            case OneDay:
                return Duration.standardDays(1);
            case OneWeek:
                return Duration.standardDays(7);
            case OneMonth:
                return Duration.standardDays(30);
            default:
                Log.e("Magellan", "getIntervalAsDuration(): intervalUnit is corrupt");
                return null;
        }
    }
}