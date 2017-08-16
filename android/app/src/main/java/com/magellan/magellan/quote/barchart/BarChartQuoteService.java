package com.magellan.magellan.quote.barchart;

import android.util.Log;

import com.barchart.ondemand.BarchartOnDemandClient;
import com.barchart.ondemand.api.HistoryRequest;
import com.barchart.ondemand.api.responses.HistoryBar;
import com.magellan.magellan.quote.IQuoteCollection;
import com.magellan.magellan.quote.IQuoteService;
import com.magellan.magellan.quote.QuoteQuery;

import java.util.Collection;
import java.util.Map;

public class BarChartQuoteService implements IQuoteService
{
    BarchartOnDemandClient mClient;
    public BarChartQuoteService()
    {
        mClient = new BarchartOnDemandClient.Builder().apiKey("053b0a25336ff63cdaccec0316ed8b84").baseUrl("http://marketdata.websol.barchart.com/").build();
    }

    public IQuoteCollection execute(QuoteQuery query)
    {
        try {
            final HistoryRequest.Builder builder = new HistoryRequest.Builder();
            builder.symbol(query.symbol).interval(query.interval);
            if (query.intervalUnit == null)
                builder.type(HistoryRequest.HistoryRequestType.MINUTES);
            else {
                switch (query.intervalUnit) {
                    case Minute:
                        builder.type(HistoryRequest.HistoryRequestType.MINUTES);
                        break;
                    case Day:
                        builder.type(HistoryRequest.HistoryRequestType.DAILY);
                        break;
                    case Week:
                        builder.type(HistoryRequest.HistoryRequestType.WEEKLY);
                        break;
                    case Month:
                        builder.type(HistoryRequest.HistoryRequestType.MONTHLY);
                        break;
                    default:
                        Log.e("Magellan", "Unhandled interval type!");
                        break;
                }
            }

            if (query.start != null)
                builder.start(query.start);

            if (query.end != null)
                builder.end(query.end);

            HistoryRequest built = builder.build();
            Map<String, Object> params = builder.build().parameters();
            final Collection<HistoryBar> quotes = mClient.fetch(built).all();
            if (quotes == null)
                return null;
            return new BarChartQuoteCollection(quotes);
        }
        catch (Exception e)
        {
            Log.e("Magellan", String.format("Stock line data for symbol '%s'could not be retrieved!", query.symbol));
            return null;
        }
    }
}