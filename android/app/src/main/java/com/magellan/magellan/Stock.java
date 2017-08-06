package com.magellan.magellan;

import android.os.AsyncTask;
import android.util.Log;

import com.barchart.ondemand.BarchartOnDemandClient;
import com.barchart.ondemand.api.HistoryRequest;
import com.barchart.ondemand.api.responses.History;
import com.barchart.ondemand.api.responses.HistoryBar;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Stock {

    public static enum IntervalType
    {
        Minute,
        Daily,
        Weekly,
        Monthly
    }

    public static class HistoryQuery
    {
        public String symbol;
        public DateTime start;
        public DateTime end;
        public IntervalType intervalType;
        public int interval;

        public HistoryQuery(String sym, DateTime st, DateTime e, IntervalType ic, int i)
        {
            symbol = sym;
            start = st;
            end = e;
            intervalType = ic;
            interval = i;
        }
    }

    public static interface IQuote
    {
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

    public static interface IStockService
    {
        IQuoteCollection execute(HistoryQuery query);
    }

    private static class BarChartQuote implements IQuote
    {
        private HistoryBar mSource;
        public BarChartQuote(HistoryBar source) { mSource = source;}
        public float getClose() { return (float)mSource.getClose();}
        public float getOpen() { return (float)mSource.getClose();}
        public float getLow() { return (float)mSource.getClose();}
        public float getHigh() { return (float)mSource.getClose();}
        public int getVolume() { return mSource.getVolume();}
    }

    private static class BarChartQuoteCollection implements IQuoteCollection
    {
        HistoryBar [] mSource;
        public BarChartQuoteCollection(Collection<HistoryBar> source) { source.toArray(mSource);}
        public IQuote get(int i) { return new BarChartQuote(mSource[i]);}
        public int size() {return mSource.length;}
    }

    private static class BarChartStockService implements IStockService
    {
        public IQuoteCollection execute(HistoryQuery query)
        {
            try {
                BarchartOnDemandClient onDemand = new BarchartOnDemandClient.Builder().apiKey("053b0a25336ff63cdaccec0316ed8b84").baseUrl("http://marketdata.websol.barchart.com/").build();
                final HistoryRequest.Builder builder = new HistoryRequest.Builder();
                builder.symbol(query.symbol).interval(query.interval);
                if (query.intervalType == null)
                    builder.type(HistoryRequest.HistoryRequestType.MINUTES);
                else {
                    switch (query.intervalType) {
                        case Minute:
                            builder.type(HistoryRequest.HistoryRequestType.MINUTES);
                            break;
                        case Daily:
                            builder.type(HistoryRequest.HistoryRequestType.DAILY);
                            break;
                        case Weekly:
                            builder.type(HistoryRequest.HistoryRequestType.WEEKLY);
                            break;
                        case Monthly:
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
                final History history = onDemand.fetch(built);
                return new BarChartQuoteCollection(history.all());
            }
            catch (Exception e)
            {
                Log.e("Magellan", String.format("Stock line data for symbol '%s'could not be retrieved!", query.symbol));
                return null;
            }
        }
    }

    public static interface HistoryQueryListener
    {
        public void onStockHistoryRetrieved(List<IQuoteCollection> mQuoteResults);
    }

    public static class HistoryTask extends AsyncTask<HistoryQuery, Integer, Long> {

        private HistoryQueryListener mListener;
        private List<IQuoteCollection> mStockHistories;

        public HistoryTask(HistoryQueryListener listener)
        {
            super();
            mListener = listener;
        }

        protected Long doInBackground(HistoryQuery... queries) {
            BarChartStockService stockService = new BarChartStockService();
            int count = queries.length;
            long result = 0;
            mStockHistories = new ArrayList<IQuoteCollection>();
            for (int i = 0; i < count; i++) {
                HistoryQuery query = queries[i];
                mStockHistories.add(stockService.execute(query));
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
}
