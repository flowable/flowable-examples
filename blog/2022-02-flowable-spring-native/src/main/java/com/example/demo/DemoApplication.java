package com.example.demo;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

    public static final Logger LOGGER = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public DemoDelegate demoDelegate() {
        return new DemoDelegate();
    }

    @Bean
    public CommandLineRunner commandLineRunner(RepositoryService repositoryService, RuntimeService runtimeService, TaskService taskService,
            ProcessEngine processEngine) {
        return args -> {

            repositoryService.createDeployment()
                    .name("Native manual deployment")
                    .addClasspathResource("processes/oneTaskProcess.bpmn20.xml")
                    .addClasspathResource("processes/oneServiceTaskDelegateProcess.bpmn20.xml")
                    .deploy();
            ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                    .processDefinitionKey("oneTaskProcess")
                    .start();

            LOGGER.info("Started process {}", processInstance.getProcessDefinitionKey());

            processInstance = runtimeService.createProcessInstanceBuilder()
                    .processDefinitionKey("oneServiceTaskDelegateProcess")
                    .variable("user", "Test")
                    .start();

            LOGGER.info("There are {} tasks", taskService.createTaskQuery().count());
        };
    }

}
