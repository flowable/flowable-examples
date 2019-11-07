package org.flowable.eventdemo.adapter;

import org.flowable.eventregistry.api.OutboundEventChannelAdapter;
import org.springframework.kafka.core.KafkaTemplate;

public abstract class AbstractKafkaOutboundChannelAdapter implements OutboundEventChannelAdapter {

    protected KafkaTemplate<String, String> kafkaTemplate;
    protected String topic;

    public AbstractKafkaOutboundChannelAdapter(KafkaTemplate<String, String> kafkaTemplate, String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void sendEvent(String event) {
        kafkaTemplate.send(topic, event);
    }

}
