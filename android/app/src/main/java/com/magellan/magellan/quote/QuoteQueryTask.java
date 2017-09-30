package com.magellan.magellan.quote;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.magellan.magellan.ApplicationContext;
import com.magellan.magellan.equity.Equity;
import com.magellan.magellan.market.Market;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

public class QuoteQueryTask extends AsyncTask<QuoteQuery, Integer, Long> {

    private IQuoteQueryListener mListener;
    private List<List<Quote>> mQuoteCollections;
    private List<QuoteQuery> mQueries;
    private IQuoteService mQuoteService;
    private File mCacheDir;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd_HH:mm").withZone(Market.getTradingTimeZone());
    public QuoteQueryTask(Context context, IQuoteService quoteService, IQuoteQueryListener listener)
    {
        super();
        mCacheDir = context.getCacheDir();
        mListener = listener;
        mQuoteService = quoteService;
    }

    protected Long doInBackground(QuoteQuery... queries) {
        int count = queries.length;
        long result = 0;
        mQuoteCollections = new ArrayList<List<Quote>>();
        mQueries = new ArrayList<QuoteQuery>();
        for (int i = 0; i < count; i++) {
            QuoteQuery query = queries[i];
            File endOfPeriodCachedFile = query.getCacheFile(mCacheDir);

            List<Quote> quotes = null;
            synchronized (getLockForCache(endOfPeriodCachedFile))
            {
                if (endOfPeriodCachedFile.exists())
                    quotes = Quote.loadFrom(endOfPeriodCachedFile);
            }

            if (quotes == null)
            {
                quotes = mQuoteService.execute(query);
                if (quotes != null)
                {
                    mQueries.add(query);
                    mQuoteCollections.add(quotes);
                    try {
                        if (quotes != null && !quotes.isEmpty())
                        {
                            Quote lastQuote = quotes.get(quotes.size() -1);
                            if (lastQuote.time.isEqual(query.end))  // Only cache up to date results, but results with past data missing is ok (TODO: mark in someway during saving)
                            {
                                synchronized (getLockForCache(endOfPeriodCachedFile)) {
                                    endOfPeriodCachedFile.createNewFile();
                                    Quote.saveTo(endOfPeriodCachedFile, quotes);
                                }
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        Log.e("Magellan","QuoteQueryTask.doInBackground() :" + e.getMessage());
                    }
                }
            }
            else
            {
                mQueries.add(query);
                mQuoteCollections.add(quotes);
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

        mListener.onQuotesReceived(mQueries, mQuoteCollections);
    }

    private static HashMap<String, Object> mLockMap = new HashMap<String,Object>();
    private static Object getLockForCache(File file)
    {
        String key = file.getName();
        Object ret = mLockMap.get(key);
        if (ret == null)
        {
            mLockMap.put(key, file);
            ret = file;
        }
        return ret;
    }
}