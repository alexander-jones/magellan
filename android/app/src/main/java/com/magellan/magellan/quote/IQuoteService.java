package com.magellan.magellan.quote;

public interface IQuoteService
{
    IQuoteCollection execute(QuoteQuery query);
}