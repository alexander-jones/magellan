package com.magellan.magellan.quote;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.magellan.magellan.ApplicationContext;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QuoteQueryTask extends AsyncTask<QuoteQuery, Integer, Long> {

    private IQuoteQueryListener mListener;
    private List<List<Quote>> mQuoteCollections;
    private List<QuoteQuery> mQueries;
    private IQuoteService mQuoteService;
    private File mCacheDir;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd_HH:mm").withZone(ApplicationContext.TRADING_TIME_ZONE);
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
            File endpointDir = new File(mCacheDir, dateTimeFormatter.print(query.getEnd()));
            if (!endpointDir.exists())
                endpointDir.mkdir();

            List<Quote> quotes = null;
            String queryCacheFilename = query.symbol + "_" + query.period.toString() + "_" + query.interval.toString();
            File cachedFile = new File(endpointDir, queryCacheFilename);
            if (cachedFile.exists())
                quotes = Quote.loadFrom(cachedFile);

            if (quotes == null)
            {
                quotes = mQuoteService.execute(query);
                if (quotes != null)
                {
                    mQueries.add(query);
                    mQuoteCollections.add(quotes);
                    try {
                        cachedFile.createNewFile();
                        Quote.saveTo(cachedFile, quotes);
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
}