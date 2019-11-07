package org.flowable.eventdemo.service;

import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class RandomSentimentService {

    private Random random = new Random();

    public String generator() {
        return random.nextBoolean() ? "negative" : "positive";
    }

}
