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
package org.flowable.demo;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.sql.DataSource;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.history.async.message.AsyncHistoryJobMessageReceiver;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.zaxxer.hikari.HikariDataSource;

/**
 * The Listener Service application:
 * 
 * - Embedded Flowable process engine, configured by simply adding 
 *   the flowable-spring-boot-starter-basic dependency to the pom.xml
 * - A RabbitMQ listener that will receive async history data and send it to Elasticsearch  
 * 
 * @author Joram Barrez
 */
@SpringBootApplication(exclude = org.flowable.spring.boot.SecurityAutoConfiguration.class)
public class ListenerApplication {
    
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
        return container;
    }
    
    @Bean
    public MessageListenerAdapter listenerAdapter(AsyncHistoryJobMessageReceiver receiver) {
        return new MessageListenerAdapter(receiver, "messageForJobReceived");
    }
    
    @Bean
    public AsyncHistoryJobMessageReceiver asyncHistoryJobMessageReceiver(ProcessEngineConfigurationImpl processEngineConfiguration, MyJobMessageHandler messageHandler) {
        AsyncHistoryJobMessageReceiver asyncHistoryJobMessageReceiver = new AsyncHistoryJobMessageReceiver();
        asyncHistoryJobMessageReceiver.setProcessEngineConfiguration(processEngineConfiguration);
        asyncHistoryJobMessageReceiver.setAsyncHistoryJobMessageHandler(messageHandler);
        return asyncHistoryJobMessageReceiver;
    }
    
    @Bean
    public MyJobMessageHandler myJobMessageHandler(Client client) {
        return new MyJobMessageHandler(client);
    }
    
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

    @Bean(destroyMethod = "close")
    public Client elasticSearchClient() throws UnknownHostException {
        TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddresses(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
        return client;
    }
    
    @Bean
    public CommandLineRunner commandLineRunner(SimpleMessageListenerContainer simpleMessageListenerContainer) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                simpleMessageListenerContainer.start();
            }
        };
    }
    
    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(ListenerApplication.class, args);
    }

}
