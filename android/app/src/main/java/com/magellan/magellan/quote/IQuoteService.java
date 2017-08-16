package com.magellan.magellan.quote;

import java.util.List;

public interface IQuoteService
{
    List<IQuote> execute(QuoteQuery query);
}