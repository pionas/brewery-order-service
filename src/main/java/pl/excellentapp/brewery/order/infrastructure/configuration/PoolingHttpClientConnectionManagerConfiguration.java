package pl.excellentapp.brewery.order.infrastructure.configuration;

import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
class PoolingHttpClientConnectionManagerConfiguration {

    @Bean
    PoolingHttpClientConnectionManager poolingHttpClientConnectionManager(HttpClientProperties properties) {
        final var connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(properties.getMaxTotal());
        connectionManager.setDefaultMaxPerRoute(properties.getMaxPerRoute());
        connectionManager.setDefaultSocketConfig(getSocketConfig(properties.getSocketTimeout()));
        connectionManager.closeIdle(TimeValue.of(properties.getIdleTimeout()));
        return connectionManager;
    }

    private SocketConfig getSocketConfig(Duration duration) {
        return SocketConfig.custom().setSoTimeout(Timeout.of(duration)).build();
    }
}
