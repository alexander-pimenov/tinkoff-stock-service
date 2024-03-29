package com.pimalex.tinkoff.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pimalex.tinkoff.models.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.model.rest.InstrumentType;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrumentList;

import java.util.concurrent.CompletableFuture;

@ExtendWith(MockitoExtension.class)
class TinkoffStockServiceTest {
    @Mock
    private CompletableFuture<MarketInstrumentList> completableFuture;
    @Mock
    private OpenApi api;
    @Mock
    MarketContext marketContext;

    private TinkoffStockService tinkoffStockService;

    public static final String STOCK_NAME = "Test name";
    public static final String STOCK_TICKER = "Test ticker";
    public static final String STOCK_TYPE = "Etf";
    public static final com.pimalex.tinkoff.models.Currency STOCK_CURR = com.pimalex.tinkoff.models.Currency.RUB;

    @BeforeEach
    void beforeEach() {
        MarketInstrumentList list = new MarketInstrumentList();
        MarketInstrument marketInstrument = new MarketInstrument();
        marketInstrument.setName(STOCK_NAME);
        marketInstrument.setTicker(STOCK_TICKER);
        marketInstrument.setType(InstrumentType.ETF);
        marketInstrument.setCurrency(ru.tinkoff.invest.openapi.model.rest.Currency.RUB);
        list.addInstrumentsItem(marketInstrument);

        when(api.getMarketContext())
                .thenReturn(marketContext);
        when(marketContext.searchMarketInstrumentsByTicker(anyString()))
                .thenReturn(completableFuture);
        when(completableFuture.join())
                .thenReturn(list);
        tinkoffStockService = new TinkoffStockService(api);
    }

    @Test
    void getStockByTicker() {
        Stock actualStock = tinkoffStockService.getStockByTicker("TEST");
        assertEquals(STOCK_NAME, actualStock.getName());
        assertEquals(STOCK_TICKER, actualStock.getTicker());
        assertEquals(STOCK_TYPE, actualStock.getType());
        assertEquals(STOCK_CURR, actualStock.getCurrency());

        verify(api, times(1)).getMarketContext();
    }
}