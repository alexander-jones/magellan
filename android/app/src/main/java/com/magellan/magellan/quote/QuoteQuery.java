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

        end = ApplicationContext.getLastCloseDateTime();
        switch (period)
        {
            case OneDay:
                start = ApplicationContext.getOpenTimeForCloseTime(end);
                return; // dont recompute open time
            case OneWeek:
                start = end.minusWeeks(1);
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

        start = ApplicationContext.getLastTradingDateTimeFrom(start);

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

    public DateTime getLastStepFromTime(DateTime time)
    {
        if (time.isAfter(end))
            return end;
        else if (time.isBefore(start))
            return null;

        DateTime ret = ApplicationContext.getLastTradingDateTimeFrom(time);
        switch (interval)
        {
            case OneMinute:
                return ret.withSecondOfMinute(0).withMillisOfSecond(0);
            case FiveMinutes:
                return getLastTradingTimeEvenlyDivisibleByMinutes(ret, 5);
            case FifteenMinutes:
                return getLastTradingTimeEvenlyDivisibleByMinutes(ret, 15);
            case ThirtyMinutes:
                return getLastTradingTimeEvenlyDivisibleByMinutes(ret, 30);
            case OneHour:
                return getLastTradingTimeEvenlyDivisibleByMinutes(ret, 60);
            case OneDay:
                DateTime inputClose = ApplicationContext.getCloseTimeWithSameDate(time);
                Duration diffSinceInputClose = new Duration(ret, inputClose);
                if (diffSinceInputClose.getStandardDays() >= 1)
                    return ret;
                else
                    return ApplicationContext.getLastTradingDateTimeFrom(inputClose.minusDays(1));
            case OneWeek: //
                return ApplicationContext.getLastTradingDateTimeFrom(ret.minusWeeks(1));
            case OneMonth:
                return ApplicationContext.getLastTradingDateTimeFrom(ret.minusMonths(1));
            default:
                Log.e("Magellan", "getIntervalAsDuration(): intervalUnit is corrupt");
                return null;
        }
    }

    private DateTime getLastTradingTimeEvenlyDivisibleByMinutes(DateTime time, int minutes)
    {
        int minuteOfHour = time.minuteOfHour().get();
        int minutesSince = minuteOfHour % 5;
        if (minuteOfHour % minutes == 0)
            return time.withSecondOfMinute(0).withMillisOfSecond(0);
        else
        {
            DateTime ret = time.minusMinutes(minutesSince).withSecondOfMinute(0).withMillisOfSecond(0);
            int hourOfDay = ret.hourOfDay().get();
            minuteOfHour -= minutesSince;
            if (hourOfDay > 16) // 4:00 pm is NYSE close
                ret = ApplicationContext.getCloseTimeWithSameDate(ret);
            else if (hourOfDay < 9 || (hourOfDay == 9 && minuteOfHour < 30)) // 9:30 am is NYSE open
                ret = ApplicationContext.getCloseTimeWithSameDate(ret.minusDays(1));
            return  ret;
        }
    }
}