package pl.ksef.hub.integration.ksef.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class KsefWebClientConfig {

    @Value("${ksef.api.base-url}")
    private String ksefBaseUrl;

    @Value("${ksef.api.timeout:30000}")
    private int timeout;

    @Bean
    public WebClient ksefWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                .responseTimeout(Duration.ofMillis(timeout))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(timeout, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(ksefBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("KSeF Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) ->
                    values.forEach(value -> log.debug("KSeF Request Header: {}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("KSeF Response Status: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}
