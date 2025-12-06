package com.securecoda.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
@Configuration
public class CodaWebClientConfig {
    @Value("${coda.api.token:}") private String apiToken;
    @Value("${coda.base-url:https://coda.io/apis/v1}") private String baseUrl;
    @Bean public WebClient codaWebClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
        WebClient.Builder b = WebClient.builder().baseUrl(baseUrl).exchangeStrategies(strategies);
        if (apiToken != null && !apiToken.isEmpty()) b.defaultHeader("Authorization", "Bearer " + apiToken);
        return b.build();
    }
}
