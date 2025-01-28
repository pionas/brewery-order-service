package pl.excellentapp.brewery.order.infrastructure.configuration;

import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
class ClientHttpRequestFactoryConfiguration {

    @Bean
    ClientHttpRequestFactory clientHttpRequestFactory(PoolingHttpClientConnectionManager connectionManager, HttpClientProperties properties) {
        final var httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.of(properties.getIdleTimeout()))
                .setKeepAliveStrategy(getKeepAliveStrategy(properties.getKeepAliveTime()))
                .build();
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    private ConnectionKeepAliveStrategy getKeepAliveStrategy(Duration keepAliveTime) {
        return (response, context) -> TimeValue.of(keepAliveTime);
    }
}
