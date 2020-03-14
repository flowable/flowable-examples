package org.flowable.eventdemo.config;

import org.flowable.eventregistry.api.definition.EventPayloadTypes;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.context.SmartLifecycle;

public class EngineEventConfigurator implements SmartLifecycle {

    private SpringProcessEngineConfiguration processEngineConfiguration;
    private SentimentAnalysisChannelAdapter sentimentAnalysisChannelAdapter;
    private SentimentAnalysisResultChannelAdapter sentimentAnalysisResultChannelAdapter;

    private boolean running;

    public EngineEventConfigurator(SpringProcessEngineConfiguration processEngineConfiguration,
            SentimentAnalysisChannelAdapter sentimentAnalysisChannelAdapter,
            SentimentAnalysisResultChannelAdapter sentimentAnalysisResultChannelAdapter) {
        this.processEngineConfiguration = processEngineConfiguration;
        this.sentimentAnalysisChannelAdapter = sentimentAnalysisChannelAdapter;
        this.sentimentAnalysisResultChannelAdapter = sentimentAnalysisResultChannelAdapter;
    }

    @Override
    public void start() {
        // Sentiment analysis channel
        processEngineConfiguration.getEventRegistry()
            .newInboundChannelDefinition()
            .key("sentimentAnalysisChannel")
            .channelAdapter(sentimentAnalysisChannelAdapter)
            .jsonDeserializer()
            .fixedEventKey("sentimentAnalysisEvent")
            .jsonFieldsMapDirectlyToPayload()
            .register();

        // Sentiment analysis event
        processEngineConfiguration.getEventRegistry()
            .newEventDefinition()
            .key("sentimentAnalysisEvent")
            .inboundChannelKey("sentimentAnalysisChannel")
            .payload("userId", EventPayloadTypes.STRING)
            .payload("comment", EventPayloadTypes.STRING)
            .register();

        // Sentiment result channel
        processEngineConfiguration.getEventRegistry()
            .newOutboundChannelDefinition()
            .key("sentimentAnalysisResultChannel")
            .channelAdapter(sentimentAnalysisResultChannelAdapter)
            .xmlSerializer()
            .register();

        // Sentiment result event
        processEngineConfiguration.getEventRegistry()
            .newEventDefinition()
            .key("sentimentAnalysisResultEvent")
            .outboundChannelKey("sentimentAnalysisResultChannel")
            .payload("userId", EventPayloadTypes.STRING)
            .payload("comment", EventPayloadTypes.STRING)
            .payload("sentiment", EventPayloadTypes.STRING)
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
