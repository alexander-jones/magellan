package com.magellan.magellan.quote;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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
    private IQuoteService mQuoteService;
    private File mCacheDir;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd_HH:mm").withZone(DateTimeZone.forID("America/New_York"));
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
        for (int i = 0; i < count; i++) {
            QuoteQuery query = queries[i];
            String queryCacheFilename = query.symbol + "_" + query.period.toString() + "_" + query.intervalUnit.toString() + "_" + Integer.toString(query.interval) + "_" + dateTimeFormatter.print(query.getEnd());

            List<Quote> quotes = null;
            File cachedFile = new File(mCacheDir, queryCacheFilename);
            if (cachedFile.exists())
                quotes = Quote.loadFrom(cachedFile);

            if (quotes == null)
            {
                quotes = mQuoteService.execute(query);
                if (quotes != null)
                {
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
                mQuoteCollections.add(quotes);

            publishProgress((int) ((i / (float) count) * 100));

            if (isCancelled()) break;
        }
        return result;
    }

    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    protected void onPostExecute(Long result) {

        mListener.onQuotesReceived(mQuoteCollections);
    }
}