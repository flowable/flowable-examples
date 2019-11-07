package org.flowable.eventdemo.controller;

import org.flowable.eventdemo.adapter.AbstractKafkaOutboundChannelAdapter;
import org.springframework.kafka.core.KafkaTemplate;

public class SentimentAnalysisChannelAdapter extends AbstractKafkaOutboundChannelAdapter {

    public SentimentAnalysisChannelAdapter(KafkaTemplate kafkaTemplate, String topic) {
        super(kafkaTemplate, topic);
    }

}
