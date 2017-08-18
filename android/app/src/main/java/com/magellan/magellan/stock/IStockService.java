package com.magellan.magellan.stock;

import java.util.List;

public interface IStockService {
    List<IStock> execute(StockQuery stockPrefix);
}