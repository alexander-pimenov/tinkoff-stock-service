package com.pimalex.tinkoff.services;

import com.pimalex.tinkoff.dto.FigiesDto;
import com.pimalex.tinkoff.dto.StocksDto;
import com.pimalex.tinkoff.dto.StocksPricesDto;
import com.pimalex.tinkoff.dto.TickersDto;
import com.pimalex.tinkoff.models.Stock;

/**
 * StockService в виде интерфейса, чтобы соблюдать последний принцип SOLID
 * И также легко подменять реализации в тестировании.
 */
public interface StockService {
    Stock getStockByTicker(String ticker);

    StocksDto getStocksByTickers(TickersDto tickers);

    StocksPricesDto getPricesStocksByFigies(FigiesDto figiesDto);
}
