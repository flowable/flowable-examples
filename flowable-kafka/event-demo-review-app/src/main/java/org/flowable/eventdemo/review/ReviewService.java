package org.flowable.eventdemo.review;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Service
public class ReviewService {

    private KafkaSender kafkaSender;
    private ObjectMapper objectMapper;

    public ReviewService(KafkaSender kafkaSender, ObjectMapper objectMapper) {
        this.kafkaSender = kafkaSender;
        this.objectMapper = objectMapper;
    }

    public Mono<Void> sendReviewToKafka(String messageContent) {
        SenderRecord<String, String, Void> message =
            SenderRecord.create(new ProducerRecord<String, String>("reviews", messageContent), null);
        return kafkaSender.send(Mono.just(message)).next();
    }

}
