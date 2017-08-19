package com.magellan.magellan.quote;

import java.util.List;

public interface IQuoteService
{
    List<Quote> execute(QuoteQuery query);
}