package pl.excellentapp.brewery.order.infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@Configuration(proxyBeanMethods = false)
class TaskConfiguration {

    @Bean
    TaskExecutor taskExecutor(@Value("${spring.application.name}") String applicationName) {
        return new SimpleAsyncTaskExecutor(applicationName.concat("-"));
    }
}
