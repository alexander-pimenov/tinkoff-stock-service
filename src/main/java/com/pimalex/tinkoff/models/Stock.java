package com.pimalex.tinkoff.models;


import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Модель ценной бумаги. Поля заполняются данными с сайта Tinkoff.
 * {@link @Value} - нужна, чтобы нормально делать json.
 */
@Value
@AllArgsConstructor
public class Stock {
    String ticker;
    //Идентификатор цены
    String figi;
    //Название ценной бумаги
    String name;
    //Акция фонда или облигация
    String type;
    //Валюта в которой хранится акция
    Currency currency;
    //Информация откуда эту акцию притащили/взяли (Moex or Tinkoff)
    String source;


}
