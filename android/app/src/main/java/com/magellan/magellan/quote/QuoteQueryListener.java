package com.magellan.magellan.quote;

import java.util.List;

public interface QuoteQueryListener
{
    public void onQuotesReceived(List<IQuoteCollection> mQuoteResults);
}