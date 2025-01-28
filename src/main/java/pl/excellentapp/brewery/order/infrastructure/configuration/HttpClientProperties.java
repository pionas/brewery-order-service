package pl.excellentapp.brewery.order.infrastructure.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties("http-client")
class HttpClientProperties {

    private int maxTotal;
    private int maxPerRoute;
    private Duration socketTimeout;
    private Duration idleTimeout;
    private Duration keepAliveTime;
}
