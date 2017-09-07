package com.magellan.magellan.equity;

import java.util.List;

public interface IEquityService {
    List<Equity> execute(EquityQuery stockPrefix);
}