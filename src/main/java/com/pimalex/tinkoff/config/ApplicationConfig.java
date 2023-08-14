package com.pimalex.tinkoff.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApi;

@Configuration
@EnableConfigurationProperties(ApiConfig.class)
@RequiredArgsConstructor
public class ApplicationConfig {

    private final ApiConfig apiConfig;

    /**
     * 1-й аргумент - SSO токен, чтобы обращаться к Тинькову - хранить будем его в переменных окружения
     * <p>
     * 2-й аргумент - режим песочницы, прописано в application.yml
     *
     * @return настроенный бин OpenApi
     */
    @Bean
    public OpenApi api() {
        String ssoToken = System.getenv("ssoToken");
        return new OkHttpOpenApi(ssoToken, apiConfig.getIsSandBoxMode());

    }
}
