# Flowable Kafka Event Example

This is an example project that demonstrates how the Flowable Event Registry works with Kafka.
It shows how one can create an architecture as in the video [Flowable business processing from Kafka events](https://www.youtube.com/watch?v=nX0dRiPqOmk) from Devoxx 2019

## Setup

Download and run Kafka.
Create the following topics: sentiment-analysis, sentiment-analysis-results and reviews

Create a postgres database named `eventdemo` with user and pass as flowable.

This is needed for the `CustomerCaseApplication`.
If you want to use another database then change the `spring.datasource.url` in the [application.properties](event-demo-customer-case-app/src/main/resources/application.properties).

## Architecture

There are 4 applications:

* [`ApiGatewayApplication`](event-demo-api-gateway/src/main/java/org/flowable/eventdemo/ApiGatewayApplication.java) - A Spring Cloud Gateway for redirecting the API calls
* [`CustomerCaseApplication`](event-demo-customer-case-app/src/main/java/org/flowable/eventdemo/CustomerCaseApplication.java) - An embedded Flowable Application for customer review management. Uses postgres as a database
* [`SentimentAnalysisApplication`](event-demo-sentiment-analysis-app/src/main/java/org/flowable/eventdemo/sentiment/SentimentAnalysisApplication.java) - An embedded Flowable Application which performs Sentiment Analysis (different than the `CustomerCaseApplication`). Uses in memory h2 as a database
* [`ReviewApplication`](event-demo-review-app/src/main/java/org/flowable/eventdemo/review/ReviewApplication.java) - Reactive Review application responsible for streaming the reviews to Kafka

## Frontend

There is a small frontend in the [frontend](frontend) directory.

Run the following commands to start it:

```sh
cd frontend
yarn install
yarn start
```

This starts a UI on [localhost:3000](http://localhost:3000) and a dashboard at [localhost:3000/#dashboard](http://localhost:3000/#dashboard)
