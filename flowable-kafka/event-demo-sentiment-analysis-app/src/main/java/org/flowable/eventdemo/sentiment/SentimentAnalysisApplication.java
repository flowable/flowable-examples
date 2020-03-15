package org.flowable.eventdemo.sentiment;

import org.flowable.eventdemo.service.RandomSentimentService;
import org.flowable.eventdemo.service.SentimentService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SentimentAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentimentAnalysisApplication.class, args);
    }

    @Bean
    public RandomSentimentService randomSentimentService() {
        return new RandomSentimentService();
    }

    @Bean
    public SentimentService sentimentService() {
        return new SentimentService();
    }

}
