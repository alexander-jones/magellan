package com.magellan.magellan.quote;

import java.util.List;

public interface IQuoteQueryListener
{
    public void onQuotesReceived(List<List<IQuote>> mQuoteResults);
}