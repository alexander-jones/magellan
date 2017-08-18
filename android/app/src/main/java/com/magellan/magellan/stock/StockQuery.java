package com.magellan.magellan.stock;

import java.util.List;

public class StockQuery {
    public String symbolTemplate;
    public List<String> mRestrictToExchanges;

    public StockQuery(String inSymbolTemplate, List<String> restrictToExchanges)
    {
        symbolTemplate = inSymbolTemplate;
        mRestrictToExchanges = restrictToExchanges;
    }
}
