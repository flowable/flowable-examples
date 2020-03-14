package org.flowable.eventdemo.sentiment;

import org.flowable.eventdemo.config.EngineEventConfigurator;
import org.flowable.eventdemo.config.SentimentAnalysisChannelAdapter;
import org.flowable.eventdemo.config.SentimentAnalysisResultChannelAdapter;
import org.flowable.eventdemo.constant.KafkaTopicConstants;
import org.flowable.eventdemo.service.RandomSentimentService;
import org.flowable.eventdemo.service.SentimentService;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootApplication
public class SentimentAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentimentAnalysisApplication.class, args);
    }

    @Bean
    public EngineEventConfigurator engineEventConfigurator(SpringProcessEngineConfiguration processEngineConfiguration,
            SentimentAnalysisChannelAdapter sentimentAnalysisChannelAdapter, SentimentAnalysisResultChannelAdapter sentimentAnalysisResultChannelAdapter) {

        return new EngineEventConfigurator(processEngineConfiguration,
            sentimentAnalysisChannelAdapter,
            sentimentAnalysisResultChannelAdapter);

    }

    @Bean
    public SentimentAnalysisChannelAdapter sentimentAnalysisChannelAdapter() {
        return new SentimentAnalysisChannelAdapter();
    }

    @Bean
    public SentimentAnalysisResultChannelAdapter sentimentAnalysisResultChannelAdapter(KafkaTemplate<String, String> kafkaTemplate) {
        return new SentimentAnalysisResultChannelAdapter(kafkaTemplate, KafkaTopicConstants.TOPIC_SENTIMENT_ANALYSIS_RESULT);
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
