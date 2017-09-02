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

    static final int MINUTES_IN_HOUR = 60;
    static final int MINUTES_IN_DAY = 1440;
    static final int MINUTES_IN_WEEK = MINUTES_IN_DAY * 7;
    static final int MINUTES_IN_MONTH = 43800;
    static final int MINUTES_IN_THREE_MONTHS = MINUTES_IN_MONTH * 3;
    static final int MINUTES_IN_YEAR= MINUTES_IN_MONTH * 12;
    static final int MINUTES_IN_FIVE_YEARS= MINUTES_IN_YEAR * 5;
    static final int MINUTES_IN_TEN_YEARS= MINUTES_IN_FIVE_YEARS * 2;

    public int getExpectedQuoteCount()
    {
        int intervalMinutes;
        switch (intervalUnit)
        {
            case Minute:
                intervalMinutes = 1;
                break;
            case Hour:
                intervalMinutes = MINUTES_IN_HOUR;
                break;
            case Day:
                intervalMinutes = MINUTES_IN_DAY;
                break;
            case Week:
                intervalMinutes = MINUTES_IN_WEEK;
                break;
            case Month:
                intervalMinutes = MINUTES_IN_MONTH;
                break;
            default:
                Log.e("Magellan", "getIntervalAsDuration(): intervalUnit is corrupt");
                return 0;
        }

        intervalMinutes *= interval;

        switch (period)
        {
            case OneDay:
                return MINUTES_IN_DAY / intervalMinutes;
            case OneWeek:
                return MINUTES_IN_WEEK / intervalMinutes;
            case OneMonth:
                return MINUTES_IN_MONTH / intervalMinutes;
            case ThreeMonths:
                return MINUTES_IN_THREE_MONTHS / intervalMinutes;
            case OneYear:
                return MINUTES_IN_YEAR / intervalMinutes;
            case FiveYears:
                return MINUTES_IN_FIVE_YEARS / intervalMinutes;
            case TenYears:
                return MINUTES_IN_TEN_YEARS / intervalMinutes;
            default:
                Log.e("Magellan", "getStart(): period is corrupt");
                return 0;
        }
    }
}