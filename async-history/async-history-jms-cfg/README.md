## Example: Flowable async history configuration using a JMS message queue

### Description

This example builds upon the knowledge gained from the [default async history configuration example](../async-history-default-cfg). 

In it, the default async history setup that uses threadpools and database polling to do its work is replaced by a Message Queue solution. In this particular example, JMS is used as the transport protocol, but any protocol works. The various components of this setup are shown below:

![Async History with message queue](src/main/resources/diagrams/async-history-message-queue-default.png "Async History with message queue")

* Instead of polling the database, a message is now sent to a Message Queue indicating that a historical job is ready.
* The history job data is inserted in the same transaction  as the runtime data. This is *very important* as we need to have transactional guarantees that the history data is not out of sync with the runtime data.
* Flowable provides an example message listener implementation, which listens on the Message Queue for new messages, fetches the job data, processes and stores it in the historical tables.
* If the data is not desired to be stored in the history tables of Flowable, it's easy to use the same mechanism to store the data in an external data store (such as MongoDb, Elasticsearch, another relational database, and so on).

![Async History with message queue 2](src/main/resources/diagrams/async-history-message-queue-custom.png "Async History with message queue 2")

Note that in this architecture, the message listeners do not have to run on the same server as the process engine.

### Dependencies

As JMS is being used here, two dependencies will need to be added to the previous example. See the [pom.xml](pom.xml) for all dependencies.


```xml
<dependency>
	<groupId>org.apache.activemq</groupId>
	<artifactId>activemq-broker</artifactId>
	<version>${activemq.version}</version>
</dependency>
<dependency>
	<groupId>org.flowable</groupId>
	<artifactId>flowable-jms-spring-executor</artifactId>
	<version>${flowable.version}</version>
</dependency>
``` 

This example uses ActiveMQ as the JMS broker, which is the first dependency.

The second dependency includes the default JMS message listener implementations that write the data to the Flowable history tables. This dependency will transitively include _spring-jms_. When not using the out-of-the-box message listeners and using Spring, make sure to include the _spring-jms_ dependency instead when removing this dependency.

### Code
 
The [example process](src/main/resources/test-process.bpmn20.xml) and [code to run it](src/main/java/org/flowable/Example.java) is exactly the same as in the default example. 
 
The configuration class is where the changes are needed, and can be found at [src/main/java/org/flowable/Configuration](src/main/java/org/flowable/Configuration.java).
 
First, set up the ActiveMQ connection factory:

```java
@Bean
public ConnectionFactory connectionFactory() {
	ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
	activeMQConnectionFactory.setUseAsyncSend(true);
	activeMQConnectionFactory.setAlwaysSessionAsync(true);
	activeMQConnectionFactory.setStatsEnabled(true);
	return new CachingConnectionFactory(activeMQConnectionFactory);
}
```   

Note that an in-memory ActiveMQ broker is being used here. To use a real and persistent ActiveMQ JMS broker, download and unzip the latest version from http://activemq.apache.org/. Run it using the script in the _bin_ folder (for example, on Linux/OSX it is _./activemq console_) and switch the third line to:

```java
ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
```

The next step is configuring the message listener:

```java
@Bean
public MessageListenerContainer messageListenerContainer() {
	DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();
	messageListenerContainer.setConnectionFactory(connectionFactory());
	messageListenerContainer.setDestinationName("flowable-history-jobs");
	messageListenerContainer.setMessageListener(historyJobsMessageListener());
	messageListenerContainer.setConcurrentConsumers(10);
	messageListenerContainer.start();
	return messageListenerContainer;
}

@Bean
public HistoryJobMessageListener historyJobsMessageListener() {
	HistoryJobMessageListener historyJobMessageListener = new HistoryJobMessageListener();
	historyJobMessageListener.setProcessEngineConfiguration(processEngineConfiguration());
	return historyJobMessageListener;
}
```

In this example, the message listeners are being run in a simple _DefaultMessageListenerContainer_ from Spring, on the same machine and even same application context as the Process Engine. In practice, this could (or really should) run on another machine. Here 10 concurrent listeners are configured to listen for new messages on the _floable-history-jobs_ queue.

The last bit to connect it all, is configuring the Process Engine to send a message to the message queue when a new Async History job is created:

```java
@Bean
public ProcessEngineConfigurationImpl processEngineConfiguration() {
	SpringProcessEngineConfiguration config = new SpringProcessEngineConfiguration();
	config.setDataSource(dataSource());
	config.setTransactionManager(transactionManager());
	config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_DROP_CREATE);

	config.setAsyncHistoryEnabled(true);
	config.setAsyncHistoryExecutorActivate(true);
        
	config.setJobManager(jobManager());
	config.setAsyncHistoryExecutorMessageQueueMode(true);

	return config;
}

@Bean
public MessageBasedJobManager jobManager() {
	MessageBasedJobManager jobManager = new MessageBasedJobManager();
	jobManager.setHistoryJmsTemplate(jmsTemplate());
	return jobManager;
}

@Bean
public JmsTemplate jmsTemplate() {
	JmsTemplate jmsTemplate = new JmsTemplate();
	jmsTemplate.setDefaultDestination(new ActiveMQQueue("flowable-history-jobs"));
	jmsTemplate.setConnectionFactory(connectionFactory());
	return jmsTemplate;
}
```

The first obvious setting is enabling the async history:

```
config.setAsyncHistoryEnabled(true);
```

The main thing here is the custom _JobManager_ that's injected into the Process Engine configuration. This _MessageBasedJobManager_ takes care of sending the message to the Message Queue.  For this case, use the _JmsTemplate_ from Spring that's configured to send messages to the _flowable-history-jobs_ queue:

```
config.setJobManager(jobManager());
config.setAsyncHistoryExecutorMessageQueueMode(true);
```

One seemingly odd thing here is that the Async History Executor is activated and configured to run in 'message queue mode':

```
config.setAsyncHistoryEnabled(true);
config.setAsyncHistoryExecutorMessageQueueMode(true);
```

Don't worry, the Async History Executor will be booted in a _light_ mode: no polling will take place, the only thing used is the 'reset of expired jobs' functionality. In case the message queue fails or is down, jobs would otherwise be in the system forever. This resetting makes sure job is sent to the Message Queue again later on.

If you want to send the historical data to a different place than the Flowable history tables, use the code from above, but change the message listener to another implementation.


