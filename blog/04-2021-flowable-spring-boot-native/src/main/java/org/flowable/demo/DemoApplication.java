package org.flowable.demo;

import org.flowable.engine.ProcessEngine;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ProcessEngine processEngine) {
		return new CommandLineRunner() {

			@Override
			public void run(String... args) throws Exception {
				processEngine.getRepositoryService().createDeployment()
					.disableSchemaValidation()
					.addClasspathResource("org/flowable/test.bpmn20.xml")
					.deploy();

				while (true) {
					processEngine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess");
					System.out.println("Number of tasks: " + processEngine.getTaskService().createTaskQuery().count());
					Thread.sleep(1000);
				}
			}
		};
	}

}
