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

import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.MessageListener;
import javax.sql.DataSource;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.history.async.AsyncHistoryListener;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.executor.jms.MessageBasedJobManager;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

import com.atomikos.icatch.config.UserTransactionService;
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.jms.AtomikosConnectionFactoryBean;
import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean(initMethod = "init", destroyMethod = "shutdownForce")
    public UserTransactionService userTransactionService() {
        Properties properties = new Properties();
        properties.put("com.atomikos.icatch.service", "com.atomikos.icatch.standalone.UserTransactionServiceFactory");
        UserTransactionServiceImp userTransactionServiceImp = new UserTransactionServiceImp(properties);
        return userTransactionServiceImp;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(atomikosTransactionManager());
        jtaTransactionManager.setUserTransaction(userTransaction());
        return jtaTransactionManager;
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    public UserTransactionManager atomikosTransactionManager() {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(false);
        userTransactionManager.setStartupTransactionService(false);
        return userTransactionManager;
    }

    @Bean
    public UserTransaction userTransaction() {
        UserTransactionImp userTransactionImp = new UserTransactionImp();
        try {
            userTransactionImp.setTransactionTimeout(1000);
        } catch (SystemException e) {
            e.printStackTrace();
        }
        return userTransactionImp;
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    public DataSource dataSource() {
        MysqlXADataSource mysqlXaDataSource = new MysqlXADataSource();
        mysqlXaDataSource
                .setUrl("jdbc:mysql://localhost:3306/flowable?useSSL=false&characterEncoding=UTF-8&serverTimezone=UTC");
        mysqlXaDataSource.setPinGlobalTxToPhysicalConnection(true);
        mysqlXaDataSource.setPassword("flowable");
        mysqlXaDataSource.setUser("flowable");

        AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
        xaDataSource.setXaDataSource(mysqlXaDataSource);
        xaDataSource.setUniqueResourceName("xads");
        xaDataSource.setBorrowConnectionTimeout(30000);
        xaDataSource.setMinPoolSize(10);
        xaDataSource.setPoolSize(20);
        xaDataSource.setMaxPoolSize(50);
        return xaDataSource;
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    public ConnectionFactory connectionFactory() {
        ActiveMQXAConnectionFactory activeMQXAConnectionFactory = new ActiveMQXAConnectionFactory();
        activeMQXAConnectionFactory.setUseAsyncSend(true);
        activeMQXAConnectionFactory.setAlwaysSessionAsync(true);
        activeMQXAConnectionFactory.setStatsEnabled(true);
        activeMQXAConnectionFactory.setBrokerURL("tcp://127.0.0.1:61616");

        AtomikosConnectionFactoryBean atomikosConnectionFactoryBean = new AtomikosConnectionFactoryBean();
        atomikosConnectionFactoryBean.setUniqueResourceName("xamq");
        atomikosConnectionFactoryBean.setLocalTransactionMode(false);
        atomikosConnectionFactoryBean.setMaxPoolSize(100);
        atomikosConnectionFactoryBean.setBorrowConnectionTimeout(30000);
        atomikosConnectionFactoryBean.setXaConnectionFactory(activeMQXAConnectionFactory);
        return atomikosConnectionFactoryBean;
    }

    @Bean
    public ProcessEngineConfigurationImpl processEngineConfiguration() {
        SpringProcessEngineConfiguration config = new SpringProcessEngineConfiguration();
        config.setDataSource(dataSource());
        config.setTransactionManager(transactionManager());
        config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_DROP_CREATE);

        config.setAsyncHistoryEnabled(true);
        config.setAsyncHistoryExecutorActivate(true);
        config.setAsyncHistoryExecutorMessageQueueMode(true);

        config.setJobManager(jobManager());
        config.setAsyncHistoryListener(jmsAsyncHistoryListener());

        return config;
    }

    @Bean
    public AsyncHistoryListener jmsAsyncHistoryListener() {
        JmsAsyncHistoryListener jmsAsyncHistoryListener = new JmsAsyncHistoryListener();
        jmsAsyncHistoryListener.setJmsTemplate(jmsTemplate());
        return jmsAsyncHistoryListener;
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
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestination(new ActiveMQQueue("flowable-history-jobs"));
        jmsTemplate.setConnectionFactory(connectionFactory());
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }

    @Bean
    public MessageListenerContainer messageListenerContainer() {
        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();
        messageListenerContainer.setSessionTransacted(true);
        messageListenerContainer.setTransactionManager(transactionManager());
        messageListenerContainer.setConnectionFactory(connectionFactory());
        messageListenerContainer.setDestinationName("flowable-history-jobs");
        messageListenerContainer.setMessageListener(jmsListener());
        messageListenerContainer.setConcurrentConsumers(10);
        messageListenerContainer.start();
        return messageListenerContainer;
    }

    @Bean
    public MessageListener jmsListener() {
        return new ExampleJmsListener();
    }

}
