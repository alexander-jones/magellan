package com.magellan.magellan.stock;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

public class StockQueryTask extends AsyncTask<StockQuery, Integer, Long> {
    private IStockQueryListener mListener;
    private List<List<Stock>> mResult;
    private IStockService mService;

    public StockQueryTask(IStockService service, IStockQueryListener listener)
    {
        super();
        mListener = listener;
        mService = service;
    }

    protected Long doInBackground(StockQuery... queries) {
        int count = queries.length;
        long result = 0;
        mResult = new ArrayList<List<Stock>>();
        for (int i = 0; i < count; i++) {
            StockQuery query = queries[i];
            mResult.add(mService.execute(query));
            if (isCancelled()) break;
        }
        return result;
    }

    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    protected void onPostExecute(Long result) {

        mListener.onStocksReceived(mResult);
    }
}
