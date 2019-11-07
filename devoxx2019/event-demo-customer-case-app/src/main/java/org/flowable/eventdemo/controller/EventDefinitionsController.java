
package org.flowable.eventdemo.controller;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.flowable.cmmn.engine.CmmnEngine;
import org.flowable.eventregistry.api.EventRegistry;
import org.flowable.eventregistry.api.definition.EventCorrelationParameterDefinition;
import org.flowable.eventregistry.api.definition.EventDefinition;
import org.flowable.eventregistry.api.definition.EventPayloadDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Joram Barrez
 */
@RestController
public class EventDefinitionsController {

    private EventRegistry eventRegistry;

    @Autowired
    public EventDefinitionsController(CmmnEngine cmmnEngine) {
        this.eventRegistry = cmmnEngine.getCmmnEngineConfiguration().getEventRegistry();
    }

    @GetMapping("/event-definitions")
    public Collection<EventDefinitionRepresentation> getAllEventDefinitions() {
        return eventRegistry.getAllEventDefinitions().stream().map(e -> new EventDefinitionRepresentation(e)).collect(Collectors.toList());
    }

    public static class EventDefinitionRepresentation {

        private String key;
        private List<CorrelationParameterRepresentation> correlationParameterDefinitions;
        private List<PayloadDefinitionRepresentation> payloadDefinitions;

        public EventDefinitionRepresentation(EventDefinition eventDefinition) {
            this.key = eventDefinition.getKey();
            this.correlationParameterDefinitions = eventDefinition.getCorrelationParameterDefinitions()
                .stream().map(CorrelationParameterRepresentation::new).collect(Collectors.toList());
            this.payloadDefinitions = eventDefinition.getEventPayloadDefinitions()
                .stream().map(PayloadDefinitionRepresentation::new).collect(Collectors.toList());
        }

        public String getKey() {
            return key;
        }
        public void setKey(String key) {
            this.key = key;
        }
        public List<CorrelationParameterRepresentation> getCorrelationParameterDefinitions() {
            return correlationParameterDefinitions;
        }
        public void setCorrelationParameterDefinitions(
            List<CorrelationParameterRepresentation> correlationParameterDefinitions) {
            this.correlationParameterDefinitions = correlationParameterDefinitions;
        }
        public List<PayloadDefinitionRepresentation> getPayloadDefinitions() {
            return payloadDefinitions;
        }
        public void setPayloadDefinitions(List<PayloadDefinitionRepresentation> payloadDefinitions) {
            this.payloadDefinitions = payloadDefinitions;
        }
    }

    public static class CorrelationParameterRepresentation {

        private String name;
        private String type;

        public CorrelationParameterRepresentation(EventCorrelationParameterDefinition correlationParameterDefinition) {
            this.name = correlationParameterDefinition.getName();
            this.type = correlationParameterDefinition.getType();
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
    }


    public static class PayloadDefinitionRepresentation {

        private String name;
        private String type;

        public PayloadDefinitionRepresentation(EventPayloadDefinition eventPayloadDefinition) {
            this.name = eventPayloadDefinition.getName();
            this.type = eventPayloadDefinition.getType();
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
    }

}
