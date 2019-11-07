package org.flowable.eventdemo.adapter;

import org.flowable.eventregistry.api.EventRegistry;
import org.flowable.eventregistry.api.InboundEventChannelAdapter;

public abstract class AbstractKafkaInboundChannelAdapter implements InboundEventChannelAdapter {

    protected String channelKey;
    protected EventRegistry eventRegistry;

    @Override
    public void setChannelKey(String channelKey) {
        this.channelKey = channelKey;
    }

    @Override
    public void setEventRegistry(EventRegistry eventRegistry) {
        this.eventRegistry = eventRegistry;
    }

}
