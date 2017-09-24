package com.magellan.magellan.market;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.GregorianChronology;

public class Market {

    public static DateTimeZone getTradingTimeZone() { return DateTimeZone.forID("America/New_York"); }
    public static final GregorianChronology getChronology() {return  GregorianChronology.getInstance(getTradingTimeZone());}

    public static boolean isTradingHoliday(DateTime date)
    {
        DateTime tradingDate = date.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        int year = tradingDate.year().get();
        if (year == 2017) {
            for (StockMarketHolidays2017 holiday : StockMarketHolidays2017.values()) {
                if (holiday.date.isEqual(tradingDate))
                    return true;
            }
        } else if (year == 2018) {
            for (StockMarketHolidays2018 holiday : StockMarketHolidays2018.values()) {
                if (holiday.date.isEqual(tradingDate))
                    return true;
            }
        } else if (year == 2019) {
            for (StockMarketHolidays2019 holiday : StockMarketHolidays2019.values()) {
                if (holiday.date.isEqual(tradingDate))
                    return true;
            }
        } else {
            Log.e("Magellan", "ERROR: Holiday / Blacklist days not handed for year " + Integer.toString(year));
        }
        return false;
    }
    public static enum StockMarketHolidays2017
    {
        NewYearsDay(1, 2), // actually 1,1 but observed 1, 2
        MartinLutherKingDay(1, 16),
        WashingtonsBirthday(2, 20),
        GoodFriday(4, 14),
        MemorialDay(5, 29),
        IndependenceDay(7,4),
        LaborDay(9,4),
        ThanskgivingDay(11, 23),
        Christmas(12, 25);

        private final DateTime date;
        StockMarketHolidays2017(int month, int day)
        {
            date = new DateTime(2017, month, day, 0, 0, 0, 0, getChronology());
        }
    }

    public static enum StockMarketHolidays2018
    {
        NewYearsDay(1, 1),
        MartinLutherKingDay(1, 15),
        WashingtonsBirthday(2, 19),
        GoodFriday(3, 30),
        MemorialDay(5, 28),
        IndependenceDay(7,4),
        LaborDay(9,3),
        ThanskgivingDay(11, 22),
        Christmas(12, 25);

        public final DateTime date;
        StockMarketHolidays2018(int month, int day)
        {
            date = new DateTime(2018, month, day, 0, 0, 0, 0, getChronology());
        }
    }

    public static enum StockMarketHolidays2019
    {
        NewYearsDay(1, 1),
        MartinLutherKingDay(1, 21),
        WashingtonsBirthday(2, 18),
        GoodFriday(4, 19),
        MemorialDay(5, 27),
        IndependenceDay(7,4),
        LaborDay(9,2),
        ThanskgivingDay(11, 28),
        Christmas(12, 25);

        public final DateTime date;
        StockMarketHolidays2019(int month, int day)
        {
            date = new DateTime(2019, month, day, 0, 0, 0, 0, getChronology());
        }
    }
}
