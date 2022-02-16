package com.example.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.flowable.spring.boot.FlowableProperties;
import org.flowable.spring.boot.process.FlowableProcessProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author Filip Hrisafov
 * @author Joram Barrez
 */
@Configuration(proxyBeanMethods = false)
public class ProcessEngineCustomConfiguration {

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> processEngineConfigurationConfigurer(FlowableProperties processProperties) {
        return engineConfiguration -> {
            // For some reason the auto deployment is not working, so we manually do it.
            if (processProperties.isCheckProcessDefinitions()) {
                Resource[] resources = engineConfiguration.getDeploymentResources();
                List<Resource> resourceList = new ArrayList<>();
                Collections.addAll(resourceList, resources);
                resourceList.add(new ClassPathResource("processes/oneTaskProcess.bpmn20.xml"));
                resourceList.add(new ClassPathResource("processes/oneServiceTaskDelegateProcess.bpmn20.xml"));
                engineConfiguration.setDeploymentResources(resourceList.toArray(new Resource[0]));
            }

            engineConfiguration.setValidateFlowable5EntitiesEnabled(false);
            engineConfiguration.setEnableConfiguratorServiceLoader(false);
        };
    }

}
