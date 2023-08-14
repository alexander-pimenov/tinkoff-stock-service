package com.pimalex.tinkoff.services;

import com.pimalex.tinkoff.dto.FigiesDto;
import com.pimalex.tinkoff.dto.StockPrice;
import com.pimalex.tinkoff.dto.StocksDto;
import com.pimalex.tinkoff.dto.StocksPricesDto;
import com.pimalex.tinkoff.dto.TickersDto;
import com.pimalex.tinkoff.exceptions.StockNotFoundException;
import com.pimalex.tinkoff.models.Currency;
import com.pimalex.tinkoff.models.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrumentList;
import ru.tinkoff.invest.openapi.model.rest.Orderbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TinkoffStockService implements StockService {

    private final OpenApi openApi;

    /**
     * Благодаря этому ассинхронному методу мы не будем ждать выполнения каждого
     * запроса отдельно, а будем кидать например 10 запросов, а в дальнейшем будем
     * ждат ответ и джоинить их в одном месте в главный поток.
     * Возвращается CompletableFuture<MarketInstrumentList> из метода API Tinkoff
     * searchMarketInstrumentsByTicker
     *
     * @param ticker названеи тикера
     * @return CompletableFuture и это обеспечит возможность использовать ассинхронность.
     */
    @Async
    private CompletableFuture<MarketInstrumentList> getMarketInstrumentTicker(String ticker) {
        log.info("Getting {} from Tinkoff", ticker);
        //получаем контекст
        var context = openApi.getMarketContext();
        //возвращаем из контекста список ценных бумаг по тикерам
        return context.searchMarketInstrumentsByTicker(ticker);
    }

    @Override
    public Stock getStockByTicker(String ticker) {

        //вынесли это в отдельный ассинхронный метод
//        var context = openApi.getMarketContext();
//        CompletableFuture<MarketInstrumentList> listCompletableFuture = context.searchMarketInstrumentsByTicker(ticker);

        var cf = getMarketInstrumentTicker(ticker);
        var list = cf.join().getInstruments();
        if (list.isEmpty()) {
            throw new StockNotFoundException(String.format("Stock %S not found", ticker));
        }

        //возьмем item, он стоит на первом месте
        var item = list.get(0);
        //возьмем данные из item и обернем в нашу модель Stock
        //и вернем в контроллер

        return new Stock(
                item.getTicker(),
                item.getFigi(),
                item.getName(),
                item.getType().getValue(),
                Currency.valueOf(item.getCurrency().getValue()),
                "TINKOFF" //здесь немного хардкода
        );
    }

    /**
     * Объяснение как этот метод работает:
     * на вход приходит список с 40 тикерами ценных бумаг ([SBER, ALI, TINKOFF, ...]).
     * Каждый тикер мы кладем в метод
     * getMarketInstrumentTicker и получаем на выходе CompletableFuture. Не дожидаемся каких либо данных,
     * все 40 запросов в Тинькофф уйдут практически параллельно.
     * Далее в месте CompletableFuture::join дожидаемся пока все запросы отработают. После этого у нас
     * есть один поток с заполненными данными. Т.е. мы с Тинькова получили все данные, которые
     * запрашивали. Проходим по каждому запросу и смотрим, а нашли ли мы что то по данному тикеру
     * mi.getInstruments().isEmpty(). Если нашли, то получаем этот тикер. Если нет то просто выбрасываем null.
     * Например по тикеру ALI ничего не нашлось и мы просто вернем null.
     * Потом те которые нашли обернем в наши объекты Stock. И вернем пользователю new StocksDto(stocks)
     *
     * @param tickers тикер названия ценной бумаги
     * @return объект хранящий данные по ценной бумаге
     */
    @Override
    public StocksDto getStocksByTickers(TickersDto tickers) {
        //можно сделать так для обработки списка, но это будет долго, нужно делать ассинхронно
        tickers.getTickers().forEach(this::getStockByTicker);

        List<CompletableFuture<MarketInstrumentList>> marketInstruments = new ArrayList<>();
        //пробежимся по массиву тикеров, вызывая ассинхронный метод getMarketInstrumentTicker,
        //добавляя в список marketInstruments результат метода
        //и в дальнейшем их сджоиним в дто и отправим пользователю
        tickers.getTickers().forEach(ticker -> marketInstruments.add(getMarketInstrumentTicker(ticker)));
        //После того, как заполнили список объектами CompletableFuture, будем дожидаться выполнения этих методов
        //с помощью метода join (он ждет выполнения потока и мержит их в один)
        List<Stock> stocks = marketInstruments.stream()
                .map(CompletableFuture::join)
                .map(mi -> {
                    if (!mi.getInstruments().isEmpty()) {
                        return mi.getInstruments().get(0);
                    }
                    return null;
                })
                .filter(el -> Objects.nonNull(el))
                .map(mi -> new Stock(
                        mi.getTicker(),
                        mi.getFigi(),
                        mi.getName(),
                        mi.getType().getValue(),
                        Currency.valueOf(mi.getCurrency().getValue()),
                        "TINKOFF"))
                .collect(Collectors.toList());

        return new StocksDto(stocks);
    }

    /**
     * Метод для получения ценны ценной бумаги.
     * Тинькофф требует для получения цены ценной бумаги идентификатор figi.
     * Т.е. цену по тикеру нельзя получить.
     *
     * @param figiesDto Объект хранящий список фиджиков
     * @return объект содержащий список StockPrice
     */
    @Override
    public StocksPricesDto getPricesStocksByFigies(FigiesDto figiesDto) {
        long start = System.currentTimeMillis();
        //Создаем список возвращаемых объектов
        List<CompletableFuture<Optional<Orderbook>>> orderBooks = new ArrayList<>();
        //проходимся по массиву figi
        figiesDto.getFigies().forEach(figi -> orderBooks.add(getOrderBookByFigi(figi)));
        List<StockPrice> prices = orderBooks.stream()
                .map(CompletableFuture::join) //здесь ждем пока все наши вызовы завершаться
                .map(oo -> oo.orElseThrow(() -> new StockNotFoundException("Stock not found."))) //если не нашли цену то кидаем исепшн
                .map(orderBook -> new StockPrice( //теперь всё мапим в нужный тип данных
                        orderBook.getFigi(), //идентификатор
                        orderBook.getLastPrice().doubleValue())) //getLastPrice дает данные в BigDecimal, переложим их в Double
                .collect(Collectors.toList());

        log.info("Time getting prices - {}", System.currentTimeMillis() - start);
        return new StocksPricesDto(prices);
    }

    /**
     * CompletableFuture<Optional<Orderbook>> - возвращается при вызове  тиньковского метода getMarketOrderbook,
     * поэтому такой и возвращаемый тип.
     * Этот метод выполняется ассинхронно.
     *
     * @param figi идентификатор ценной бумаги
     * @return объект CompletableFuture<Optional<Orderbook>>
     */
    @Async
    private CompletableFuture<Optional<Orderbook>> getOrderBookByFigi(String figi) {
        //берем последнюю цену - 0
        var orderBook = openApi.getMarketContext().getMarketOrderbook(figi, 0);
        log.info("Getting price {} from Tinkoff", figi);
        return orderBook;
    }
}
