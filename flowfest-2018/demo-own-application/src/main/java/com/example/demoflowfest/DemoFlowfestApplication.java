package com.example.demoflowfest;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoFlowfestApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoFlowfestApplication.class, args);
    }

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> customProcessEngineConfigurer() {
        return engineConfiguration -> {
            engineConfiguration.setValidateFlowable5EntitiesEnabled(false);
        };
    }
}
