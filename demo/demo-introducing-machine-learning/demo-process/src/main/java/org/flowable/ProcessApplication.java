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

import org.flowable.demo.ProcessService;
import org.flowable.demo.RabbitMQMessageBasedJobManager;
import org.flowable.demo.StartController;
import org.flowable.dmn.spring.configurator.SpringDmnEngineConfigurator;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.form.spring.configurator.SpringFormEngineConfigurator;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Process Service Application: 
 * 
 * - Embedded Flowable process engine, configured to send async history jobs to Rabbit MQ
 * - A simple REST endpoint (see {@link StartController}) that, when called, will start up a number of process instances 
 * 
 * @author Joram Barrez
 */
@SpringBootApplication
@EnableAsync
@ComponentScan("org.flowable.demo")
public class ProcessApplication {
    
    public static final String EXCHANGE = "flowable-exchange";

    @Bean
    public DataSource dataSource() {
        
        String jdbcUrl = "jdbc:mysql://localhost/demo?useSSL=false";
        String jdbcDriver = "com.mysql.jdbc.Driver";
        String jdbcUsername = "flowable";
        String jdbcPassword = "flowable";

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setDriverClassName(jdbcDriver);
        dataSource.setUsername(jdbcUsername);
        dataSource.setPassword(jdbcPassword);
        dataSource.setMaximumPoolSize(20);

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
        
        config.addConfigurator(new SpringDmnEngineConfigurator());
        config.addConfigurator(new SpringFormEngineConfigurator());

        // Async history cfg
        config.setAsyncHistoryEnabled(true);
        config.setAsyncHistoryExecutorActivate(true);
        config.setAsyncHistoryExecutorMessageQueueMode(true);
        config.setJobManager(jobManager());

        return config;
    }

    @Bean
    public ProcessEngine processEngine() {
        return processEngineConfiguration().buildProcessEngine();
    }

    @Bean
    public RabbitMQMessageBasedJobManager jobManager() {
        RabbitMQMessageBasedJobManager jobManager = new RabbitMQMessageBasedJobManager();
        jobManager.setRabbitTemplate(rabbitTemplate());
        return jobManager;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory());
        rabbitTemplate.setExchange(EXCHANGE);
        return rabbitTemplate;
    }
    
    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }
    
    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }
    
    @Bean
    public CommandLineRunner schedulingRunner(final TaskExecutor executor, final AmqpAdmin amqpAdmin, final ProcessService processStarter) {
        return args -> executor.execute(() -> {
            
            // Init Rabbit exchange and queue
            amqpAdmin.deleteExchange(EXCHANGE);
            TopicExchange exchange = new TopicExchange(EXCHANGE);
            amqpAdmin.declareExchange(exchange);
            Queue queue = new Queue("flowable-history-jobs", true);
            amqpAdmin.declareQueue(queue);
            amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("flowable-history-jobs"));
          
        });
    }
    
    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(ProcessApplication.class, args);
    }
    
}
