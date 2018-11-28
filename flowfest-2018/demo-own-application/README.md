## Build your own Flowable Spring Boot Application

This application is a demo application showcasing how one can build their own custom Flowable Spring Boot application.


Steps to create the demo application 

1. Go to [start.spring.io](https://start.spring.io)
1. Pick the dependencies you need (web, h2, actuator, security, devtools)

With these steps you will have a full fledged Spring Boot application 

See the [`application.properties`](src/main/resources/application.properties) for some customizations.

Convert this application to a Flowable Spring Boot application:

1. Add `flowable-spring-boot-starter` or `flowable-spring-boot-starter-rest` (the second one exposes the Flowable REST API)
1. Add some custom endpoints that use the Flowable services
    1. `HelloController` - Example controller that says hello
    1. `DefinitionsController` - Get a list of the latest deployed process keys
1. Customize the security of the application (`SecurityConfiguration`)
1. Add `UserCreatorCommandLineRunner` - creates users in the Flowable IDM engine so you can login
1. Customize the engine through defining bean `EngineConfigurationConfigurer<SpringProcessEngineConfiguration>`
1. Add some DMN and BPMN resources (they will be auto deployed when they are in the appropriate folder)
1. Add `spring-cloud-starter-function-web` for exposing Spring Cloud functions and using Flowable Services
    1. Add Function for calculating risk (see [risk](src/main/java/com/example/demoflowfest/risk) package for classes)
    1. Add Consumer for consuming vacation request (see [vacation](src/main/java/com/example/demoflowfest/vacation) package for classes)
1. Add test for showing that everything works bundled togehter