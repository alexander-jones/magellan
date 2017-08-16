package com.magellan.magellan.quote;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

public class QuoteQueryTask extends AsyncTask<QuoteQuery, Integer, Long> {

    private QuoteQueryListener mListener;
    private List<IQuoteCollection> mQuoteCollections;
    private IQuoteService mQuoteService;

    public QuoteQueryTask(IQuoteService quoteService, QuoteQueryListener listener)
    {
        super();
        mListener = listener;
        mQuoteService = quoteService;
    }

    protected Long doInBackground(QuoteQuery... queries) {
        int count = queries.length;
        long result = 0;
        mQuoteCollections = new ArrayList<IQuoteCollection>();
        for (int i = 0; i < count; i++) {
            QuoteQuery query = queries[i];
            mQuoteCollections.add(mQuoteService.execute(query));
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