package org.flowable.eventdemo;

import org.flowable.app.spring.SpringAppEngineConfiguration;
import org.flowable.eventdemo.controller.ReviewEventCounter;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CustomerCaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerCaseApplication.class, args);
    }

    @Bean
    public EngineConfigurationConfigurer<SpringAppEngineConfiguration> customAppEngineConfigurer(ReviewEventCounter reviewEventCounter) {
        return engineConfiguration -> {
            engineConfiguration.addEventRegistryEventConsumer(reviewEventCounter.getConsumerKey(), reviewEventCounter);
        };
    }
}
