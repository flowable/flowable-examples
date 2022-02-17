# Flowable Spring Native

This is an example project running the Flowable Process Engine using Spring Native and Spring Boot.

To make this work we used and modified the [`LiquibaseNativeConfiguration`](src/main/java/io/cloudnativejava/liquibase/LiquibaseNativeConfiguration.java).
The original implementation is in https://github.com/bootiful-spring-graalvm/hints

There is a [`FlowableProcessEngineNativeConfiguration`](src/main/java/org/flowable/spring/nativex/hints/FlowableProcessEngineNativeConfiguration.java) that is used to do the necessary configuration for the needed Graal Native reflections.

This project uses a Postgres database (`native`).
Before running it make sure that you have setup such a database.

To create a native image run:
```shell
./mvnw package -Pnative
```

Then run:

```shell
target/demo
```

You should see the following output:

```
2022-02-16 20:25:29.513  INFO 40462 --- [           main] o.s.nativex.NativeListener               : AOT mode enabled

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.6.3)

2022-02-16 20:25:29.517  INFO 40462 --- [           main] com.example.demo.DemoApplication         : Starting DemoApplication v0.0.1-SNAPSHOT using Java 17.0.2 on filips-macbook-pro.home with PID 40462 (/Users/filiphr/development/oss/flowable-examples/blog/2022-02-flowable-spring-native/target/demo started by filiphr in /Users/filiphr/development/oss/flowable-examples/blog/2022-02-flowable-spring-native)
2022-02-16 20:25:29.517  INFO 40462 --- [           main] com.example.demo.DemoApplication         : No active profile set, falling back to default profiles: default
2022-02-16 20:25:29.543  INFO 40462 --- [           main] o.f.s.b.ProcessEngineAutoConfiguration   : No deployment resources were found for autodeployment
2022-02-16 20:25:29.553  INFO 40462 --- [           main] o.f.s.b.e.EventRegistryAutoConfiguration : No deployment resources were found for autodeployment
2022-02-16 20:25:29.554  INFO 40462 --- [           main] o.f.s.SpringProcessEngineConfiguration   : Executing beforeInit() of class org.flowable.idm.engine.configurator.IdmEngineConfigurator (priority:150000)
2022-02-16 20:25:29.555  INFO 40462 --- [           main] o.f.s.SpringProcessEngineConfiguration   : Executing beforeInit() of class org.flowable.eventregistry.spring.configurator.SpringEventRegistryConfigurator (priority:100000)
2022-02-16 20:25:29.557  INFO 40462 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2022-02-16 20:25:29.581  INFO 40462 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2022-02-16 20:25:29.884  INFO 40462 --- [           main] o.f.s.SpringProcessEngineConfiguration   : Executing configure() of class org.flowable.idm.engine.configurator.IdmEngineConfigurator (priority:150000)
2022-02-16 20:25:29.892  INFO 40462 --- [           main] o.f.idm.engine.impl.IdmEngineImpl        : IdmEngine default created
2022-02-16 20:25:29.892  INFO 40462 --- [           main] o.f.s.SpringProcessEngineConfiguration   : Executing configure() of class org.flowable.eventregistry.spring.configurator.SpringEventRegistryConfigurator (priority:100000)
2022-02-16 20:25:29.894  WARN 40462 --- [           main] liquibase.pro.packaged.gx                : Unable to load JDK7 types (annotations, java.nio.file.Path): no Java7 support added
2022-02-16 20:25:29.927  INFO 40462 --- [           main] liquibase.database                       : Set default schema name to public
2022-02-16 20:25:29.933  INFO 40462 --- [           main] liquibase.lockservice                    : Successfully acquired change log lock
2022-02-16 20:25:30.007  INFO 40462 --- [           main] liquibase.changelog                      : Reading from public.flw_ev_databasechangelog
2022-02-16 20:25:30.027  INFO 40462 --- [           main] liquibase.lockservice                    : Successfully released change log lock
2022-02-16 20:25:30.027  INFO 40462 --- [           main] o.f.e.impl.EventRegistryEngineImpl       : EventRegistryEngine default created
2022-02-16 20:25:30.044  INFO 40462 --- [           main] o.f.engine.impl.ProcessEngineImpl        : ProcessEngine default created
2022-02-16 20:25:30.057  INFO 40462 --- [           main] o.f.j.s.i.a.AbstractAsyncExecutor        : Starting up the async job executor [org.flowable.spring.job.service.SpringAsyncExecutor] for engine bpmn
2022-02-16 20:25:30.078  INFO 40462 --- [           main] o.f.s.c.DefaultAutoDeploymentStrategy    : Deploying resources [class path resource [processes/oneTaskProcess.bpmn20.xml], class path resource [processes/oneServiceTaskDelegateProcess.bpmn20.xml]] for engine org.flowable.engine.impl.ProcessEngineImpl@50491cce deployment name hint SpringBootAutoDeployment
2022-02-16 20:25:30.078  INFO 40462 --- [       Thread-3] o.f.j.s.i.a.ResetExpiredJobsRunnable     : starting to reset expired jobs for engine bpmn
2022-02-16 20:25:30.078  INFO 40462 --- [       Thread-2] o.f.j.s.i.a.AcquireTimerJobsRunnable     : starting to acquire async jobs due for engine bpmn
2022-02-16 20:25:30.078  INFO 40462 --- [       Thread-1] o.f.j.s.i.a.AcquireAsyncJobsDueRunnable  : starting to acquire async jobs due for engine bpmn
2022-02-16 20:25:30.084  INFO 40462 --- [           main] com.example.demo.DemoApplication         : Started DemoApplication in 0.607 seconds (JVM running for 0.609)
2022-02-16 20:25:30.144  INFO 40462 --- [           main] com.example.demo.DemoApplication         : Started process oneTaskProcess
2022-02-16 20:25:30.145  INFO 40462 --- [           main] com.example.demo.DemoDelegate            : Hello Test
2022-02-16 20:25:30.189  INFO 40462 --- [           main] com.example.demo.DemoApplication         : There are 40 tasks
2022-02-16 20:33:47.678  INFO 40462 --- [ionShutdownHook] o.f.j.s.i.a.AbstractAsyncExecutor        : Shutting down the async job executor [org.flowable.spring.job.service.SpringAsyncExecutor] for engine bpmn
2022-02-16 20:33:47.678  INFO 40462 --- [uire-async-jobs] o.f.j.s.i.a.AcquireAsyncJobsDueRunnable  : stopped async job due acquisition for engine bpmn
2022-02-16 20:33:47.678  INFO 40462 --- [et-expired-jobs] o.f.j.s.i.a.ResetExpiredJobsRunnable     : stopped resetting expired jobs for engine bpmn
2022-02-16 20:33:47.678  INFO 40462 --- [uire-timer-jobs] o.f.j.s.i.a.AcquireTimerJobsRunnable     : stopped async job due acquisition for engine bpmn
2022-02-16 20:33:47.681  INFO 40462 --- [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2022-02-16 20:33:47.682  INFO 40462 --- [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
```
