package org.flowable.eventdemo.controller;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.flowable.eventdemo.adapter.AbstractKafkaInboundChannelAdapter;
import org.flowable.eventdemo.constant.KafkaTopicConstants;
import org.springframework.kafka.annotation.KafkaListener;

public class SentimentAnalysisResultChannelAdapter extends AbstractKafkaInboundChannelAdapter {

    @KafkaListener(topics = KafkaTopicConstants.TOPIC_SENTIMENT_ANALYSIS_RESULT)
    public void listen(ConsumerRecord<String, String> record) {
        eventRegistry.eventReceived(channelKey, record.value());
    }

    /*
     * Reactive version -> eventReceived needs to be made reactive to make this fully reactive.
     */

//    private KafkaReceiver kafkaReceiver;

//    public KafkaInboundChannelAdapter(KafkaReceiver kafkaReceiver) {
//        this.kafkaReceiver = kafkaReceiver;
//    }

//    public void start() {
//        ((Flux<ReceiverRecord>) kafkaReceiver.receive())
//            .doOnNext(record -> {
//                eventRegistry.eventReceived(channelKey, (String) record.value());
//                record.receiverOffset().acknowledge();
//            })
//            .subscribe();
//    }

}
