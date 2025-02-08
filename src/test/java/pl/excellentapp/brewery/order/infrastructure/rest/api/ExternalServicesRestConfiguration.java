package pl.excellentapp.brewery.order.infrastructure.rest.api;

import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@TestConfiguration
class ExternalServicesRestConfiguration {

    private final WireMockServer wireMockServer;

    public ExternalServicesRestConfiguration() {
        this.wireMockServer = new WireMockServer(wireMockConfig().port(8888));
        this.wireMockServer.start();
    }

    @PreDestroy
    public void stopWireMock() {
        wireMockServer.stop();
    }

    @Bean(destroyMethod = "stop")
    public WireMockServer wireMockServer() {
        return wireMockServer;
    }
}