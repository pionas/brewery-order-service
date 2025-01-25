package pl.excellentapp.brewery.order.infrastructure.rest.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import({RestTemplateConfiguration.class})
@Profile("it")
public abstract class AbstractIT {

    @Autowired
    protected TestRestTemplate restTemplate;

}