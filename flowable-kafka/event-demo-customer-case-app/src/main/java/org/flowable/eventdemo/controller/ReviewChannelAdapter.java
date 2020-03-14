package org.flowable.eventdemo.controller;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.flowable.eventdemo.adapter.AbstractKafkaInboundChannelAdapter;
import org.flowable.eventdemo.constant.KafkaTopicConstants;
import org.springframework.kafka.annotation.KafkaListener;

public class ReviewChannelAdapter extends AbstractKafkaInboundChannelAdapter {

    private AtomicLong eventCounter = new AtomicLong(0);

    @KafkaListener(topics = KafkaTopicConstants.TOPIC_REVIEWS)
    public void listen(ConsumerRecord<String, String> record) {
        eventCounter.incrementAndGet();
        eventRegistry.eventReceived(channelKey, record.value());
    }

    public long getEventCount() {
        return eventCounter.get();
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
