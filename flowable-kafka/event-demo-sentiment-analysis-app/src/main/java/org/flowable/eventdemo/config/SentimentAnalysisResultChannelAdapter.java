package org.flowable.eventdemo.config;

import org.flowable.eventdemo.adapter.AbstractKafkaOutboundChannelAdapter;
import org.springframework.kafka.core.KafkaTemplate;

public class SentimentAnalysisResultChannelAdapter extends AbstractKafkaOutboundChannelAdapter {

    public SentimentAnalysisResultChannelAdapter(KafkaTemplate<String, String> kafkaTemplate, String topic) {
        super(kafkaTemplate, topic);
    }

}
