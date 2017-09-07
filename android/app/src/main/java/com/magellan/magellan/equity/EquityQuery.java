package com.magellan.magellan.equity;

import java.util.List;

public class EquityQuery {
    public String symbolTemplate;
    public List<String> mRestrictToExchanges;

    public EquityQuery(String inSymbolTemplate, List<String> restrictToExchanges)
    {
        symbolTemplate = inSymbolTemplate;
        mRestrictToExchanges = restrictToExchanges;
    }
}
