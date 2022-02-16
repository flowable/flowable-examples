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
```
