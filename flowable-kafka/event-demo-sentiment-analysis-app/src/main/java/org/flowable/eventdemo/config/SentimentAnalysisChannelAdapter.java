package org.flowable.eventdemo.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.flowable.eventdemo.adapter.AbstractKafkaInboundChannelAdapter;
import org.flowable.eventdemo.constant.KafkaTopicConstants;
import org.springframework.kafka.annotation.KafkaListener;

public class SentimentAnalysisChannelAdapter extends AbstractKafkaInboundChannelAdapter {

    @KafkaListener(topics = KafkaTopicConstants.TOPIC_SENTIMENT_ANALYSIS)
    public void listen(ConsumerRecord<String, String> record) {
        eventRegistry.eventReceived(channelKey, record.value());
    }

}
