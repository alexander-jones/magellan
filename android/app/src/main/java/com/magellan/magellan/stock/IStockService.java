package com.magellan.magellan.stock;

import java.util.List;

public interface IStockService {
    List<Stock> execute(StockQuery stockPrefix);
}