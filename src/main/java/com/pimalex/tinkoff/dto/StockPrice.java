package com.pimalex.tinkoff.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class StockPrice {
    //значение идентификатора figi
    String figi;
    //Цена бумаги
    Double price;
}
