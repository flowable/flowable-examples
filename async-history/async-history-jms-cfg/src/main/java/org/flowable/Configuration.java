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

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.executor.jms.HistoryJobMessageListener;
import org.flowable.spring.executor.jms.MessageBasedJobManager;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    public DataSource dataSource() {

        // MySQL
//        String jdbcUrl = "jdbc:mysql://localhost:3306/flowable?useSSL=false&characterEncoding=UTF-8&serverTimezone=UTC";
//        String jdbcDriver = "com.mysql.jdbc.Driver";
//        String jdbcUsername = "flowable";
//        String jdbcPassword = "flowable";

        String jdbcUrl = "jdbc:h2:mem:flowable;DB_CLOSE_DELAY=1000;MVCC=TRUE";
        String jdbcDriver = "org.h2.Driver";
        String jdbcUsername = "sa";
        String jdbcPassword = "";

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
        config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_DROP_CREATE);

        // Async history configuration
        config.setAsyncHistoryEnabled(true);
        config.setAsyncHistoryExecutorActivate(true);
        
        // Optional settings
        config.setAsyncHistoryJsonGroupingEnabled(true);
        config.setAsyncHistoryJsonGzipCompressionEnabled(false);
        config.setAsyncHistoryJsonGroupingThreshold(10);
        
        // Async history JMS settings
        config.setJobManager(jobManager());
        config.setAsyncHistoryExecutorMessageQueueMode(true);

        return config;
    }

    @Bean
    public ProcessEngine processEngine() {
        return processEngineConfiguration().buildProcessEngine();
    }

    @Bean
    public MessageBasedJobManager jobManager() {
        MessageBasedJobManager jobManager = new MessageBasedJobManager();
        jobManager.setHistoryJmsTemplate(jmsTemplate());
        return jobManager;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        
        // Using an in memory ms queue for easy of demonstration
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        
        // Uncomment the following line if a real, standalone ActiveMQ JMS queue should be used  
        //ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
        
        activeMQConnectionFactory.setUseAsyncSend(true);
        activeMQConnectionFactory.setAlwaysSessionAsync(true);
        activeMQConnectionFactory.setStatsEnabled(true);
        return new CachingConnectionFactory(activeMQConnectionFactory);
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestination(new ActiveMQQueue("flowable-history-jobs"));
        jmsTemplate.setConnectionFactory(connectionFactory());
        return jmsTemplate;
    }

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

}
