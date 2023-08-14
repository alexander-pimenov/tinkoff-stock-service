package com.pimalex.tinkoff.controllers;

import com.pimalex.tinkoff.dto.FigiesDto;
import com.pimalex.tinkoff.dto.StocksDto;
import com.pimalex.tinkoff.dto.StocksPricesDto;
import com.pimalex.tinkoff.dto.TickersDto;
import com.pimalex.tinkoff.models.Stock;
import com.pimalex.tinkoff.services.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class StockController {

    private final StockService stockService;

    /**
     * Получение информации ценной бумаге по её тикеру.
     *
     * @param ticker тикер, по которому находим нашу акцию/облигацию.
     * @return Stock - модель ценной бумаги.
     */
    @GetMapping("/stocks/{ticker}")
    public Stock getStock(@PathVariable String ticker) {
        return stockService.getStockByTicker(ticker);
    }

    @PostMapping("/stocks/getStocksByTickers")
    public StocksDto getStocksByTickers(@RequestBody TickersDto tickers) {
        return stockService.getStocksByTickers(tickers);
    }

    @PostMapping("/prices")
    public StocksPricesDto getPricesStocksByFigies(@RequestBody FigiesDto figiesDto) {
        return stockService.getPricesStocksByFigies(figiesDto);
    }
}
