package com.magellan.magellan;

import android.app.Activity;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

import yahoofinance.YahooFinance;
import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

public class StockQuote {

    public static class Query
    {
        public String symbol;
        public Calendar start;
        public Calendar end;
        public Interval interval;

        public Query(String sym, Calendar st, Calendar e, Interval i)
        {
            symbol = sym;
            start = st;
            end = e;
        }
    }

    public static interface StockQueryListener
    {
        public void onStockHistoryRetrieved(List<List<HistoricalQuote>> mQuoteResults);
    }

    public static class QueryTask extends AsyncTask<Query, Integer, Long> {

        private StockQueryListener mListener;
        private List<List<HistoricalQuote>> mStockHistories;

        public QueryTask(StockQueryListener listener)
        {
            super();
            mListener = listener;
        }

        protected Long doInBackground(Query... queries) {
            int count = queries.length;
            long result = 0;
            mStockHistories = new ArrayList<List<HistoricalQuote>>();
            for (int i = 0; i < count; i++) {
                Query query = queries[i];
                Stock stock;
                try
                {
                    if (query.start == null)
                    {
                        if (query.interval == null)
                            stock = YahooFinance.get(query.symbol);
                        else
                            stock = YahooFinance.get(query.symbol, query.interval);
                    }
                    else if (query.end == null)
                    {
                        if (query.interval == null)
                            stock = YahooFinance.get(query.symbol, query.start);
                        else
                            stock = YahooFinance.get(query.symbol, query.start, query.interval);
                    }
                    else
                    {
                        if (query.interval == null)
                            stock = YahooFinance.get(query.symbol, query.start, query.end);
                        else
                            stock = YahooFinance.get(query.symbol, query.start, query.end, query.interval);
                    }
                    mStockHistories.add(stock.getHistory());
                }
                catch (IOException e)
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
