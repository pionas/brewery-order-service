package pl.excellentapp.brewery.order.infrastructure.service.beerinventory;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("rest.inventory")
@ExtensionMethod(StringUtils.class)
class HttpBeerInventoryClientProperties {

    private String url;
    private String username;
    private String password;

    public boolean shouldAuth() {
        return username.isNotEmpty() && password.isNotEmpty();
    }
}
