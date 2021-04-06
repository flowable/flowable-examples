# Introduction

Running the job benchmark, means running 3 different ways of the same application (see below):

- First, generate jobs (e.g. 1M jobs of a certain type)
- Second, boot up x nodes and run the 'execute' mode, which will start the async executor (which will pick up jobs/timers automatically)
- Third, run the 'monitor' mode in a different terminal, concurrent with the execution instances


# Build the app

mvn clean package


# Create configuration file

Next to the jar, create a file `application.properties`

Example configuration:

```
spring.datasource.url=jdbc:postgresql://localhost/benchmark
spring.datasource.username=flowable
spring.datasource.password=flowable
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.maximum-pool-size=150
```

Alternatively, you can pass the properties via command line, which is often easier (see below).


# 1. Generate Jobs

Following properties are relevant for generation jobs and need to be added to application.properties:

```
mode=generate
generate.number-of-jobs=1000000
generate.threads=128
generate.process=asyncNoop 

flowable.async-executor-activate=false
```

Possible values for the process: asyncNoop | asyncWait | timer


Then run the application:

```
java -Xmx4g -jar jobapplication-0.0.1-SNAPSHOT.jar
```

or, passing the properties via command line arguments:

```
java -Xmx4g -jar -Dmode=generate -Dgenerate.number-of-jobs=100 -Dgenerate.threads=128 -Dgenerate.process=asyncNoop -Dflowable.async-executor-activate=false -Dspring.datasource.url=jdbc:postgresql://localhost/benchmark -Dspring.datasource.username=flowable -Dspring.datasource.password=flowable -Dspring.datasource.hikari.minimum-idle=10 -Dspring.datasource.hikari.maximum-pool-size=150  jobapplication-0.0.1-SNAPSHOT.jar
```

# 2. Execute Jobs


Set following properties to execute the jobs:

```
mode=execute
flowable.async-executor-activate=true

spring.task.execution.pool.core-size=128
spring.task.execution.pool.max-size=128
spring.task.execution.pool.queue-capacity=4096

globalLock=true
jobPageSize=512
lockPollRate=500
timerLockPollRate=500
```

Or passing through the command line:

```
java -Xmx4g -jar -Dmode=execute -Dflowable.async-executor-activate=true -Dspring.datasource.url=jdbc:postgresql://localhost/benchmark -Dspring.datasource.username=flowable -Dspring.datasource.password=flowable -Dspring.datasource.hikari.minimum-idle=10 -Dspring.datasource.hikari.maximum-pool-size=150 -Dspring.task.execution.pool.core-size=128 -Dspring.task.execution.pool.max-size=128 -Dspring.task.execution.pool.queue-capacity=4096 -DglobalLock=true -DjobPageSize=512 -DlockPollRate=500 -DtimerLockPollRate=500 jobapplication-0.0.1-SNAPSHOT.jar
```

# 3. Monitor


To monitor the job throughput, use mode=monitor

```
java -Xmx4g -jar -Dmode=monitor -Dflowable.async-executor-activate=false -Dspring.datasource.url=jdbc:postgresql://localhost/benchmark -Dspring.datasource.username=flowable -Dspring.datasource.password=flowable -Dspring.datasource.hikari.minimum-idle=10 -Dspring.datasource.hikari.maximum-pool-size=150 jobapplication-0.0.1-SNAPSHOT.jar
```