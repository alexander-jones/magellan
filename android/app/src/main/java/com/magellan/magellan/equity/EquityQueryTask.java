package com.magellan.magellan.equity;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

public class EquityQueryTask extends AsyncTask<EquityQuery, Integer, Long> {
    private IEquityQueryListener mListener;
    private List<List<Equity>> mResult;
    private IEquityService mService;

    public EquityQueryTask(IEquityService service, IEquityQueryListener listener)
    {
        super();
        mListener = listener;
        mService = service;
    }

    protected Long doInBackground(EquityQuery... queries) {
        int count = queries.length;
        long result = 0;
        mResult = new ArrayList<List<Equity>>();
        for (int i = 0; i < count; i++) {
            EquityQuery query = queries[i];
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
