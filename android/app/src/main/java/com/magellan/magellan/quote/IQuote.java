package com.magellan.magellan.quote;

import org.joda.time.DateTime;

public interface IQuote
{
    DateTime getTime();
    float getClose();
    float getOpen();
    float getLow();
    float getHigh();
    int getVolume();
}
