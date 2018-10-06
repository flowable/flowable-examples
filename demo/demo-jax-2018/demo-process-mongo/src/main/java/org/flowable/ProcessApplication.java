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

import org.flowable.common.engine.impl.history.HistoryLevel;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.mongodb.cfg.MongoDbProcessEngineConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author Joram Barrez
 */
@SpringBootApplication
@EnableAsync
@ComponentScan("org.flowable.demo")
public class ProcessApplication {

    @Bean
    public ProcessEngineConfiguration processEngineConfiguration() {
        return new MongoDbProcessEngineConfiguration()
            .setConnectionUrl("localhost:27017")
            .setDisableIdmEngine(true)
            .setDatabaseSchemaUpdate(MongoDbProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
            .setHistoryLevel(HistoryLevel.AUDIT);
    }

    @Bean
    public ProcessEngine processEngine() {
        return processEngineConfiguration().buildProcessEngine();
    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(ProcessApplication.class, args);
    }
    
}
