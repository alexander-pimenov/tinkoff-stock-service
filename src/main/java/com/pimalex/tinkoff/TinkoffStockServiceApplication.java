package com.pimalex.tinkoff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync //включаем ассинхронные обработки
public class TinkoffStockServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TinkoffStockServiceApplication.class, args);
	}

}
