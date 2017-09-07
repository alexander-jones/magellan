package com.magellan.magellan.equity;

import java.util.List;

public interface IEquityQueryListener {
    public void onStocksReceived(List<List<Equity>> results);
}
