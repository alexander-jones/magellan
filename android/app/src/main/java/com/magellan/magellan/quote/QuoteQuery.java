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
    public DateTime start;
    public DateTime end;

    public QuoteQuery(String sym, Period p, Interval i)
    {
        symbol = sym;
        period = p;
        interval = i;

        end = ApplicationContext.getLastTradingDayCloseTime();
        switch (period)
        {
            case OneDay:
                start = ApplicationContext.getOpenTimeForCloseTime(end);
                return; // dont recompute open time
            case OneWeek:
                start = end.minusWeeks(1); //ApplicationContext.getCloseTimeOneTradingWeekFromClose(end);
                break;
            case OneMonth:
                start = end.minusMonths(1);
                break;
            case ThreeMonths:
                start = end.minusMonths(3);
                break;
            case OneYear:
                start = end.minusYears(1);
                break;
            case FiveYears:
                start = end.minusYears(5);
                break;
            case TenYears:
                start = end.minusYears(10);
                break;
            default:
                Log.e("Magellan", "getStart(): period is corrupt");
        }

        if (interval.ordinal() < Interval.OneHour.ordinal()) // interval neatly spans from start to open.
            start = ApplicationContext.getOpenTimeForCloseTime(start);
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