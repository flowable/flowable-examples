package org.flowable.eventdemo;

import org.flowable.eventdemo.config.EngineEventConfigurator;
import org.flowable.eventdemo.constant.KafkaTopicConstants;
import org.flowable.eventdemo.controller.ReviewChannelAdapter;
import org.flowable.eventdemo.controller.SentimentAnalysisChannelAdapter;
import org.flowable.eventdemo.controller.SentimentAnalysisResultChannelAdapter;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootApplication
public class CustomerCaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerCaseApplication.class, args);
    }

    @Bean
    public EngineEventConfigurator engineEventConfigurator(SpringProcessEngineConfiguration processEngineConfiguration,
            ReviewChannelAdapter reviewChannelAdapter, SentimentAnalysisChannelAdapter sentimentAnalysisChannelAdapter,
            SentimentAnalysisResultChannelAdapter sentimentAnalysisResultChannelAdapter) {

        return new EngineEventConfigurator(processEngineConfiguration, reviewChannelAdapter, sentimentAnalysisChannelAdapter, sentimentAnalysisResultChannelAdapter);

    }

    @Bean
    public ReviewChannelAdapter reviewChannelAdapter() {
        return new ReviewChannelAdapter();
    }

    @Bean
    public SentimentAnalysisChannelAdapter sentimentAnalysisChannelAdapter(KafkaTemplate<String, String> kafkaTemplate) {
        return new SentimentAnalysisChannelAdapter(kafkaTemplate, KafkaTopicConstants.TOPIC_SENTIMENT_ANALYSIS);
    }

    @Bean
    public SentimentAnalysisResultChannelAdapter sentimentAnalysisResultChannelAdapter() {
        return new SentimentAnalysisResultChannelAdapter();
    }

    //    @Bean
    //    public KafkaReceiver kafkaReceiver() {
    //        Map<String, Object> consumerProps = new HashMap<>();
    //        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    //        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    //        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "consumer-group-1-client-1");
    //        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group-1");
    //        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    //
    //        ReceiverOptions<Object, Object> consumerOptions = ReceiverOptions.create(consumerProps)
    //            .subscription(Collections.singleton("reviews"))
    //            .addAssignListener(partitions -> System.out.println("onPartitionsAssigned " + partitions))
    //            .addRevokeListener(partitions ->  System.out.println("onPartitionsRevoked " + partitions));
    //
    //        return KafkaReceiver.create(consumerOptions);
    //    }

}
