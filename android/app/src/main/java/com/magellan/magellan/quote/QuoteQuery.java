package com.magellan.magellan.quote;

import android.content.Context;
import android.util.Log;

import com.magellan.magellan.ApplicationContext;
import com.magellan.magellan.market.Market;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;

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
    public int tag;

    public QuoteQuery(String sym, Period p, Interval i)
    {
        this(sym, p, i, 0);
    }

    public QuoteQuery(String sym, Period p, Interval i, int t)
    {
        symbol = sym;
        period = p;
        interval = i;
        tag = t;

        end = getLastCloseDateTime();
        switch (period)
        {
            case OneDay:
                start = getOpenTimeForCloseTime(end);
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

        start = getLastTradingDateTimeFrom(start);

        if (interval.ordinal() < Interval.OneHour.ordinal()) // interval neatly spans from start to open.
            start = getOpenTimeForCloseTime(start);
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

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd_HH:mm").withZone(Market.getTradingTimeZone());

    public File getCacheFile(File parentDir)
    {
        DateTime lastQueryTime = getLastStepBefore(DateTime.now());

        File endpointDir = new File(parentDir, dateTimeFormatter.print(lastQueryTime));
        if (!endpointDir.exists())
            endpointDir.mkdir();

        String queryCacheFilename = symbol + "_" + period.toString() + "_" + interval.toString();
        return new File(endpointDir, queryCacheFilename);
    }

    public DateTime getLastStepBefore(DateTime time)
    {
        if (time.isAfter(end))
            return end;
        else if (time.isBefore(start))
            return null;

        DateTime ret = getLastTradingDateTimeFrom(time);
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
                DateTime inputClose = getCloseTimeWithSameDate(time);
                Duration diffSinceInputClose = new Duration(ret, inputClose);
                if (diffSinceInputClose.getStandardDays() >= 1)
                    return ret;
                else
                    return getLastTradingDateTimeFrom(inputClose.minusDays(1));
            case OneWeek: //
                return getLastTradingDateTimeFrom(ret.minusWeeks(1));
            case OneMonth:
                return getLastTradingDateTimeFrom(ret.minusMonths(1));
            default:
                Log.e("Magellan", "getIntervalAsDuration(): intervalUnit is corrupt");
                return null;
        }
    }

    public int getExpectedSteps()
    {
        return getTradingStepsInRange(start, end, getIntervalAsDuration());
    }

    private static DateTime getLastTradingTimeEvenlyDivisibleByMinutes(DateTime time, int minutes)
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
                ret = getCloseTimeWithSameDate(ret);
            else if (hourOfDay < 9 || (hourOfDay == 9 && minuteOfHour < 30)) // 9:30 am is NYSE open
                ret = getCloseTimeWithSameDate(ret.minusDays(1));
            return  ret;
        }
    }


    private static DateTime getCloseTimeWithSameDate(DateTime time)
    {
        return time.withHourOfDay(16).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0); // 4:00 pm is NYSE close
    }

    private static DateTime getOpenTimeForCloseTime(DateTime closeTime)
    {
        return closeTime.minusHours(7).plusMinutes(30); // 9:30 EST is NYSE open*/
    }

    private static DateTime getLastCloseDateTime()
    {
        DateTime now = DateTime.now(Market.getTradingTimeZone());
        return getLastTradingDateTimeFrom(now);
    }

    private static int getTradingStepsInRange(DateTime start, DateTime end, Duration interval)
    {
        int ret = 0;
        DateTime cursor = end;

        int endDay = -1;
        do
        {
            int newEndDay = cursor.dayOfWeek().get();
            if (endDay != newEndDay)
            {
                do {
                    endDay = cursor.dayOfWeek().get();
                    if (endDay == 6) // Saturday?
                        cursor = cursor.minusDays(1);
                    else if (endDay == 7) // Sunday?
                        cursor = cursor.minusDays(2);
                    else if (Market.isTradingHoliday(cursor))
                        cursor = cursor.minusDays(1);
                    else
                        break;
                }while(true);
            }

            int hourOfDay = cursor.hourOfDay().get();
            int minuteOfHour = cursor.minuteOfHour().get();
            if (hourOfDay > 16)
                cursor = getCloseTimeWithSameDate(cursor);
            else if (hourOfDay < 9 || (hourOfDay == 9 && minuteOfHour < 30))
                cursor = getCloseTimeWithSameDate(cursor.minusDays(1));
            else
            {
                ++ret;
                cursor = cursor.minus(interval.getMillis());
            }
        }
        while (cursor.isAfter(start));
        if (cursor.isEqual(start))
            ++ret;
        return ret;
    }

    private static DateTime getLastTradingDateTimeFrom(DateTime inTime)
    {
        DateTime ret = inTime;
        int hourOfDay = ret.hourOfDay().get();
        int minuteOfHour = ret.minuteOfHour().get();
        if (hourOfDay > 16)
            ret = getCloseTimeWithSameDate(ret);
        else if (hourOfDay < 9 || (hourOfDay == 9 && minuteOfHour < 30))
            ret = getCloseTimeWithSameDate(ret.minusDays(1));
        do
        {
            int daysToUnwind = 0;
            int endDay = ret.dayOfWeek().get();
            if (endDay == 6) // Saturday?
                daysToUnwind = 1;
            else if (endDay == 7) // Sunday?
                daysToUnwind = 2;
            else if (Market.isTradingHoliday(ret))
                daysToUnwind = 1;

            if (daysToUnwind == 0)
                break;
            else
                ret = getCloseTimeWithSameDate(ret.minusDays(daysToUnwind));
        }
        while (true);

        return ret;
    }
}