package org.flowable.jaxdemo;

import org.flowable.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class MyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }

    @RestController
    public class InsuranceRestController {

        private RuntimeService runtimeService;

        @Autowired
        public InsuranceRestController(RuntimeService runtimeService) {
            this.runtimeService = runtimeService;
        }

        @PostMapping("/insurance-claim")
        public String start() {
            return runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("myProcess")
                .variable("someData", "Hello")
                .start()
                .getId();
        }

    }

}


