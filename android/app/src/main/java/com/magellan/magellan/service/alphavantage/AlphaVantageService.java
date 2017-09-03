package com.magellan.magellan.service.alphavantage;

import android.util.Log;

import com.barchart.ondemand.BarchartOnDemandClient;
import com.barchart.ondemand.api.HistoryRequest;
import com.magellan.magellan.quote.IQuoteService;
import com.magellan.magellan.quote.Quote;
import com.magellan.magellan.quote.QuoteQuery;
import com.magellan.magellan.stock.IStockService;
import com.magellan.magellan.stock.Stock;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AlphaVantageService implements IQuoteService {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.forID("America/New_York"));
    private static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    public List<Quote> execute(QuoteQuery query) {

        String function;
        String interval = null;
        String output_size = null;
        DateTimeFormatter formatter = dateFormatter;

        switch (query.interval)
        {
            case OneMinute:
                function = "TIME_SERIES_INTRADAY";
                interval = "1min";
                output_size = "full";
                formatter = dateTimeFormatter;
                break;
            case FiveMinutes:
                function = "TIME_SERIES_INTRADAY";
                interval = "5min";
                if (query.period.ordinal() > QuoteQuery.Period.OneDay.ordinal())
                    output_size = "full";
                formatter = dateTimeFormatter;
                break;
            case FifteenMinutes:
                function = "TIME_SERIES_INTRADAY";
                interval = "15min";
                if (query.period.ordinal() > QuoteQuery.Period.OneDay.ordinal())
                    output_size = "full";
                formatter = dateTimeFormatter;
                break;
            case ThirtyMinutes:
                function = "TIME_SERIES_INTRADAY";
                interval = "30min";
                if (query.period.ordinal() > QuoteQuery.Period.OneWeek.ordinal())
                    output_size = "full";
                formatter = dateTimeFormatter;
                break;
            case OneHour:
                function = "TIME_SERIES_INTRADAY";
                interval = "60min";
                if (query.period.ordinal() > QuoteQuery.Period.OneWeek.ordinal())
                    output_size = "full";
                formatter = dateTimeFormatter;
                break;
            case OneDay:
                function = "TIME_SERIES_DAILY";
                if (query.period.ordinal() > QuoteQuery.Period.ThreeMonths.ordinal())
                    output_size = "full";
                break;
            case OneWeek:
                function = "TIME_SERIES_WEEKLY";
                break;
            case OneMonth:
                function = "TIME_SERIES_MONTHLY";
                break;
            default:
                Log.e("Magellan", "getIntervalAsDuration(): intervalUnit is corrupt");
                return null;
        }

        String url_select = "https://www.alphavantage.co/query?function=" + function + "&symbol=" + query.symbol;
        if (interval != null)
            url_select += "&interval=" + interval;
        if (output_size != null)
            url_select += "&outputsize=" + output_size;
        url_select += "&datatype=csv&apikey=RQZA9G66FK7JMYSY";

        List<Quote> ret = null;
        try {
            URL url = new URL(url_select);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                try {
                    BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 67108864);

                    String line = null;
                    DateTime queryStart = query.getStart();
                    bReader.readLine(); // skip header
                    ret = new ArrayList<Quote>();

                    DateTime lastDate = null;
                    while ((line = bReader.readLine()) != null) {
                        String [] parts = line.split(",");
                        DateTime date = DateTime.parse(parts[0], formatter);
                        if (date.isBefore(queryStart) && !date.isEqual(queryStart))
                            break;

                        float open = Float.parseFloat(parts[1]);
                        float high = Float.parseFloat(parts[2]);
                        float low = Float.parseFloat(parts[3]);
                        float close = Float.parseFloat(parts[4]);
                        int volume = Integer.parseInt(parts[5]);

                        ret.add(new Quote(date, open, close, low, high, volume));
                    }

                    inputStream.close();

                } catch (Exception e) {
                    Log.e("Magellan", "Error unpacking json from alpha vantage:" + e.getMessage());
                }
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            Log.e("Magellan", "Could not connect to alpha vantage" + e.toString());

        }
        Collections.reverse(ret);
        return ret;
    }
}
