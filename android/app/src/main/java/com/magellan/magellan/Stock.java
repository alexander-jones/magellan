package com.magellan.magellan;

import android.os.AsyncTask;
import android.util.Log;

import com.barchart.ondemand.BarchartOnDemandClient;
import com.barchart.ondemand.api.HistoryRequest;
import com.barchart.ondemand.api.responses.History;
import com.barchart.ondemand.api.responses.HistoryBar;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Stock {

    private static IQuoteService mQuoteService = new BarChartStockService();

    public static enum IntervalUnit
    {
        Minute,
        Day,
        Week,
        Month
    }

    public static class HistoryQuery
    {
        public String symbol;
        public DateTime start;
        public DateTime end;
        public IntervalUnit intervalUnit;
        public int interval;

        public HistoryQuery(String sym, DateTime st, DateTime e, IntervalUnit iu, int i)
        {
            symbol = sym;
            start = st;
            end = e;
            intervalUnit = iu;
            interval = i;
        }
    }

    public static interface IQuote
    {
        DateTime getTime();
        float getClose();
        float getOpen();
        float getLow();
        float getHigh();
        int getVolume();
    }

    public static interface IQuoteCollection
    {
        IQuote get(int i);
        int size();
    }

    public static interface IQuoteService
    {
        IQuoteCollection execute(HistoryQuery query);
    }

    public static IQuoteService getQuoteService()
    {
        return mQuoteService;
    }

    public static interface HistoryQueryListener
    {
        public void onStockHistoryRetrieved(List<IQuoteCollection> mQuoteResults);
    }

    public static class HistoryQueryTask extends AsyncTask<HistoryQuery, Integer, Long> {

        private HistoryQueryListener mListener;
        private List<IQuoteCollection> mStockHistories;

        public HistoryQueryTask(HistoryQueryListener listener)
        {
            super();
            mListener = listener;
        }

        protected Long doInBackground(HistoryQuery... queries) {
            IQuoteService quoteService = getQuoteService();
            int count = queries.length;
            long result = 0;
            mStockHistories = new ArrayList<IQuoteCollection>();
            for (int i = 0; i < count; i++) {
                HistoryQuery query = queries[i];
                mStockHistories.add(quoteService.execute(query));
                publishProgress((int) ((i / (float) count) * 100));

                if (isCancelled()) break;
            }
            return result;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {

            mListener.onStockHistoryRetrieved(mStockHistories);
        }
    }

    /**********************************
     BarChart service implementations
    ***********************************/
    private static class BarChartQuote implements IQuote
    {
        private HistoryBar mSource;
        public BarChartQuote(HistoryBar source) { mSource = source;}
        public DateTime getTime() { return DateTime.parse(mSource.getTimestamp());}
        public float getClose() { return (float)mSource.getClose();}
        public float getOpen() { return (float)mSource.getClose();}
        public float getLow() { return (float)mSource.getClose();}
        public float getHigh() { return (float)mSource.getClose();}
        public int getVolume() { return mSource.getVolume();}
    }

    private static class BarChartQuoteCollection implements IQuoteCollection
    {
        HistoryBar [] mSource;
        public BarChartQuoteCollection(Collection<HistoryBar> source) { mSource = new HistoryBar [source.size()]; source.toArray(mSource);}
        public IQuote get(int i) { return new BarChartQuote(mSource[i]);}
        public int size() {return mSource.length;}
    }

    private static class BarChartStockService implements IQuoteService
    {
        BarchartOnDemandClient mClient;
        public BarChartStockService()
        {
            mClient = new BarchartOnDemandClient.Builder().apiKey("053b0a25336ff63cdaccec0316ed8b84").baseUrl("http://marketdata.websol.barchart.com/").build();
        }
        public IQuoteCollection execute(HistoryQuery query)
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
}