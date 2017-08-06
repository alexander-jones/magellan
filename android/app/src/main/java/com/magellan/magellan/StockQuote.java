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

public class StockQuote {

    public static class HistoryQuery
    {
        public String symbol;
        public DateTime start;
        public DateTime end;
        public HistoryRequest.HistoryRequestType interval;

        public HistoryQuery(String sym, DateTime st, DateTime e, HistoryRequest.HistoryRequestType i)
        {
            symbol = sym;
            start = st;
            end = e;
            interval = i;
        }
    }

    public static interface HistoryQueryListener
    {
        public void onStockHistoryRetrieved(List<Collection<HistoryBar>> mQuoteResults);
    }

    public static class HistoryTask extends AsyncTask<HistoryQuery, Integer, Long> {

        private HistoryQueryListener mListener;
        private List<Collection<HistoryBar>> mStockHistories;

        public HistoryTask(HistoryQueryListener listener)
        {
            super();
            mListener = listener;
        }

        protected Long doInBackground(HistoryQuery... queries) {
            int count = queries.length;
            long result = 0;
            mStockHistories = new ArrayList<Collection<HistoryBar>>();
            for (int i = 0; i < count; i++) {
                HistoryQuery query = queries[i];
                try
                {
                    BarchartOnDemandClient onDemand = new BarchartOnDemandClient.Builder().apiKey("053b0a25336ff63cdaccec0316ed8b84").baseUrl("http://marketdata.websol.barchart.com/").build();
                    final HistoryRequest.Builder builder = new HistoryRequest.Builder();
                    builder.symbol(query.symbol);
                    if (query.interval == null)
                        builder.type(HistoryRequest.HistoryRequestType.MINUTES);
                    else
                        builder.type(query.interval);

                    if (query.start != null)
                        builder.start(query.start);

                    if (query.end != null)
                        builder.end(query.end);

                    HistoryRequest built = builder.build();
                    Map<String, Object> params = builder.build().parameters();
                    final History history = onDemand.fetch(built);
                    mStockHistories.add(history.all());
                }
                catch (IOException e)
                {
                    Log.e("Magellan", String.format("Stock line data for symbol '%s'could not be retrieved!", query.symbol));
                    result = result & (1 >> i);
                    mStockHistories.add(null);
                    continue;
                }
                catch (Exception e)
                {
                    Log.e("Magellan", String.format("Stock line data for symbol '%s'could not be retrieved!", query.symbol));
                    result = result & (1 >> i);
                    mStockHistories.add(null);
                    continue;
                }
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
