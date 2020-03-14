package org.flowable.eventdemo.config;

import org.flowable.eventdemo.controller.ReviewChannelAdapter;
import org.flowable.eventdemo.controller.SentimentAnalysisChannelAdapter;
import org.flowable.eventdemo.controller.SentimentAnalysisResultChannelAdapter;
import org.flowable.eventregistry.api.definition.EventPayloadTypes;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.context.SmartLifecycle;

public class EngineEventConfigurator implements SmartLifecycle {

    private SpringProcessEngineConfiguration processEngineConfiguration;
    private ReviewChannelAdapter reviewChannelAdapter;
    private SentimentAnalysisChannelAdapter sentimentAnalysisChannelAdapter;
    private SentimentAnalysisResultChannelAdapter sentimentAnalysisResultChannelAdapter;

    private boolean running;

    public EngineEventConfigurator(SpringProcessEngineConfiguration processEngineConfiguration,
            ReviewChannelAdapter reviewChannelAdapter,
            SentimentAnalysisChannelAdapter sentimentAnalysisChannelAdapter,
            SentimentAnalysisResultChannelAdapter sentimentAnalysisResultChannelAdapter) {
        this.processEngineConfiguration = processEngineConfiguration;
        this.reviewChannelAdapter = reviewChannelAdapter;
        this.sentimentAnalysisChannelAdapter = sentimentAnalysisChannelAdapter;
        this.sentimentAnalysisResultChannelAdapter = sentimentAnalysisResultChannelAdapter;
    }

    @Override
    public void start() {

        // Review channel
        processEngineConfiguration.getEventRegistry()
            .newInboundChannelDefinition()
            .key("reviewChannel")
            .channelAdapter(reviewChannelAdapter)
            .jsonDeserializer()
            .fixedEventKey("reviewEvent")
            .jsonFieldsMapDirectlyToPayload()
            .register();

        // Review event
        processEngineConfiguration.getEventRegistry()
            .newEventDefinition()
            .key("reviewEvent")
            .inboundChannelKey("reviewChannel")
            .correlationParameter("userId", EventPayloadTypes.STRING)
            .payload("stationId", EventPayloadTypes.INTEGER)
            .payload("rating", EventPayloadTypes.INTEGER)
            .payload("comment", EventPayloadTypes.STRING)
            .register();

        // Sentiment analysis channel
        processEngineConfiguration.getEventRegistry()
            .newOutboundChannelDefinition()
            .key("sentimentAnalysisChannel")
            .channelAdapter(sentimentAnalysisChannelAdapter)
            .jsonSerializer()
            .register();

        // Sentiment analysis event
        processEngineConfiguration.getEventRegistry()
            .newEventDefinition()
            .key("sentimentAnalysisEvent")
            .outboundChannelKey("sentimentAnalysisChannel")
            .payload("userId", EventPayloadTypes.STRING)
            .payload("comment", EventPayloadTypes.STRING)
            .register();

        // Sentiment result channel
        processEngineConfiguration.getEventRegistry()
            .newInboundChannelDefinition()
            .key("sentimentAnalysisResultChannel")
            .channelAdapter(sentimentAnalysisResultChannelAdapter)
            .xmlDeserializer()
            .fixedEventKey("sentimentAnalysisResultEvent")
            .xmlElementsMapDirectlyToPayload()
            .register();

        // Sentiment result event
        processEngineConfiguration.getEventRegistry()
            .newEventDefinition()
            .key("sentimentAnalysisResultEvent")
            .inboundChannelKey("sentimentAnalysisResultChannel")
            .correlationParameter("userId", EventPayloadTypes.STRING)
            .correlationParameter("comment", EventPayloadTypes.STRING)
            .payload("sentiment", EventPayloadTypes.STRING)
            .register();

        // Demo events just to fill up the UI
        processEngineConfiguration.getEventRegistry()
            .newEventDefinition()
            .key("documentCreated")
            .inboundChannelKey("demo-channel")
            .correlationParameter("folderId", EventPayloadTypes.STRING)
            .payload("documentName", EventPayloadTypes.STRING)
            .register();
        processEngineConfiguration.getEventRegistry()
            .newEventDefinition()
            .key("demoEvent")
            .inboundChannelKey("test-channel")
            .register();

        running = true;
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
