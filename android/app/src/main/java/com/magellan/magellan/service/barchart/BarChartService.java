package com.magellan.magellan.service.barchart;

import android.util.Log;

import com.barchart.ondemand.BarchartOnDemandClient;
import com.barchart.ondemand.api.HistoryRequest;
import com.barchart.ondemand.api.responses.HistoryBar;
import com.magellan.magellan.quote.Quote;
import com.magellan.magellan.quote.IQuoteService;
import com.magellan.magellan.quote.QuoteQuery;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BarChartService implements IQuoteService
{
    BarchartOnDemandClient mClient;
    public BarChartService()
    {
        mClient = new BarchartOnDemandClient.Builder().apiKey("053b0a25336ff63cdaccec0316ed8b84").baseUrl("http://marketdata.websol.barchart.com/").build();
    }

    public List<Quote> execute(QuoteQuery query)
    {
        try {
            final HistoryRequest.Builder builder = new HistoryRequest.Builder();
            builder.symbol(query.symbol);
            switch (query.interval) {
                case OneMinute:
                    builder.type(HistoryRequest.HistoryRequestType.MINUTES).interval(1);
                    break;
                case FiveMinutes:
                    builder.type(HistoryRequest.HistoryRequestType.MINUTES).interval(5);
                    break;
                case FifteenMinutes:
                    builder.type(HistoryRequest.HistoryRequestType.MINUTES).interval(15);
                    break;
                case ThirtyMinutes:
                    builder.type(HistoryRequest.HistoryRequestType.MINUTES).interval(30);
                    break;
                case OneHour:
                    builder.type(HistoryRequest.HistoryRequestType.MINUTES).interval(60);
                    break;
                case OneDay:
                    builder.type(HistoryRequest.HistoryRequestType.DAILY);
                    break;
                case OneWeek:
                    builder.type(HistoryRequest.HistoryRequestType.WEEKLY);
                    break;
                case OneMonth:
                    builder.type(HistoryRequest.HistoryRequestType.MONTHLY);
                    break;
                default:
                    Log.e("Magellan", "getIntervalAsDuration(): intervalUnit is corrupt");
                    return null;
            }

            builder.start(query.start);
            builder.end(query.end);

            HistoryRequest built = builder.build();
            Map<String, Object> params = builder.build().parameters();
            final Collection<HistoryBar> rawQuotes = mClient.fetch(built).all();
            if (rawQuotes == null)
                return null;

            HistoryBar [] tmp = new HistoryBar [rawQuotes.size()];
            rawQuotes.toArray(tmp);

            List<Quote> quotes = new ArrayList<Quote>(rawQuotes.size());
            for (int i = 0; i < rawQuotes.size(); i++)
                quotes.add(new Quote(new DateTime(tmp[i].getTimestamp()), (float)tmp[i].getOpen(), (float)tmp[i].getClose(), (float)tmp[i].getLow(), (float)tmp[i].getHigh(), tmp[i].getVolume()));
            return quotes;
        }
        catch (Exception e)
        {
            Log.e("Magellan", String.format("Equity line data for symbol '%s'could not be retrieved!", query.symbol));
            return null;
        }
    }
}