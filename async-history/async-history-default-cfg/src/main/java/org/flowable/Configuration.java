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
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    public DataSource dataSource() {
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
    public ProcessEngineConfiguration processEngineConfiguration() {
        SpringProcessEngineConfiguration config = new SpringProcessEngineConfiguration();
        config.setDataSource(dataSource());
        config.setTransactionManager(transactionManager());
        config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_DROP_CREATE);
        
        // Async history configuration
        config.setAsyncHistoryEnabled(true);
        config.setAsyncHistoryExecutorActivate(true);
        
        // Optional tweaking
        config.setAsyncHistoryJsonGroupingEnabled(true);
        config.setAsyncHistoryJsonGzipCompressionEnabled(true);
        config.setAsyncHistoryJsonGroupingThreshold(10);
        
        // To speed up the example. Don't use this in production, it'll hammer the db.
        config.setAsyncExecutorDefaultAsyncJobAcquireWaitTime(50);
        
        return config;
    }

    @Bean
    public ProcessEngine processEngine() {
        return processEngineConfiguration().buildProcessEngine();
    }

}
