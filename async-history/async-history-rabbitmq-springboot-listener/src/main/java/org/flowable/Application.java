/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable;

import javax.sql.DataSource;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.history.async.message.AsyncHistoryJobMessageReceiver;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

@SpringBootApplication
public class Application {
    
    private static final String queueName = "flowable-history-jobs";

    @Bean
    public Queue queue() {
        return new Queue(queueName, true);
    }
    
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(exchange()).with(queueName);
    }
    
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("flowable-exchange");
    }
    
    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(listenerAdapter);
        container.setConcurrentConsumers(8);
        container.start();
        return container;
    }
    
    @Bean
    public Receiver receiver() {
        Receiver receiver = new Receiver();
        receiver.setAsyncHistoryJobMessageReceiver(asyncHistoryJobMessageReceiver());
        return receiver;
    }

    @Bean
    public AsyncHistoryJobMessageReceiver asyncHistoryJobMessageReceiver() {
        AsyncHistoryJobMessageReceiver asyncHistoryJobMessageReceiver = new AsyncHistoryJobMessageReceiver();
        asyncHistoryJobMessageReceiver.setProcessEngineConfiguration(processEngineConfiguration());
        asyncHistoryJobMessageReceiver.setAsyncHistoryJobMessageHandler(myJobMessageHandler());
        return asyncHistoryJobMessageReceiver;
    }
    
    @Bean
    public MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }
    
    @Bean
    public MyJobMessageHandler myJobMessageHandler() {
        return new MyJobMessageHandler();
    }
    
    @Bean
    public DataSource dataSource() {
        String jdbcUrl = "jdbc:mysql://localhost:3306/flowable?useSSL=false&characterEncoding=UTF-8&serverTimezone=UTC";
        String jdbcDriver = "com.mysql.jdbc.Driver";
        String jdbcUsername = "flowable";
        String jdbcPassword = "flowable";

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setDriverClassName(jdbcDriver);
        dataSource.setUsername(jdbcUsername);
        dataSource.setPassword(jdbcPassword);
        dataSource.setMaximumPoolSize(50);

        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(dataSource());
        return transactionManager;
    }

    @Bean
    public ProcessEngineConfigurationImpl processEngineConfiguration() {
        SpringProcessEngineConfiguration config = new SpringProcessEngineConfiguration();
        config.setDataSource(dataSource());
        config.setTransactionManager(transactionManager());
        config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        return config;
    }

    @Bean
    public ProcessEngine processEngine() {
        return processEngineConfiguration().buildProcessEngine();
    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Application.class, args);
    }

}
