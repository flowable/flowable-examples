package org.flowable.eventdemo.service;

import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.amazonaws.services.comprehend.model.DetectSentimentRequest;
import com.amazonaws.services.comprehend.model.DetectSentimentResult;

@Service
public class SentimentService {

    public String analyse(String comment) {
        AWSCredentialsProvider awsCreds = DefaultAWSCredentialsProviderChain.getInstance();

        AmazonComprehend comprehendClient =
            AmazonComprehendClientBuilder.standard()
                .withCredentials(awsCreds)
                .withRegion("eu-central-1")
                .build();

        // Call detectSentiment API
        System.out.println("Calling AWS Comprehend API");
        DetectSentimentRequest detectSentimentRequest = new DetectSentimentRequest()
            .withText(comment)
            .withLanguageCode("en");
        DetectSentimentResult detectSentimentResult = comprehendClient.detectSentiment(detectSentimentRequest);
        System.out.println("Finished calling AWS Comprehend API. Result: " + detectSentimentResult);
        return detectSentimentResult.getSentiment();
    }

}
