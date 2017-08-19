package com.magellan.magellan.stock;

import java.util.List;

public interface IStockQueryListener {
    public void onStocksReceived(List<List<Stock>> results);
}
