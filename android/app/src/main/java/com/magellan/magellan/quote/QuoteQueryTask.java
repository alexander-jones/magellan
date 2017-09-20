package com.magellan.magellan.quote;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.magellan.magellan.ApplicationContext;

import org.joda.time.DateTime;
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

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd_HH:mm").withZone(ApplicationContext.getTradingTimeZone());
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
            File endOfPeriodCachedFile = getCacheFile(query.symbol, query.period, query.interval, query.end);

            List<Quote> quotes = null;
            if (endOfPeriodCachedFile.exists())
                quotes = Quote.loadFrom(endOfPeriodCachedFile);

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
                            Quote firstQuote = quotes.get(0);
                            Quote lastQuote = quotes.get(quotes.size() -1);
                            if (firstQuote.time.isEqual(query.start) && lastQuote.time.isEqual(query.end)) {
                                endOfPeriodCachedFile.createNewFile();
                                Quote.saveTo(endOfPeriodCachedFile, quotes);
                            }
                            /*else // For now don't cache intermediate results.
                            {
                                File intermediateFile = getCacheFile(query.symbol, query.period, query.interval, lastQuote.time);
                                intermediateFile.createNewFile();
                                Quote.saveTo(intermediateFile, quotes);
                            }*/
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

    private File getCacheFile(String symbol, QuoteQuery.Period period, QuoteQuery.Interval interval, DateTime dateTime)
    {
        File endpointDir = new File(mCacheDir, dateTimeFormatter.print(dateTime));
        if (!endpointDir.exists())
            endpointDir.mkdir();

        String queryCacheFilename = symbol + "_" + period.toString() + "_" + interval.toString();
        return new File(endpointDir, queryCacheFilename);
    }

    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    protected void onPostExecute(Long result) {

        mListener.onQuotesReceived(mQueries, mQuoteCollections);
    }
}